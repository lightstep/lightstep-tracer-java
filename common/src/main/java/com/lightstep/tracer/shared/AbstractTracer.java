package com.lightstep.tracer.shared;

import com.lightstep.tracer.thrift.Auth;
import com.lightstep.tracer.thrift.Command;
import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.ReportRequest;
import com.lightstep.tracer.thrift.ReportResponse;
import com.lightstep.tracer.thrift.ReportingService;
import com.lightstep.tracer.thrift.Runtime;
import com.lightstep.tracer.thrift.SpanRecord;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import static com.lightstep.tracer.shared.AbstractTracer.InternalLogLevel.DEBUG;
import static com.lightstep.tracer.shared.AbstractTracer.InternalLogLevel.ERROR;
import static com.lightstep.tracer.shared.Options.VERBOSITY_DEBUG;
import static com.lightstep.tracer.shared.Options.VERBOSITY_FIRST_ERROR_ONLY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_INFO;

public abstract class AbstractTracer implements Tracer {
    public static final int DEFAULT_MAX_BUFFERED_SPANS = 1000;

    // Maximum interval between reports
    private static final long DEFAULT_CLOCK_STATE_INTERVAL_MILLIS = 500;
    private static final int DEFAULT_REPORT_TIMEOUT_MILLIS = 10 * 1000;

    protected static final String LIGHTSTEP_TRACER_PLATFORM_KEY = "lightstep.tracer_platform";
    protected static final String LIGHTSTEP_TRACER_PLATFORM_VERSION_KEY = "lightstep.tracer_platform_version";
    protected static final String LIGHTSTEP_TRACER_VERSION_KEY = "lightstep.tracer_version";

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

    private final int verbosity;
    private final Auth auth;
    private final Runtime runtime;

    /**
     * False, until the first error has been logged, after which it is true, and if verbosity says
     * not to log more than one error, no more errors will be logged.
     */
    private boolean firstErrorLogged = false;
    private URL collectorURL;

    // Timestamp of the last recorded span. Used to terminate the reporting
    // loop thread if no new data has come in (which is necessary for clean
    // shutdown).
    private final AtomicLong lastNewSpanMillis;
    private ArrayList<SpanRecord> spans;
    private final ClockState clockState;
    private ClientMetrics clientMetrics;

    // Should *NOT* attempt to take a span's lock while holding this lock.
    protected final Object mutex = new Object();
    private boolean reportInProgress;

    // This is set to non-null if background reporting is enabled.
    private ReportingLoop reportingLoop;

    // This is set to non-null when a background Thread is actually reporting.
    private Thread reportingThread;

    private boolean isDisabled;

    private TTransport transport;
    private ReportingService.Client client;

    public AbstractTracer(Options options) {
        // Set verbosity first so debug logs from the constructor take effect
        verbosity = options.verbosity;

        // TODO sanity check options
        lastNewSpanMillis = new AtomicLong(System.currentTimeMillis());
        spans = new ArrayList<>(DEFAULT_MAX_BUFFERED_SPANS);

        clockState = new ClockState();
        clientMetrics = new ClientMetrics();

        auth = new Auth();
        auth.setAccess_token(options.accessToken);

        runtime = new Runtime();
        runtime.setGuid(options.getGuid());

        // Unfortunately Java7 has no way to generate a timestamp that's both
        // precise (a la System.nanoTime()) and absolute (a la
        // System.currentTimeMillis()). We store an absolute start timestamp but at
        // least get a precise duration at Span.finish() time via
        // startTimestampRelativeNanos (search for it below).
        runtime.setStart_micros(nowMicrosApproximate());

        for (Map.Entry<String, Object> entry : options.tags.entrySet()) {
            addTracerTag(entry.getKey(), entry.getValue().toString());
        }

        collectorURL = options.collectorUrl;

        if (!options.disableReportingLoop) {
            reportingLoop = new ReportingLoop(options.maxReportingIntervalMillis);
        }
    }

    /**
     * This call is NOT synchronized
     */
    private void doStopReporting() {
        synchronized (this) {
            // Note: There is no synchronization to prevent multiple
            // reporting loops from running simultaneously.  It's possible
            // for one to start before another one exits, which is safe
            // because flushInternal() is itself synchronized.
            if (reportingThread == null) {
                return;
            }
            reportingThread.interrupt();
            reportingThread = null;
        }
    }

    /**
     * This call is synchronized
     */
    private void maybeStartReporting() {
        if (reportingThread != null) {
            return;
        }
        reportingThread = new Thread(reportingLoop);
        reportingThread.start();
    }

