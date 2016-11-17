package com.lightstep.tracer.shared;

import org.junit.Test;

import static com.lightstep.tracer.shared.Options.VERBOSITY_DEBUG;
import static com.lightstep.tracer.shared.Options.VERBOSITY_ERRORS_ONLY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_FIRST_ERROR_ONLY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_INFO;
import static com.lightstep.tracer.shared.Options.VERBOSITY_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractTracerTest {

    private static final String ACCESS_TOKEN = "abc123";
    private static final String TEST_MSG = "hello tracer";

    /**
     * Provides an implementation of AbstractTracer (for use in testing) that is configured to
     * the provided verbosity level.
     */
    private StubTracer createTracer(int verbosity) throws Exception {
        Options options = new Options.OptionsBuilder()
                .withAccessToken(ACCESS_TOKEN)
                .withVerbosity(verbosity)
                .build();
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
    public void testVerbosityDebug() throws Exception {
        StubTracer undertest = createTracer(VERBOSITY_DEBUG);
        callAllLogMethods(undertest);

        // At debug level, all of the calls should be logged
        assertEquals(4, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityInfo() throws Exception {
        StubTracer undertest = createTracer(VERBOSITY_INFO);
        callAllLogMethods(undertest);

        // At info level, all of the calls should be logged, except the debug log
        assertEquals(3, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityErrorsOnly() throws Exception {
        StubTracer undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        callAllLogMethods(undertest);
        undertest.error(TEST_MSG); // make an extra call to error, all error calls should be logged

        // At error level, all of the error calls should be logged
        assertEquals(2, undertest.getNumberOfConsoleLogCalls());
    }

    @Test
    public void testVerbosityNone() throws Exception {
        StubTracer undertest = createTracer(VERBOSITY_NONE);
        callAllLogMethods(undertest);

        // At none, nothing should be logged
        assertTrue(undertest.consoleLogCallsIsEmpty());
    }

    @Test
    public void testVerbosityFirstErrorOnly() throws Exception {
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

    @Test
    public void testFlush_timeoutOccurs() throws Exception {
        StubTracer undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.flushResult = new SimpleFuture<>();
        assertNull(undertest.flush(1L));
    }

    @Test
    public void testFlush_noTimeoutSuccess() throws Exception {
        StubTracer undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.flushResult = new SimpleFuture<>(true);
        assertTrue(undertest.flush(20000L));
    }

    @Test
    public void testFlush_noTimeoutFailure() throws Exception {
        StubTracer undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        undertest.flushResult = new SimpleFuture<>(false);
        assertFalse(undertest.flush(20000L));
    }

    @Test
    public void testGenerateTraceURL() throws Exception {
        String spanId = "span789";
        StubTracer undertest = createTracer(VERBOSITY_ERRORS_ONLY);
        String result = undertest.generateTraceURL(spanId);
        String expectedUrlStart = "https://app.lightstep.com/" + ACCESS_TOKEN + "/trace?span_guid="
                + spanId + "&at_micros=";
        assertTrue("Unexpected trace url: " + result, result.startsWith(expectedUrlStart));
    }
}