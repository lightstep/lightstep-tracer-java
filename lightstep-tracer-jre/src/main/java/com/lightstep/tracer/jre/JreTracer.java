package com.lightstep.tracer.jre;

import com.lightstep.tracer.shared.*;
import java.util.HashMap;

public class JreTracer extends AbstractTracer {

    private static class JavaTracerHolder {
        private static final JreTracer INSTANCE = new JreTracer(null);
    }

    /**
     * Returns the singleton Tracer instance that can be utilized to record logs and spans.
     * @return  tracer instance
     */
    public static JreTracer getInstance() {
        return JavaTracerHolder.INSTANCE;
    }

    public JreTracer(Options options) {
        super(options);
    }

    // Flush any data stored in the log and span buffers
    public void flush() {
        /*if (disabledTracer) return;

        if (initializedTracer) {
            if (debugReporter != null) {
                debugFlush();
            } else {
                Connection connection = new Connection(this.serviceUrl);
                connection.openConnection();
                flushWorker(connection);
                connection.closeConnection();
            }
        }*/
    }

    protected HashMap<String, String> retrieveDeviceInfo() {
        // TODO: Implement for Java Desktop Applications
        return null;
    }
}