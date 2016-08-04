package com.lightstep.tracer.shared;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.thrift.TException;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;

import io.opentracing.References;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import com.lightstep.tracer.thrift.Auth;
import com.lightstep.tracer.thrift.Command;
import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.Runtime;
import com.lightstep.tracer.thrift.SpanRecord;
import com.lightstep.tracer.thrift.ReportingService;
import com.lightstep.tracer.thrift.ReportRequest;
import com.lightstep.tracer.thrift.ReportResponse;
import com.lightstep.tracer.shared.SimpleFuture;

public abstract class AbstractTracer implements Tracer {
  // Delay before sending the initial report
  private static final long REPORTING_DELAY_MILLIS = 20;
  // Maximum interval between reports
  private static final long DEFAULT_CLOCK_STATE_INTERVAL_MILLIS = 500;
  private static final int DEFAULT_MAX_BUFFERED_SPANS = 1000;
  private static final int DEFAULT_REPORT_TIMEOUT_MILLIS = 10 * 1000;

  private static final String DEFAULT_HOST = "collector.lightstep.com";
  private static final int DEFAULT_SECURE_PORT = 443;
  private static final int DEFAULT_PLAINTEXT_PORT = 80;
  private static final String COLLECTOR_PATH = "/_rpc/v1/reports/binary";

  public static final String LIGHTSTEP_TRACER_PLATFORM_KEY = "lightstep.tracer_platform";
  public static final String LIGHTSTEP_TRACER_PLATFORM_VERSION_KEY = "lightstep.tracer_platform_version";
  public static final String LIGHTSTEP_TRACER_VERSION_KEY = "lightstep.tracer_version";
  public static final String LEGACY_COMPONENT_NAME_KEY = "component_name";
  public static final String COMPONENT_NAME_KEY = "lightstep.component_name";
  public static final String GUID_KEY = "lightstep.guid";

  protected static final int VERBOSITY_DEBUG = 4;
  protected static final int VERBOSITY_INFO = 3;
  protected static final int VERBOSITY_ERRORS_ONLY = 2;
  protected static final int VERBOSITY_FIRST_ERROR_ONLY = 1;
  protected static final int VERBOSITY_NONE = 0;

