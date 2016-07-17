package com.lightstep.tracer.shared;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

import com.lightstep.tracer.thrift.Auth;
import com.lightstep.tracer.thrift.Command;
import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.Runtime;
import com.lightstep.tracer.thrift.SpanRecord;
import com.lightstep.tracer.thrift.ReportingService;
import com.lightstep.tracer.thrift.ReportRequest;
import com.lightstep.tracer.thrift.ReportResponse;
import com.lightstep.tracer.shared.Span;

import io.opentracing.References;
import io.opentracing.Tracer;

public abstract class AbstractTracer implements Tracer {
  // Delay before sending the initial report
  private static final long REPORTING_DELAY_MILLIS = 20;
  // Maximum interval between reports
  private static final long DEFAULT_REPORTING_INTERVAL_MILLIS = 2500;
  private static final int DEFAULT_MAX_BUFFERED_SPANS = 1000;

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

  /**
   * For mapping internal logs to Android log levels without importing Android
   * pacakges.
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
  private AtomicBoolean hasUnreportSpans;
  private final Timer timer;
  private final ThreadPoolExecutor executor;
  protected boolean isDisabled;

  protected TTransport transport;
  protected ReportingService.Client client;

  public AbstractTracer(Options options) {

    // A description of the background flush / threading setup:
    //
    // First off, the current background flush setup can likely be simplified.
    // There are three interacting parts, which seems overly complicated for what
    // needs to be done (especially given that Android has specific API for background
    // network calls). Regardless, on to the details...
    //
    // TIMER: the AbstractTracer creates a *daemon* timer which fires at a regular
    // interval to trigger background flush. This is good because the daemon timer
    // does not prevent the JVM from shutting down (otherwise, the client library
    // would require an explicit shutdown() call from the consumer of the library).
    // The downside, is the daemon threads are killed with no chance to clean-up
    // which means if the sendReport implementation is invoked from the daemon
    // thread it may be killed and leave the Tracer in a bad state - which prevents
    // the Tracer from doing a final, at-shutdown flush.
    //
    // EXECUTOR: to resolve this, the timer, instead of doing the work itself,
    // runs the flush in a ThreadPoolExecutor that won't get killed arbitrarily.
    // The thread pool itself will keep the JVM from shutting down (thus the
    // point of a daemon timer in the first place), so the thread pool is given
    // a keep-alive time where the thread is released if there's no work for the
    // pool.  Thus the JVM is held up for a max of the keep-alive time. (Also,
    // if the time out happens in normal operation, the pool automatically will
    // recreate the thread if new tasks come in.)
    //
    // ATOMIC BOOLEAN: lastly there's an AtomicBoolean that gets set every time
    // the Tracer gets a new span. This allows the timer to avoid putting new
    // tasks into the executor when there's no work to be done (and thus
    // constantly refreshing the keep-alive, preventing a clean shutdown). It's
    // an atomic bool so the daemon thread doesn't have to touch the Tracer's
    // mutex and potentially leave that in a bad state if killed.
    //
    // This executor setup comes from:
    // http://stackoverflow.com/questions/13883293/turning-an-executorservice-to-daemon-in-java
    //
    // TODO: the timer should have some jitter in the interval
    this.timer = new Timer(true);
    this.executor = new ThreadPoolExecutor(1, 1, 2 * DEFAULT_REPORTING_INTERVAL_MILLIS, MILLISECONDS,
      new java.util.concurrent.LinkedBlockingQueue<Runnable>());
    this.executor.allowCoreThreadTimeOut(true);

    // TODO sanity check options
    this.maxBufferedSpans = options.maxBufferedSpans > 0 ?
      options.maxBufferedSpans : DEFAULT_MAX_BUFFERED_SPANS;
    this.hasUnreportSpans = new AtomicBoolean(false);
    this.spans = new ArrayList<SpanRecord>(maxBufferedSpans);

    this.clockState = new ClockState();
    this.clientMetrics = new ClientMetrics();
    this.verbosity = options.verbosity;
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

    // Schedule a fixed-delay task.
    this.debug("Starting reporting timer.");
    long interval = options.maxReportingIntervalSeconds > 0 ?
      options.maxReportingIntervalSeconds * 1000 : DEFAULT_REPORTING_INTERVAL_MILLIS;
    timer.schedule(new FlushTimer(), REPORTING_DELAY_MILLIS, interval);

    java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
       public void run() {
         AbstractTracer.this.debug("Running shutdown hook");
         AbstractTracer.this.shutdown();
       }
    });
  }

  public String getAccessToken() {
    synchronized (this.mutex) {
      return auth.getAccess_token();
    }
  }

  /**
   * Extends TimerTask to call flush().
   */
  class FlushRunnable implements Runnable {
    @Override
    public void run() {
      AbstractTracer.this.flush();
    }
  }

