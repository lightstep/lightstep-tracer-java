package com.lightstep.tracer.shared;

import org.junit.Test;

import static com.lightstep.tracer.shared.Options.VERBOSITY_DEBUG;
import static com.lightstep.tracer.shared.Options.VERBOSITY_ERRORS_ONLY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_FIRST_ERROR_ONLY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_INFO;
import static com.lightstep.tracer.shared.Options.VERBOSITY_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractTracerTest {

    private static final String ACCESS_TOKEN = "abc123";
    private static final String TEST_MSG = "hello tracer";

    /**
     * Provides an implementation of AbstractTracer (for use in testing) that is configured to
     * the provided verbosity level.
     */
    private StubTracer createTracer(int verbosity) {
        Options options = new Options(ACCESS_TOKEN);
        options.withVerbosity(verbosity);
        return createTracer(options);
    }

    /**
     * Provides an implementation of AbstractTracer (for use in testing) that is configured with
     * the provided Options.
     */
    private StubTracer createTracer(Options options) {
        return new StubTracer(options);
    }

    @Test
    public void testVerbosityDebug() {
        StubTracer undertest = createTracer(VERBOSITY_DEBUG);
        callAllLogMethods(undertest);

        // At debug level, all of the calls should be logged
        assertEquals(4, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityInfo() {
        StubTracer undertest = createTracer(VERBOSITY_INFO);
        callAllLogMethods(undertest);

        // At info level, all of the calls should be logged, except the debug log
        assertEquals(3, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityErrorsOnly() {
        StubTracer undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        callAllLogMethods(undertest);
        undertest.error(TEST_MSG); // make an extra call to error, all error calls should be logged

        // At error level, all of the error calls should be logged
        assertEquals(2, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityNone() {
        StubTracer undertest = createTracer(VERBOSITY_NONE);
        callAllLogMethods(undertest);

        // At none, nothing should be logged
        assertTrue(undertest.consoleLogCallsIsEmpty());
    }

    @Test
    public void testVerbosityFirstErrorOnly() {
        StubTracer undertest = createTracer(VERBOSITY_FIRST_ERROR_ONLY);
        callAllLogMethods(undertest);
        undertest.error(TEST_MSG); // make a second call to error

        // At error level, only the first error call should be logged
        assertEquals(1, undertest.getNumberOfConsoleLogCalls());
    }

    /**
     * Calls each of the available logging methods on the tracer with a dummy message.
     */
    private void callAllLogMethods(StubTracer undertest) {
        undertest.debug(TEST_MSG);
        undertest.info(TEST_MSG);
        undertest.warn(TEST_MSG);
        undertest.error(TEST_MSG);
    }
}