  /**
   * For mapping internal logs to Android log levels without importing Android
   * packages.
   */
  protected enum InternalLogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
  }

  /**
   * The tag key used to record the relationship between child and parent
   * spans.
   */
  public static final String PARENT_SPAN_GUID_KEY = "parent_span_guid";

  // copied from options
  private final int maxBufferedSpans;
  protected final int verbosity;
  protected int visibleErrorCount;

  private final Auth auth;
  protected final Runtime runtime;
  private URL collectorURL;

  protected ArrayList<SpanRecord> spans;
  protected ClockState clockState;
  protected ClientMetrics clientMetrics;

  // Should *NOT* attempt to take a span's lock while holding this lock.
  protected final Object mutex = new Object();
  private boolean reportInProgress;
  private ReportingLoop reportingLoop;
  protected boolean isDisabled;

  protected TTransport transport;
  protected ReportingService.Client client;

  public AbstractTracer(Options options) {
    // Set verbosity first so debug logs from the constructor take effect
    this.verbosity = options.verbosity;

    // TODO sanity check options
    this.maxBufferedSpans = options.maxBufferedSpans > 0 ?
      options.maxBufferedSpans : DEFAULT_MAX_BUFFERED_SPANS;
    this.spans = new ArrayList<SpanRecord>(maxBufferedSpans);

    this.clockState = new ClockState();
    this.clientMetrics = new ClientMetrics();
    this.visibleErrorCount = 0;

    this.auth = new Auth();
    auth.setAccess_token(options.accessToken);

    // Set some default attributes if not found in options
    if (options.tags.get(COMPONENT_NAME_KEY) == null) {
      // TODO: support other ways of setting component name by default
      String s = System.getProperty("sun.java.command");
      if (s != null) {
        StringTokenizer st = new StringTokenizer(s);
        if (st.hasMoreTokens()) {
          String name = st.nextToken();
          options.tags.put(COMPONENT_NAME_KEY, name);
          options.tags.put(LEGACY_COMPONENT_NAME_KEY, name);
        }
      }
    }

    String guid;
    if (options.tags.get(GUID_KEY) == null) {
      guid = generateGUID();
      options.tags.put(GUID_KEY, guid);
    } else {
      guid = options.tags.get(GUID_KEY).toString();
    }

    this.runtime = new Runtime();
    this.runtime.setGuid(guid);
    this.runtime.setStart_micros(System.currentTimeMillis() * 1000);
    for (Map.Entry<String, Object> entry : options.tags.entrySet()) {
      this.addTracerTag(entry.getKey(), entry.getValue().toString());
    }

    String host = options.collectorHost != null ? options.collectorHost : DEFAULT_HOST;
    int port;
    String scheme = "https";
    if (options.collectorEncryption == Options.Encryption.NONE) {
      scheme = "http";
      port = options.collectorPort > 0 ? options.collectorPort : DEFAULT_PLAINTEXT_PORT;
    } else {
      port = options.collectorPort > 0 ? options.collectorPort : DEFAULT_SECURE_PORT;
    }
    try {
      this.collectorURL = new URL(scheme, host, port, COLLECTOR_PATH);
    } catch (MalformedURLException e) {
      this.error("Collector URL malformed. Disabling tracer.", e);
      // Preemptively disable this tracer.
      this.disable();
      return;
    }

    if (!options.disableReportingLoop) {
      this.debug("Starting reporting loop.");
      this.reportingLoop = new ReportingLoop(options);
      (new Thread(this.reportingLoop)).start();
    } else {
      this.debug("Reporting loop is disabled");
    }

    if (!options.disableReportOnExit) {
      java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
            AbstractTracer.this.debug("Running shutdown hook");
            AbstractTracer.this.shutdown();
        }
      });
    } else {
      this.debug("Report at exit is disabled");
    }
  }

  public String getAccessToken() {
    synchronized (this.mutex) {
      return auth.getAccess_token();
    }
  }

  protected abstract long getDefaultReportingIntervalMillis();

  class ReportingLoop implements Runnable {
    // Controls how often the reporting loop itself checks if there's any work to do: this is
    // *not* the reporting interval itself! This should be kept relatively short as
    // this defines the gap between process exit and the start of the final at-exit
    // report.
    private static final int POLL_INTERVAL_MILLIS = 40;

    private AtomicBoolean active = new AtomicBoolean(false);
    private Random rng = new Random(System.currentTimeMillis());
    private long reportingIntervalMillis = 0;
    private int consecutiveFailures = 0;

    ReportingLoop(Options options) {
      this.reportingIntervalMillis = options.maxReportingIntervalSeconds > 0 ?
              options.maxReportingIntervalSeconds * 1000 :
              AbstractTracer.this.getDefaultReportingIntervalMillis();
    }

    @Override
    public void run() {
        this.active.set(true);
        long nextReportMillis = this.computeNextReportMillis();

        // Run until the reporting loop has been stopped
        while (this.active.get()) {
          // Check if it's time for the next report
          long nowMillis = System.currentTimeMillis();
          if (nowMillis >= nextReportMillis) {
            // TODO: nextReportMillis should be computed once the flush has succeeded or
            // failed. flush() is currently an async call that has no signal as to when
            // it completes.
            SimpleFuture<Boolean> result = AbstractTracer.this.flushInternal();
            boolean reportSucceeded = false;
            try {
              reportSucceeded = result.get();
            } catch (InterruptedException e) {
              AbstractTracer.this.warn("Future timed out");
            }

            if (!reportSucceeded) {
              this.consecutiveFailures++;
            } else {
              this.consecutiveFailures = 0;
            }

            nextReportMillis = this.computeNextReportMillis();
          }

          try {
            Thread.sleep(POLL_INTERVAL_MILLIS);
          } catch (InterruptedException e) {
            AbstractTracer.this.warn("Exception trying to sleep in reporting thread");
          }
        }
    }

    /**
     * Stops the reporting loop as soon any currently executing task finishes.
     */
    public void stop() {
      this.active.set(false);
    }

    protected long computeNextReportMillis() {
      double base;
      if (!AbstractTracer.this.clockState.isReady()) {
        base = (double)AbstractTracer.this.DEFAULT_CLOCK_STATE_INTERVAL_MILLIS;
      } else {
        base = (double)this.reportingIntervalMillis;
      }

      // Exponential back off based on number of consecutive errors, up to 8x the normal
      // interval
      int backOff = 1 + Math.min(7, this.consecutiveFailures);
      base *= (double)backOff;

      // Add +/- 10% jitter to the regular reporting interval
      final double delta = base * (0.9 + 0.2 * this.rng.nextDouble());
      final long nextMillis = System.currentTimeMillis() + (long)Math.ceil(delta);
      AbstractTracer.this.debug(String.format("Next report: %d (%f) [%d]", nextMillis, delta, AbstractTracer.this.clockState.activeSampleCount()));
      return nextMillis;
    }
  }

  /**
   * Gracefully stops the tracer.
   *
   * NOTE: this can optionally be called by the consumer of the library and,
   * if the consumer has not alreayd called it, *must* be called internally by
   * the library to cancel the timer.
   */
  public void shutdown() {
    if (isDisabled) {
      return;
    }

    this.debug("shutdown() called");
    if (this.reportingLoop != null) {
      this.reportingLoop.stop();
    }
    flush();
    disable();
  }

  /**
   * Disable the tracer, stopping any further reports and turning all
   * subsequent method invocations into no-ops.
   */
  public void disable() {
    this.info("Disabling client library");
    synchronized (this.mutex) {
      if (this.reportingLoop != null) {
        this.reportingLoop.stop();
      }

      if (this.transport != null) {
        this.transport.close();
        this.transport = null;
      }

      this.isDisabled = true;

      // The code makes various assumptions about this field never being
      // null, so replace it with an empty list rather than nulling it out.
      this.spans = new ArrayList<SpanRecord>(0);
    }
  }

  public Tracer.SpanBuilder buildSpan(String operationName) {
    return this.new SpanBuilder(operationName);
  }

  public <C> void inject(io.opentracing.SpanContext spanContext, Format<C> format, C carrier) {
    SpanContext lightstepSpanContext = (SpanContext)spanContext;
    if (format == Format.Builtin.TEXT_MAP) {
      Propagator.TEXT_MAP.inject(lightstepSpanContext, (TextMap)carrier);
    } else if (format == Format.Builtin.HTTP_HEADERS) {
      Propagator.HTTP_HEADERS.inject(lightstepSpanContext, (TextMap)carrier);
    } else if (format == Format.Builtin.BINARY) {
      this.warn("LightStep-java does not yet support binary carriers. " +
          "SpanContext: " + spanContext.toString());
      Propagator.BINARY.inject(lightstepSpanContext, (ByteBuffer)carrier);
    } else {
      this.info("Unsupported carrier type: " + carrier.getClass());
    }
  }

  public <C> io.opentracing.SpanContext extract(Format<C> format, C carrier) {
    if (format == Format.Builtin.TEXT_MAP) {
      return Propagator.TEXT_MAP.extract((TextMap)carrier);
    } else if (format == Format.Builtin.HTTP_HEADERS) {
      return Propagator.HTTP_HEADERS.extract((TextMap)carrier);
    } else if (format == Format.Builtin.BINARY) {
      this.warn("LightStep-java does not yet support binary carriers.");
      return Propagator.BINARY.extract((ByteBuffer)carrier);
    } else {
      this.info("Unsupported carrier type: " + carrier.getClass());
      return null;
    }
  }

  public void flush() {
    // TODO: does OpenTracing specify if this is a synchronous or asynchronous method?
    // It seems like it would have to be synchronous (i.e. this method should wait for
    // the promise retunred by flushInternal() to complete), but the current LightStep
    // implementation code calls this in several places and *may* be relying on the
    // current async behavior.
    this.flushInternal();
  }

  protected abstract SimpleFuture<Boolean> flushInternal();

  /**
   * Does the work of a flush by sending spans to the collector.
   *
   * @param explicitRequest   if true, the report request was made explicitly
   *                          rather than implicitly (via a reporting loop) and
   *                          therefore the code should make a 'best effort' to
   *                          truly report (i.e. send even if the clock state is
   *                          not ready).
   * @return true if the report was sent successfully
   */
  protected boolean sendReport(boolean explicitRequest) {

    synchronized (this.mutex) {
      if (this.reportInProgress) {
        this.debug("Report in progress. Skipping.");
        return true;
      }
      if (this.spans.size() == 0 && this.clockState.isReady()) {
        this.debug("Skipping report. No new data.");
        return true;
      }

      // Make sure other threads don't try to start sending a report.
      this.reportInProgress = true;
    }

    try {
      return sendReportWorker(explicitRequest);
    } finally {
      synchronized (this.mutex) {
        this.reportInProgress = false;
      }
    }
  }

  /**
   * Private worker function for sendReport() to make the locking and guard
   * variable bracketing a little more straightforward.
   *
   * Returns false in the case of an error. True if the report was successful.
   */
  private boolean sendReportWorker(boolean explicitRequest) {
    // Data to be sent. Maybe be merged back into the local buffers if the
    // report fails.
    ArrayList<SpanRecord> spans;
    ClientMetrics clientMetrics = null;

    synchronized (this.mutex) {
      if (this.clockState.isReady() || explicitRequest) {
        // Copy the reference to the spans and make a new array for other spans.
        spans = this.spans;
        clientMetrics = this.clientMetrics;
        this.spans = new ArrayList<SpanRecord>(this.maxBufferedSpans);
        this.clientMetrics = new ClientMetrics();
        this.debug(String.format("Sending report, %d spans", spans.size()));
      } else {
        // Otherwise, if the clock state is not ready, we'll send an empty
        // report.
        this.debug("Sending empty report to prime clock state");
        spans = new ArrayList<SpanRecord>();
      }

      if (this.transport == null) {
        this.debug("Creating transport");
        try {
          // TODO add support for cookies (for load balancer sessions)
          THttpClient client = new THttpClient(this.collectorURL.toString());
          client.setConnectTimeout(this.DEFAULT_REPORT_TIMEOUT_MILLIS);
          this.transport = client;
          this.transport.open();
          TBinaryProtocol protocol = new TBinaryProtocol(this.transport);
          this.client = new ReportingService.Client(protocol);
        } catch (TException e) {
          this.error("Exception creating Thrift client. Disabling tracer.", e);
          this.disable();
          return false;
        }
      }
    }

    ReportRequest req = new ReportRequest();
    req.setRuntime(this.runtime);
    req.setSpan_records(spans);
    req.setTimestamp_offset_micros(this.clockState.offsetMicros());

    if (clientMetrics != null) {
      req.setInternal_metrics(clientMetrics.toThrift());
    }

    try {
      long originMicros = System.currentTimeMillis() * 1000;
      ReportResponse resp = this.client.Report(this.auth, req);

      if (resp.isSetTiming()) {
        this.clockState.addSample(originMicros,
                                  resp.getTiming().getReceive_micros(),
                                  resp.getTiming().getTransmit_micros(),
                                  System.currentTimeMillis() * 1000);
      } else {
        this.warn("Collector response did not include timing info");
      }

      // Check whether or not to disable the tracer
      if (resp.isSetCommands()) {
        for (Command command : resp.commands) {
          if (command.disable) {
            this.disable();
          }
        }
      }

      this.debug(String.format("Report sent successfully (%d spans)", spans.size()));
      return true;

    } catch (TApplicationException e) {
      // Log as this probably indicates malformed spans
      this.error("TApplicationException: error from collector", e);
      return false;
    } catch (TException x) {
      // This may include exceptions like connection timeouts, which are expected during
      // normal operation.
      this.debug("Report failed with exception", x);

      // The request failed, add any data that was supposed to be sent back to the
      // client local buffers.
      synchronized(this.mutex) {
        this.clientMetrics.merge(clientMetrics);
      }

      // TODO should probably prepend and do it in bulk
      for (SpanRecord span : req.span_records) {
        addSpan(span);
      }
      return true;
    }
  }

  /**
   * Adds a span to the buffer.
   *
   * @param span the span to be added
   */
  void addSpan(SpanRecord span) {
    synchronized (this.mutex) {
      if (this.spans.size() >= this.maxBufferedSpans) {
        this.clientMetrics.spansDropped++;
      } else {
        this.spans.add(span);
      }
    }
  }

  static final String generateGUID() {
    // Note that ThreadLocalRandom is a singleton, thread safe Random Generator
    long guid = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
    return Long.toHexString(guid);
  }

  protected void addTracerTag(String key, String value) {
    this.debug("Adding tracer tag: " + key + " => " + value);
    this.runtime.addToAttrs(new KeyValue(key, value));
  }

  class SpanBuilder implements Tracer.SpanBuilder{
    private String operationName;
    private SpanContext parent;
    private Map<String, String> tags;
    private long startTimestampMicros;

    SpanBuilder(String operationName) {
      this.operationName = operationName;
      this.tags = new HashMap<String, String>();
    }

    public Tracer.SpanBuilder asChildOf(io.opentracing.Span parent) {
      return this.asChildOf(parent.context());
    }

    public Tracer.SpanBuilder asChildOf(io.opentracing.SpanContext parent) {
      return this.addReference(References.CHILD_OF, parent);
    }

    public Tracer.SpanBuilder addReference(String type, io.opentracing.SpanContext referredTo) {
      if (type == References.CHILD_OF || type == References.FOLLOWS_FROM) {
        this.parent = (SpanContext)referredTo;
      }
      return this;
    }

    public Tracer.SpanBuilder withTag(String key, String value) {
      this.tags.put(key, value);
      return this;
    }

    public Tracer.SpanBuilder withTag(String key, boolean value) {
      this.tags.put(key, value ? "true" : "false");
      return this;
    }

    public Tracer.SpanBuilder withTag(String key, Number value) {
      this.tags.put(key, value.toString());
      return this;
    }

    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
      this.startTimestampMicros = microseconds;
      return this;
    }

    public io.opentracing.Span start() {
      synchronized (AbstractTracer.this.mutex) {
        if (AbstractTracer.this.isDisabled) {
          return NoopSpan.INSTANCE;
        }
      }

      if (this.startTimestampMicros == 0) {
        this.startTimestampMicros = System.currentTimeMillis() * 1000;
      }

      SpanRecord record = new SpanRecord();
      record.setSpan_name(this.operationName);
      record.setOldest_micros(this.startTimestampMicros);

      String traceId = null;
      if (this.parent != null && this.parent instanceof SpanContext) {
        traceId = this.parent.getTraceId();
        record.addToAttributes(new KeyValue(
              PARENT_SPAN_GUID_KEY,
              this.parent.getSpanId()));
      }
      SpanContext newSpanContext = new SpanContext(traceId); // traceId may be null
      // Record the eventual TraceId and SpanId in the SpanRecord.
      record.setTrace_guid(newSpanContext.getTraceId());
      record.setSpan_guid(newSpanContext.getSpanId());

      Span span = new Span(AbstractTracer.this, newSpanContext, record);
      for (Map.Entry<String, String> pair : this.tags.entrySet()) {
           span.setTag(pair.getKey(), pair.getValue());
      }
      return span;
    }

    public io.opentracing.Span start(long microseconds) {
      return this.withStartTimestamp(microseconds).start();
    }
  }

  /**
   * Internal logging.
   */
  protected void debug(String s) {
    this.debug(s, null);
  }

  /**
   * Internal logging.
   */
  protected void debug(String msg, Object payload) {
    if (this.verbosity < VERBOSITY_DEBUG) {
        return;
    }
    this.printLogToConsole(InternalLogLevel.DEBUG, msg, payload);
  }

  /**
   * Internal logging.
   */
  protected void info(String s) {
    this.info(s, null);
  }

  /**
   * Internal logging.
   */
  protected void info(String msg, Object payload) {
    if (this.verbosity < VERBOSITY_INFO) {
        return;
    }
    this.printLogToConsole(InternalLogLevel.INFO, msg, payload);
  }

  /**
   * Internal logging.
   */
  protected void warn(String s) {
    this.warn(s, null);
  }

  /**
   * Internal warning.
   */
  protected void warn(String msg, Object payload) {
    if (this.verbosity < VERBOSITY_INFO) {
        return;
    }
    this.printLogToConsole(InternalLogLevel.WARN, msg, payload);
  }

  /**
   * Internal logging.
   */
  protected void error(String s) {
    this.error(s, null);
  }

  /**
   * Internal error.
   */
  protected void error(String msg, Object payload) {
    if (this.verbosity < VERBOSITY_FIRST_ERROR_ONLY) {
      return;
    }
    if (this.verbosity == VERBOSITY_FIRST_ERROR_ONLY && this.visibleErrorCount > 0) {
      return;
    }
    this.visibleErrorCount++;
    this.printLogToConsole(InternalLogLevel.ERROR, msg, payload);
  }

  protected abstract void printLogToConsole(InternalLogLevel level, String msg, Object payload);

  /**
   * Internal class used primarily for unit testing and debugging. This is not
   * part of the OpenTracing API and is not a supported API.
   */
  public class Status {
    public Map<String, String> tags;
    public ClientMetrics clientMetrics;

    public Status() {
      this.tags = new HashMap<String, String>();
    }
  }

  /**
   * Internal method used primarily for unit testing and debugging. This is not
   * part of the OpenTracing API and is not a supported API.
   *
   * Copies the internal state/status into an object that's easier to check
   * against in unit tests.
   */
  public Status status() {
    Status status = new Status();
    synchronized (this.mutex) {
      for (KeyValue pair : this.runtime.getAttrs()) {
        status.tags.put(pair.getKey(), pair.getValue());
      }
      status.clientMetrics = new ClientMetrics(this.clientMetrics);
    }
    return status;
  }

}