    /**
     * Runs a relatively frequent loop in a separate thread to check if the
     * library should flush its current buffer or if the loop should stop.
     *
     * In the JRE case, the actual flush will be run in this thread. In the
     * case of Android, this thread will block and wait until the Android
     * AsyncTask finishes.
     */
    private class ReportingLoop implements Runnable {
        // Controls how often the reporting loop itself checks if the status.
        private static final int POLL_INTERVAL_MILLIS = 40;

        private static final int THREAD_TIMEOUT_MILLIS = 2000;

        private Random rng = new Random(System.currentTimeMillis());
        private long reportingIntervalMillis = 0;
        private int consecutiveFailures = 0;

        ReportingLoop(long interval) {
            reportingIntervalMillis = interval;
        }

        @Override
        public void run() {
            debug("Reporting thread started");
            long nextReportMillis = computeNextReportMillis();

            // Run until the reporting loop has been explicitly told to stop.
            while (!Thread.interrupted()) {
                // Check if it's time to attempt the next report. At this point, the
                // report may not actually result in network traffic if the there's
                // no new data to report or, for example, the Android device does
                // not have a wireless connection.
                long nowMillis = System.currentTimeMillis();
                if (nowMillis >= nextReportMillis) {
                    SimpleFuture<Boolean> result = flushInternal(false);
                    boolean reportSucceeded = false;
                    try {
                        reportSucceeded = result.get();
                    } catch (InterruptedException e) {
                        warn("Future timed out");
                    }

                    // Check consecutive failures for back off purposes
                    if (!reportSucceeded) {
                        consecutiveFailures++;
                    } else {
                        consecutiveFailures = 0;
                    }
                    nextReportMillis = computeNextReportMillis();
                }

                // If the tracer hasn't received new data in a while, stop the
                // reporting loop. It will be restarted when the next span is finished.
                boolean hasUnreportedSpans = (unreportedSpanCount() > 0);
                long lastSpanAgeMillis = System.currentTimeMillis() - lastNewSpanMillis.get();
                if ((!hasUnreportedSpans || consecutiveFailures >= 2) &&
                        lastSpanAgeMillis > THREAD_TIMEOUT_MILLIS) {
                    doStopReporting();
                } else {
                    try {
                        Thread.sleep(POLL_INTERVAL_MILLIS);
                    } catch (InterruptedException e) {
                        warn("Exception trying to sleep in reporting thread");
                    }
                }
            }
            debug("Reporting thread stopped");
        }

        /**
         * Compute the next time, as compared to System.currentTimeMillis(), that
         * a report should be attempted.  Accounts for clock state, error back off,
         * and random jitter.
         */
        long computeNextReportMillis() {
            double base;
            if (!clockState.isReady()) {
                base = (double) DEFAULT_CLOCK_STATE_INTERVAL_MILLIS;
            } else {
                base = (double) reportingIntervalMillis;
            }

            // Exponential back off based on number of consecutive errors, up to 8x the normal
            // interval
            int backOff = 1 + Math.min(7, consecutiveFailures);
            base *= (double) backOff;

            // Add +/- 10% jitter to the regular reporting interval
            final double delta = base * (0.9 + 0.2 * rng.nextDouble());
            final long nextMillis = System.currentTimeMillis() + (long) Math.ceil(delta);
            debug(String.format("Next report: %d (%f) [%d]", nextMillis, delta, clockState.activeSampleCount()));
            return nextMillis;
        }
    }

    /**
     * Disable the tracer, stopping any further reports and turning all
     * subsequent method invocations into no-ops.
     */
    private void disable() {
        info("Disabling client library");
        doStopReporting();

        synchronized (mutex) {
            if (transport != null) {
                transport.close();
                transport = null;
            }

            isDisabled = true;

            // The code makes various assumptions about this field never being
            // null, so replace it with an empty list rather than nulling it out.
            spans = new ArrayList<>(0);
        }
    }

    public boolean isDisabled() {
        synchronized (mutex) {
            return isDisabled;
        }
    }

    public Tracer.SpanBuilder buildSpan(String operationName) {
        return new com.lightstep.tracer.shared.SpanBuilder(operationName, this);
    }

