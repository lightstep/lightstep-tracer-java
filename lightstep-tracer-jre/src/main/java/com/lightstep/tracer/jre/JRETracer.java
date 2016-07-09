package com.lightstep.tracer.jre;

import com.lightstep.tracer.shared.*;
import java.util.HashMap;

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
}