  /**
   * Extends TimerTask to call flush().
   */
  class FlushTimer extends TimerTask {
    @Override
    public void run() {
      if (!AbstractTracer.this.hasUnreportSpans.get()) {
        return;
      }
      AbstractTracer.this.executor.execute(new FlushRunnable());
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
    this.timer.cancel();
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
      this.timer.cancel();
      this.timer.purge();

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

  public void inject(io.opentracing.SpanContext spanContext, Object carrier) {
    SpanContext lightstepSpanContext = (SpanContext)spanContext;
    if (Propagator.TEXT_MAP.matchesInjectCarrier(carrier)) {
      Propagator.TEXT_MAP.inject(lightstepSpanContext, carrier);
    } else if (Propagator.HTTP_HEADER.matchesInjectCarrier(carrier)) {
      Propagator.HTTP_HEADER.inject(lightstepSpanContext, carrier);
    } else if (Propagator.BINARY.matchesInjectCarrier(carrier)) {
      this.warn("LightStep-java does not yet support binary carriers. " +
          "SpanContext: " + spanContext.toString());
      Propagator.BINARY.inject(lightstepSpanContext, carrier);
    } else {
      this.info("Unsupported carrier type: " + carrier.getClass());
    }
  }

  public io.opentracing.SpanContext extract(Object carrier) {
    if (Propagator.TEXT_MAP.matchesExtractCarrier(carrier)) {
      return Propagator.TEXT_MAP.extract(carrier);
    } else if (Propagator.HTTP_HEADER.matchesExtractCarrier(carrier)) {
      return Propagator.HTTP_HEADER.extract(carrier);
    } else if (Propagator.BINARY.matchesExtractCarrier(carrier)) {
      this.warn("LightStep-java does not yet support binary carriers.");
      return Propagator.BINARY.extract(carrier);
    } else {
      this.info("Unsupported carrier type: " + carrier.getClass());
      return null;
    }
  }

  /**
   * Sends buffered data to the collector.
   *
   * TODO: calls to flush() should likely be reversed to explicit, client calls
   * to flush the buffered data ASAP/synchronously -- the internal reporting loop likely
   * should use a different mechanism.  Otherwise there is no way to differentiate
   * the two (which require slightly different implementations).
   *
   * This method should invoke sendReport() on a thread appropriate for making
   * network calls.
   */
  public abstract void flush();

  /**
   * Does the work of a flush by sending spans to the collector.
   *
   * @param explicitRequest   if true, the report request was made explicitly
   *                          rather than implicitly (via a reporting loop) and
   *                          therefore the code should make a 'best effort' to
   *                          truly report (i.e. send even if the clock state is
   *                          not ready).
   */
  protected void sendReport(boolean explicitRequest) {

    synchronized (this.mutex) {
      if (this.reportInProgress) {
        this.debug("Report in progress. Skipping.");
        return;
      }
      if (this.spans.size() == 0 && this.clockState.isReady()) {
        this.debug("Skipping report. No new data.");
        return;
      }

      // Make sure other threads don't try to start sending a report.
      this.reportInProgress = true;
    }

    try {
      sendReportWorker(explicitRequest);
    } finally {
      synchronized (this.mutex) {
        this.reportInProgress = false;
      }
    }
  }

  /**
   * Private worker function for sendReport() to make the locking and guard
   * variable bracketing a little more straightforward.
   */
  private void sendReportWorker(boolean explicitRequest) {
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
      } else {
        // Otherwise, if the clock state is not ready, we'll send an empty
        // report.
        spans = new ArrayList<SpanRecord>();
      }

      if (this.transport == null) {
        this.debug("Creating transport");
        try {
          // TODO add support for cookies (for load balancer sessions)
          this.transport = new THttpClient(this.collectorURL.toString());
          this.transport.open();
          TBinaryProtocol protocol = new TBinaryProtocol(this.transport);
          this.client = new ReportingService.Client(protocol);
        } catch (TException e) {
          this.error("Exception creating Thrift client. Disabling tracer.", e);
          this.disable();
          return;
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
      this.debug("Sending report");
      long originMicros = System.currentTimeMillis() * 1000;
      ReportResponse resp = this.client.Report(this.auth, req);
      this.hasUnreportSpans.set(false);
      this.debug("Report sent");

      if (resp.isSetTiming()) {
        this.clockState.addSample(originMicros,
                                  resp.getTiming().getReceive_micros(),
                                  resp.getTiming().getTransmit_micros(),
                                  System.currentTimeMillis() * 1000);
      }
      // Check whether or not to disable the tracer
      if (resp.isSetCommands()) {
        for (Command command : resp.commands) {
          if (command.disable) {
            this.disable();
          }
        }
      }

      this.debug("Report sent successfully");

    } catch (TApplicationException e) {
      // Log as this probably indicates malformed spans
      this.error("TApplicationException: error from collector", e);
    } catch (TException x) {

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
        this.hasUnreportSpans.set(true);
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

    public Tracer.SpanBuilder asChildOf(Span parent) {
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
    if (this.verbosity < 4) {
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
    if (this.verbosity < 3) {
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
    if (this.verbosity < 3) {
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
    if (this.verbosity < 1) {
      return;
    }
    if (this.verbosity == 1 && this.visibleErrorCount > 0) {
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