    public <C> void inject(io.opentracing.SpanContext spanContext, Format<C> format, C carrier) {
        if ( !(spanContext instanceof SpanContext) ) {
            error("Unsupported SpanContext implementation: " + spanContext.getClass());
            return;
        }
        SpanContext lightstepSpanContext = (SpanContext) spanContext;
        if (format == Format.Builtin.TEXT_MAP) {
            Propagator.TEXT_MAP.inject(lightstepSpanContext, (TextMap) carrier);
        } else if (format == Format.Builtin.HTTP_HEADERS) {
            Propagator.HTTP_HEADERS.inject(lightstepSpanContext, (TextMap) carrier);
        } else if (format == Format.Builtin.BINARY) {
            warn("LightStep-java does not yet support binary carriers. " +
                    "SpanContext: " + spanContext.toString());
            Propagator.BINARY.inject(lightstepSpanContext, (ByteBuffer) carrier);
        } else {
            info("Unsupported carrier type: " + carrier.getClass());
        }
    }

    public <C> io.opentracing.SpanContext extract(Format<C> format, C carrier) {
        if (format == Format.Builtin.TEXT_MAP) {
            return Propagator.TEXT_MAP.extract((TextMap) carrier);
        } else if (format == Format.Builtin.HTTP_HEADERS) {
            return Propagator.HTTP_HEADERS.extract((TextMap) carrier);
        } else if (format == Format.Builtin.BINARY) {
            warn("LightStep-java does not yet support binary carriers.");
            return Propagator.BINARY.extract((ByteBuffer) carrier);
        } else {
            info("Unsupported carrier type: " + carrier.getClass());
            return null;
        }
    }

    /**
     * Initiates a flush of data to the collectors. Method does not return until the flush is
     * complete, or has timed out.
     *
     * @param timeoutMillis The amount of time, in milliseconds, to allow for the flush to complete
     * @return True if the flush completed within the time allotted, false otherwise.
     */
    public Boolean flush(long timeoutMillis) {
        SimpleFuture<Boolean> flushFuture = flushInternal(true);
        try {
            return flushFuture.getWithTimeout(timeoutMillis);
        } catch (InterruptedException e) {
            return false;
        }
    }

    protected abstract SimpleFuture<Boolean> flushInternal(boolean explicitRequest);

    /**
     * Does the work of a flush by sending spans to the collector.
     *
     * @param explicitRequest if true, the report request was made explicitly rather than implicitly
     *                        (via a reporting loop) and therefore the code should make a 'best
     *                        effort' to truly report (i.e. send even if the clock state is not
     *                        ready).
     * @return true if the report was sent successfully
     */
    protected boolean sendReport(boolean explicitRequest) {

        synchronized (mutex) {
            if (reportInProgress) {
                debug("Report in progress. Skipping.");
                return true;
            }
            if (spans.size() == 0 && clockState.isReady()) {
                debug("Skipping report. No new data.");
                return true;
            }

            // Make sure other threads don't try to start sending a report.
            reportInProgress = true;
        }

        try {
            return sendReportWorker(explicitRequest);
        } finally {
            synchronized (mutex) {
                reportInProgress = false;
            }
        }
    }

