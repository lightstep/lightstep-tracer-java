package com.lightstep.tracer.shared;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

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
import com.lightstep.tracer.thrift.TraceJoinId;
import com.lightstep.tracer.shared.Span;

import io.opentracing.Tracer;

public abstract class AbstractTracer implements Tracer {
  // Delay before sending the initial report
  private static final long REPORTING_DELAY_MILLIS = 0;
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
  public static final String COMPONENT_NAME_KEY = "lightstep.component_name";
  public static final String GUID_KEY = "lightstep.guid";

  /**
   * The tag key used to define traces which are joined based on a GUID.
   */
	public static final String TRACE_GUID_KEY = "join:trace_guid";
  /**
   * The tag key used to record the relationship between child and parent
   * spans.
   */
	public static final String PARENT_SPAN_GUID_KEY = "parent_span_guid";

  // copied from options
  private final int maxBufferedSpans;
  protected final int verbosity;

  private final Auth auth;
  protected final Runtime runtime;
  private URL collectorURL;

  protected ArrayList<SpanRecord> spans;
  protected ClockState clockState;

  // Should *NOT* attempt to take a span's lock while holding this lock.
  protected final Object mutex = new Object();
  private final Timer timer;
  private boolean reportInProgress;
  protected boolean isDisabled;

  protected TTransport transport;
  protected ReportingService.Client client;

  public AbstractTracer(Options options) {
    this.timer = new Timer(true);

    // TODO sanity check options
    this.maxBufferedSpans = options.maxBufferedSpans > 0 ?
      options.maxBufferedSpans : DEFAULT_MAX_BUFFERED_SPANS;
    this.spans = new ArrayList<SpanRecord>(maxBufferedSpans);

    this.clockState = new ClockState();
    this.verbosity = options.verbosity;

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
        }
      }
    }
    if (options.tags.get(GUID_KEY) == null) {
      String guid = generateGUID();
      options.tags.put(GUID_KEY, guid);
    }

    this.runtime = new Runtime();
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
      // TODO log this
      // Preemptively disable this tracer.
      this.disable();
      return;
    }

    // Automatic cleanup upon program exit.  Note that (according to the docs)
    // this doesn't do anything on Android.
    java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          shutdown();
        }
      });

    // Schedule a fixed-delay task.
    long interval = options.maxReportingIntervalSeconds > 0 ?
      options.maxReportingIntervalSeconds * 1000 : DEFAULT_REPORTING_INTERVAL_MILLIS;
    timer.schedule(new Flush(), REPORTING_DELAY_MILLIS, interval);
  }

  public String getAccessToken() {
    synchronized (this.mutex) {
      return auth.getAccess_token();
    }
  }

  /**
   * Gracefully stops the tracer.
   */
  public void shutdown() {
    if (isDisabled) {
      return;
    }

    flush();
    disable();
  }

  /**
   * Disable the tracer, stopping any further reports and turning all
   * subsequent method invocations into no-ops.
   */
  public void disable() {
    synchronized (this.mutex) {
      this.timer.cancel();
      this.timer.purge();

      if (this.transport != null) {
        this.transport.close();
        this.transport = null;
      }

      this.isDisabled = true;
      this.spans = null;
    }
  }

  public Tracer.SpanBuilder buildSpan(String operationName) {
    return this.new SpanBuilder(operationName);
  }

  public <T> void inject(io.opentracing.Span span, T carrier) {
    // TODO implement
    throw new RuntimeException("inject: unimplemented");
  }

  public <T> Tracer.SpanBuilder join(T carrier) {
    // TODO implement
    throw new RuntimeException("join: unimplemented");
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
   * Extends TimerTask to call flush().
   */
  class Flush extends TimerTask {
    @Override
    public void run() {
      AbstractTracer.this.flush();
    }
  }

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
    ArrayList<SpanRecord> spans;
    synchronized (this.mutex) {
      if (this.reportInProgress) {
        return;
      }
      if (this.spans.size() == 0 && this.clockState.isReady()) {
        return;
      }

      // Make sure other threads don't try to start sending a report.
      this.reportInProgress = true;

      if (this.clockState.isReady() || explicitRequest) {
        // Copy the reference to the spans...
        spans = this.spans;
        // ... and make a new array for other spans.
        this.spans = new ArrayList<SpanRecord>(this.maxBufferedSpans);
      } else {
        // Otherwise, if the clock state is not ready, we'll send an empty
        // report.
        spans = new ArrayList<SpanRecord>();
      }
    }

    if (this.transport == null) {
      try {
        // TODO add support for cookies (for load balancer sessions)
        this.transport = new THttpClient(this.collectorURL.toString());
        this.transport.open();
        TBinaryProtocol protocol = new TBinaryProtocol(this.transport);
        this.client = new ReportingService.Client(protocol);
      } catch (TException e) {
        // TODO log this exception
        this.disable();
        return;
      }
    }

    ReportRequest req = new ReportRequest();
    req.setRuntime(this.runtime);
    req.setSpan_records(spans);
    req.setTimestamp_offset_micros(this.clockState.offsetMicros());

    try {
      long originMicros = System.currentTimeMillis() * 1000;
      ReportResponse resp = this.client.Report(this.auth, req);
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
            disable();
          }
        }
      }
    } catch (TApplicationException e) {
      // TODO log something as this probably indicates malformed spans
      //System.err.println("Received error from collector: " + e.toString());
    } catch (TException x) {
      // Return spans to the buffer.
      for (SpanRecord span : req.span_records) {
        // TODO should probably prepend and do it in bulk
        addSpan(span);
      }
    }

    synchronized (this.mutex) {
      this.reportInProgress = false;
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
        // TODO is deleting a random element the right thing to do?
        int deleteIndex = ThreadLocalRandom.current().nextInt(0, this.spans.size());
        this.spans.set(deleteIndex, span);
        // TODO increment counter for dropped spans
      } else {
        this.spans.add(span);
      }
    }
  }

  private static final String generateGUID() {
    // Note that ThreadLocalRandom is a singleton, thread safe Random Generator
    long guid = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
    return Long.toHexString(guid);
  }

  protected void addTracerTag(String key, String value) {
    this.runtime.addToAttrs(new KeyValue(key, value));
  }

  class SpanBuilder implements Tracer.SpanBuilder{
    private String operationName;
    private io.opentracing.Span parent;
    private Map<String, String> tags;
    private long startTimestampMicros;

    SpanBuilder(String operationName) {
      this.operationName = operationName;
      this.tags = new HashMap<String, String>();
    }

    public Tracer.SpanBuilder withOperationName(String operationName) {
      this.operationName = operationName;
      return this;
    }

    public Tracer.SpanBuilder withParent(io.opentracing.Span parent) {
      this.parent = parent;
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
      record.setSpan_guid(generateGUID());

      String traceID;
      if (this.parent instanceof io.opentracing.Span) {
        Span parentSpan = (Span)parent;
        traceID = parentSpan.getTraceID();
        record.addToAttributes(new KeyValue(PARENT_SPAN_GUID_KEY, parentSpan.getGUID()));
      } else {
        traceID = generateGUID();
      }
      record.addToJoin_ids(new TraceJoinId(TRACE_GUID_KEY, traceID));

      Span span = new Span(AbstractTracer.this, record, traceID);
      for (Map.Entry<String, String> pair : this.tags.entrySet()) {
           span.setTag(pair.getKey(), pair.getValue());
      }
      return span;
    }

    public io.opentracing.Span start(long microseconds) {
      return this.withStartTimestamp(microseconds).start();
    }
  }
}
