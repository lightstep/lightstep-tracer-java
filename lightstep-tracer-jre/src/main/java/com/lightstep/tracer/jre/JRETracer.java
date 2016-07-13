package com.lightstep.tracer.jre;

import java.util.HashMap;
import java.util.Map;

import com.lightstep.tracer.shared.*;
import com.lightstep.tracer.thrift.KeyValue;

public class JRETracer extends AbstractTracer {

    private static class JavaTracerHolder {
        private static final JRETracer INSTANCE = new JRETracer(null);
    }

    /**
     * Returns the singleton Tracer instance that can be utilized to record logs and spans.
     * @return  tracer instance
     */
    public static JRETracer getInstance() {
        return JavaTracerHolder.INSTANCE;
    }

    public JRETracer(Options options) {
        super(options);
        this.addStandardTracerTags();
        this.addShutdownHook();
    }

    // Flush any data stored in the log and span buffers
    public void flush() {
        sendReport(true);
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

    protected void addShutdownHook() {
        final JRETracer self = this;
        try {
            Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    public void run() {
                        self.sendReport(true);
                    }
                }
            );
        } catch (Throwable t) {
        }
    }

    /**
     * Internal class used primarily for unit testing and debugging. This is not
     * part of the OpenTracing API and is not a supported API.
     */
    public class Status {
        public Map<String, String> tags;

        public Status() {
            this.tags = new HashMap<String, String>();
        }
    }

    /**
     * Internal method used primarily for unit testing and debugging. This is not
     * part of the OpenTracing API and is not a supported API.
     */
    public Status status() {
        Status status = new Status();

        for (KeyValue pair : this.runtime.getAttrs()) {
            status.tags.put(pair.getKey(), pair.getValue());
        }

        return status;
    }
}
