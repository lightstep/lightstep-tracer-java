package com.lightstep.tracer.jre;

import com.lightstep.tracer.shared.AbstractTracer;
import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.shared.SimpleFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.lightstep.tracer.shared.Version.LIGHTSTEP_TRACER_VERSION;

public class JRETracer extends AbstractTracer {

    private static final int JRE_DEFAULT_REPORTING_INTERVAL_MILLIS = 2500;

    private final Logger logger = LoggerFactory.getLogger(JRETracer.class);

    private static class JavaTracerHolder {
        private static final JRETracer INSTANCE = new JRETracer(null);
    }

    /**
     * Returns the singleton Tracer instance that can be utilized to record logs and spans.
     *
     * @return tracer instance
     */
    @SuppressWarnings("unused")
    public static JRETracer getInstance() {
        return JavaTracerHolder.INSTANCE;
    }

    public JRETracer(Options options) {
        super(options.setDefaultReportingIntervalMillis(JRE_DEFAULT_REPORTING_INTERVAL_MILLIS));
        addStandardTracerTags();
    }

    // Flush any data stored in the log and span buffers
    protected SimpleFuture<Boolean> flushInternal(boolean explicitRequest) {
        return new SimpleFuture<>(sendReport(explicitRequest));
    }

    protected void printLogToConsole(InternalLogLevel level, String msg, Object payload) {
        String s = msg;
        if (payload != null) {
            s += " " + payload.toString();
        }
        switch (level) {
            case DEBUG:
                logger.debug(s);
                break;
            case INFO:
                logger.info(s);
                break;
            case WARN:
                logger.warn(s);
                break;
            case ERROR:
                logger.error(s);
                break;
        }
    }

    /**
     * Adds standard tags set by all LightStep client libraries.
     */
    private void addStandardTracerTags() {
        // The platform is called "jre" rather than "Java" to clearly
        // differentiate this library from the Android version
        addTracerTag(LIGHTSTEP_TRACER_PLATFORM_KEY, "jre");
        addTracerTag(LIGHTSTEP_TRACER_PLATFORM_VERSION_KEY, System.getProperty("java.version"));
        addTracerTag(LIGHTSTEP_TRACER_VERSION_KEY, LIGHTSTEP_TRACER_VERSION);
    }
}
