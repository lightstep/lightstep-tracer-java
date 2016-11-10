package com.lightstep.tracer.jre;

import com.lightstep.tracer.shared.AbstractTracer;
import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.shared.SimpleFuture;
import com.lightstep.tracer.shared.Version;

import java.util.HashMap;

public class JRETracer extends AbstractTracer {

    private static final int DEFAULT_REPORTING_INTERVAL_MILLIS = 2500;

    private static class JavaTracerHolder {
        private static final JRETracer INSTANCE = new JRETracer(null);
    }

    /**
     * Returns the singleton Tracer instance that can be utilized to record logs and spans.
     *
     * @return tracer instance
     */
    public static JRETracer getInstance() {
        return JavaTracerHolder.INSTANCE;
    }

    public JRETracer(Options options) {
        super(AbstractTracer.setDefaultReportingIntervalMillis(options, DEFAULT_REPORTING_INTERVAL_MILLIS));
        this.addStandardTracerTags();
    }

    // Flush any data stored in the log and span buffers
    protected SimpleFuture<Boolean> flushInternal(boolean explicitRequest) {
        return new SimpleFuture<Boolean>(sendReport(explicitRequest));
    }

    protected void printLogToConsole(InternalLogLevel level, String msg, Object payload) {
        String s;
        switch (level) {
            case DEBUG:
                s = "[Lightstep:DEBUG] ";
                break;
            case INFO:
                s = "[Lightstep:INFO] ";
                break;
            case WARN:
                s = "[Lightstep:WARN] ";
                break;
            case ERROR:
                s = "[Lightstep:ERROR] ";
                break;
            default:
                s = "[Lightstep:???] ";
                break;
        }
        s += msg;
        if (payload != null) {
            s += " " + payload.toString();
        }
        System.err.println(s);
    }

    protected HashMap<String, String> retrieveDeviceInfo() {
        // TODO: Implement for Java Desktop Applications
        return null;
    }

    /**
     * Adds standard tags set by all LightStep client libraries.
     */
    protected void addStandardTracerTags() {
        // The platform is called "jre" rather than "Java" to clearly
        // differentiate this library from the Android version
        this.addTracerTag(LIGHTSTEP_TRACER_PLATFORM_KEY, "jre");
        this.addTracerTag(LIGHTSTEP_TRACER_PLATFORM_VERSION_KEY, System.getProperty("java.version"));
        this.addTracerTag(LIGHTSTEP_TRACER_VERSION_KEY, Version.LIGHTSTEP_TRACER_VERSION);
    }
}