    /**
     * Returns the number of currently unreported (buffered) spans.
     *
     * Note: this method acquires the mutex. In Java synchronized locks are reentrant, but if the
     * lock is already acquired, calling spans.size() directly should suffice.
     */
    private int unreportedSpanCount() {
        synchronized (mutex) {
            return spans.size();
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

        synchronized (mutex) {
            if (clockState.isReady() || explicitRequest) {
                // Copy the reference to the spans and make a new array for other spans.
                spans = this.spans;
                clientMetrics = this.clientMetrics;
                this.spans = new ArrayList<>(DEFAULT_MAX_BUFFERED_SPANS);
                this.clientMetrics = new ClientMetrics();
                debug(String.format("Sending report, %d spans", spans.size()));
            } else {
                // Otherwise, if the clock state is not ready, we'll send an empty
                // report.
                debug("Sending empty report to prime clock state");
                spans = new ArrayList<>();
            }

            if (transport == null) {
                debug("Creating transport");
                try {
                    // TODO add support for cookies (for load balancer sessions)
                    THttpClient tHttpClient = new THttpClient(collectorURL.toString());
                    tHttpClient.setConnectTimeout(DEFAULT_REPORT_TIMEOUT_MILLIS);
                    transport = tHttpClient;
                    transport.open();
                    TBinaryProtocol protocol = new TBinaryProtocol(transport);
                    client = new ReportingService.Client(protocol);
                } catch (TException e) {
                    error("Exception creating Thrift client. Disabling tracer.", e);
                    disable();
                    return false;
                }
            }
        }

        ReportRequest req = new ReportRequest();
        req.setRuntime(runtime);
        req.setSpan_records(spans);
        req.setTimestamp_offset_micros(clockState.offsetMicros());

        if (clientMetrics != null) {
            req.setInternal_metrics(clientMetrics.toThrift());
        }

        try {
            long originMicros = nowMicrosApproximate();
            long originRelativeNanos = System.nanoTime();
            ReportResponse resp = client.Report(auth, req);

            if (resp.isSetTiming()) {
                long deltaMicros = (System.nanoTime() - originRelativeNanos) / 1000;
                long destinationMicros = originMicros + deltaMicros;
                clockState.addSample(originMicros,
                        resp.getTiming().getReceive_micros(),
                        resp.getTiming().getTransmit_micros(),
                        destinationMicros);
            } else {
                warn("Collector response did not include timing info");
            }

            // Check whether or not to disable the tracer
            if (resp.isSetCommands()) {
                for (Command command : resp.commands) {
                    if (command.disable) {
                        disable();
                    }
                }
            }

            debug(String.format("Report sent successfully (%d spans)", spans.size()));
            return true;

        } catch (TApplicationException e) {
            // Log as this probably indicates malformed spans
            error("TApplicationException: error from collector", e);
            return false;
        } catch (TException x) {
            // This may include exceptions like connection timeouts, which are expected during
            // normal operation.
            debug("Report failed with exception", x);

            // The request failed, add any data that was supposed to be sent back to the
            // client local buffers.
            synchronized (mutex) {
                this.clientMetrics.merge(clientMetrics);
            }

            // TODO should probably prepend and do it in bulk
            for (SpanRecord span : req.span_records) {
                addSpan(span);
            }
            return false;
        }
    }

    /**
     * Adds a span to the buffer.
     *
     * @param span the span to be added
     */
    void addSpan(SpanRecord span) {
        lastNewSpanMillis.set(System.currentTimeMillis());

        synchronized (mutex) {
            if (spans.size() >= DEFAULT_MAX_BUFFERED_SPANS) {
                clientMetrics.spansDropped++;
            } else {
                spans.add(span);
            }

            maybeStartReporting();
        }
    }

    protected void addTracerTag(String key, String value) {
        debug("Adding tracer tag: " + key + " => " + value);
        runtime.addToAttrs(new KeyValue(key, value));
    }

    /**
     * Internal logging.
     */
    protected void debug(String s) {
        debug(s, null);
    }

    /**
     * Internal logging.
     */
    protected void debug(String msg, Object payload) {
        if (verbosity < VERBOSITY_DEBUG) {
            return;
        }
        printLogToConsole(DEBUG, msg, payload);
    }

    /**
     * Internal logging.
     */
    protected void info(String s) {
        info(s, null);
    }

    /**
     * Internal logging.
     */
    protected void info(String msg, Object payload) {
        if (verbosity < VERBOSITY_INFO) {
            return;
        }
        printLogToConsole(InternalLogLevel.INFO, msg, payload);
    }

    /**
     * Internal logging.
     */
    @SuppressWarnings("WeakerAccess")
    protected void warn(String s) {
        warn(s, null);
    }

    /**
     * Internal warning.
     */
    @SuppressWarnings("WeakerAccess")
    protected void warn(String msg, Object payload) {
        if (verbosity < VERBOSITY_INFO) {
            return;
        }
        printLogToConsole(InternalLogLevel.WARN, msg, payload);
    }

    /**
     * Internal logging.
     */
    protected void error(String s) {
        error(s, null);
    }

    /**
     * Internal error.
     */
    protected void error(String msg, Object payload) {
        if (verbosity < VERBOSITY_FIRST_ERROR_ONLY) {
            return;
        }
        if (verbosity == VERBOSITY_FIRST_ERROR_ONLY && firstErrorLogged) {
            return;
        }
        firstErrorLogged = true;
        printLogToConsole(ERROR, msg, payload);
    }

    protected abstract void printLogToConsole(InternalLogLevel level, String msg, Object payload);

    static long nowMicrosApproximate() {
        return System.currentTimeMillis() * 1000;
    }

    String generateTraceURL(long spanId) {
        return "https://app.lightstep.com/" + auth.access_token +
                "/trace?span_guid=" + Long.toHexString(spanId) +
                "&at_micros=" + (System.currentTimeMillis() * 1000);
    }

    /**
     * Internal method used primarily for unit testing and debugging. This is not
     * part of the OpenTracing API and is not a supported API.
     *
     * Copies the internal state/status into an object that's easier to check
     * against in unit tests.
     */
    public Status status() {
        synchronized (mutex) {
            return new Status(runtime.getAttrs(), new ClientMetrics(clientMetrics));
        }
    }
}
