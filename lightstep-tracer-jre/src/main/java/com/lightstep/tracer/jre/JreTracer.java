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
    }

    // Flush any data stored in the log and span buffers
    public void flush() {
        sendReport();
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
