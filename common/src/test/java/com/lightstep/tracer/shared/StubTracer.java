package com.lightstep.tracer.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of AbstractTracer for use in unit testing.
 */
class StubTracer extends AbstractTracer {
    private final List<LogCall> consoleLogCalls;

    StubTracer(Options options) {
        super(options);
        consoleLogCalls = new ArrayList<>();
    }

    @Override
    protected SimpleFuture<Boolean> flushInternal(boolean explicitRequest) {
        return null;
    }

    @Override
    protected void printLogToConsole(InternalLogLevel level, String msg, Object payload) {
        // this can only be null when print is called during Tracer construction
        if (consoleLogCalls != null) {
            consoleLogCalls.add(new LogCall(level, msg, payload));
        }
    }

    boolean consoleLogCallsIsEmpty() {
        return consoleLogCalls.isEmpty();
    }

    void resetConsoleLogCalls() {
        consoleLogCalls.clear();
    }

    int getNumberOfConsoleLogCalls() {
        if (consoleLogCallsIsEmpty()) {
            return 0;
        }
        return consoleLogCalls.size();
    }

    class LogCall {
        final InternalLogLevel level;
        final String msg;
        final Object payload;

        private LogCall(InternalLogLevel level, String msg, Object payload)  {
            this.level = level;
            this.msg = msg;
            this.payload = payload;
        }
    }
}