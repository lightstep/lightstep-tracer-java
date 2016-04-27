package io.traceguide.instrument;

import java.util.*;

import org.junit.*;
import org.hamcrest.*;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.traceguide.instrument.*;
import io.traceguide.instrument.debug.*;
import io.traceguide.instrument.shared.*;
import io.traceguide.thrift.*;

public class RuntimeTest {

    // ----------------------------------
    // INIITALIZATION & TERMINATION TESTS
    // ----------------------------------

    @Test
    // Test: flushBeforeInitialization ()
    // ----------------------------------
    // Note: Just making sure that no errors are thrown.
    public void flushBeforeInitialization() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        runtime.flush();
    }

    @Test
    // Test: closeConnectionBeforeInitialization ()
    // --------------------------------------------
    // Note: Just making sure that no errors are thrown.
    public void closeConnectionBeforeInitialization() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);
    }

    @Test
    // Test: shutdownBeforeInitialization ()
    // -------------------------------------
    // Note: Just making sure that no errors are thrown.
    // Potential error, where user creates runtime and does nothing, yet shutdown() still
    // called because of the system exit hook.
    public void shutdownBeforeNewInitialization() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        runtime.shutdown();
    }

    @Test
    // Test: shutdownBeforeSingletonInitialization ()
    // -------------------------------------
    // Note: Just making sure that no errors are thrown.
    // Potential error, where user creates runtime or retrieves runtime without
    public void shutdownBeforeSingletonInitialization() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = io.traceguide.instrument.runtime.JavaRuntime.getInstance();
        runtime.shutdown();
    }

    @Test
    // Test: shutdownTwice()
    // ---------------------
    public void shutdownTwice() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);
        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");

        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.shutdown();
        runtime.shutdown();
    }

    @Test
    // Test: createLogsBeforeRuntimeInitialization ()
    // ----------------------------------------------
    // Tests whether logs are correctly stored before runtime is initialized
    // Worfklow: Construct 2 logs pre-initialization, initialize, construct 2 logs post initialization,
    //           and then flush.
    public void createLogsBeforeRuntimeInitialization() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        runtime.log("log 1");
        runtime.log("log 2");

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.log("log 3");
        runtime.log("log 4");

        runtime.flush();

        Assert.assertTrue("Should be 0 spans", testReporter.reports.get(0).span_records.size() == 0);
        Assert.assertTrue("Should be 4 logs", testReporter.reports.get(0).log_records.size() == 4);
        checkLogs(testReporter.reports.get(0).log_records);
    }

    @Test
    // Test: createSpanBeforeRuntimeInitialization ()
    // ----------------------------------------------
    // Tests whether spans are correctly stored before runtime is initialized
    // Workflow: Construct 2 spans pre-initialization, initialize, construct 2 spans post initialization,
    //              and then flush.
    public void createSpanBeforeRuntimeInitialization() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        ActiveSpan span1 = runtime.span("Span1");
        ActiveSpan span2 = runtime.span("Span2");

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        ActiveSpan span3 = runtime.span("Span3");
        ActiveSpan span4 = runtime.span("Span4");

        span1.end();
        span2.end();
        span3.end();
        span4.end();
        runtime.flush();

        Assert.assertTrue("Should be 0 logs", testReporter.reports.get(0).log_records.size() == 0);
        Assert.assertTrue("Should be 4 spans", testReporter.reports.get(0).span_records.size() == 4);
        checkSpans(testReporter.reports.get(0).span_records);
    }

    // -------------
    // DISABLE TESTS
    // -------------

    @Test
    // Test: sendLogsWithoutPayloadsAfterDisable
    // -----------------------------------------
    public void sendLogsWithoutPayloadsAfterDisable() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        // Send 10 logs
        for (int i = 0; i < 10; i++) {
            runtime.log(Integer.toString(i));
        }
        runtime.flush();

        // Check that the 10 logs were recevied, by checking ther messages
        int i = 0;
        for (LogRecord log : testReporter.reports.get(0).log_records) {
            Assert.assertTrue("Incorrect Log detected", log.message.equals(Integer.toString(i)));
            i++;
        }

        // Delete current logs and disable the runtime
        testReporter.Clear();
        runtime.disable();

        // Send 10 logs with payloads, no report should appear
        for (int j = 0; j < 10; j++) {
            runtime.log("Log #: " + j, j);
        }
        runtime.flush();
        Assert.assertTrue("Report created", testReporter.reports.size() == 0);
    }

    @Test
    // Test: sendLogsWithPayloadsAfterDisable
    // --------------------------------------
    public void sendLogsWithPayloadsAfterDisable() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        // Send 10 logs with payloads
        for (int i = 0; i < 10; i++) {
            runtime.log("Log #: " + i, i);
        }
        runtime.flush();

        // Check that the 10 logs were recevied, by checking ther payloads
        int i = 0;
        for (LogRecord log : testReporter.reports.get(0).log_records) {
            checkPayload(Integer.toString(i), i, testReporter);
            i++;
        }

        // Delete current logs and disable the runtime
        testReporter.Clear();
        runtime.disable();

        // Send 10 logs with payloads, no report should appear
        for (int j = 0; j < 10; j++) {
            runtime.log("Log #: " + j, j);
        }
        runtime.flush();
        Assert.assertTrue("Report created", testReporter.reports.size() == 0);
    }

    // Test: sendSpansAfterDisable
    // ---------------------------
    public void sendSpansAfterDisable() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        // Send 10 spans
        for (int i = 0; i < 10; i++) {
            ActiveSpan span = runtime.span(Integer.toString(i));
            span.end();
        }
        runtime.flush();

        // Check that the 10 spans were recevied, by checking ther names
        int i = 0;
        for (SpanRecord span : testReporter.reports.get(0).span_records) {
            Assert.assertTrue("Incorrect span detected", span.span_name.equals(Integer.toString(i)));
            i++;
        }

        // Delete current spans and disable the runtime
        testReporter.Clear();
        runtime.disable();

        // Send 10 spans with payloads, no report should appear
        for (int j = 0; j < 10; j++) {
            ActiveSpan span = runtime.span(Integer.toString(j));
            span.end();
        }
        runtime.flush();
        Assert.assertTrue("Report created", testReporter.reports.size() == 0);
    }

    // Test: shutdownAfterDisable
    // --------------------------
    public void shutdownAfterDisable() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.disable();
        runtime.shutdown();
    }

    // Test: disableTwice
    // ------------------
    public void disableTwice() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.disable();
        runtime.disable();
    }

    // --------------------
    // NULL & "EMPTY" TESTS
    // --------------------

    @Test
    // Test: initializeNoHostName ()
    // -----------------------------
    // Tests whether the initialize method correctly returns false
    public void initializeNoHostName () {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();

        boolean success = runtime.initialize("", 0, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertFalse("Should not have successfully initialized the runtime", success);
    }

    @Test
    // Test: logNoLogStatement ()
    // --------------------------
    public void logNoLogStatement() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);
        runtime.log("");

        runtime.flush();
        Assert.assertTrue("Should be 1 log", testReporter.reports.get(0).log_records.size() == 1);
    }

    @Test
    // Test: spanNoSpanName ()
    // ----------------------------
    public void spanNoSpanName() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);
        ActiveSpan span = runtime.span("");
        span.end();

        runtime.flush();
        Assert.assertTrue("Should be 1 span", testReporter.reports.get(0).span_records.size() == 1);
    }

    // -------------
    // STRESS TESTS
    // -------------
    @Test
    // Test: stressLogs ()
    // -------------------
    public void stressLogs() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        for (int i = 0; i < 1000; i++) {
            runtime.log("Log #" + i);
        }

        runtime.flush();

        Assert.assertTrue("Should be 1000 logs", testReporter.reports.get(0).log_records.size() == 1000);
        checkLogs(testReporter.reports.get(0).log_records);
    }

    @Test
    // Test: stressSpans ()
    // -------------------
    public void stressSpans() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        for (int i = 0; i < 1000; i++) {
            ActiveSpan span = runtime.span("Span #" + i);
            span.end();
        }

        runtime.flush();

        Assert.assertTrue("Should be 1000 spans", testReporter.reports.get(0).span_records.size() == 1000);
        checkSpans(testReporter.reports.get(0).span_records);
    }

    // ---------
    // LOG TESTS
    // ---------
    @Test
    public void infofLog() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);
        runtime.infof("Hi there %s %s", "John", "Smith");
        runtime.flush();
        LogRecord log = testReporter.reports.get(0).log_records.get(0);
        Assert.assertTrue("Incorrect Log", log.message.equals("Hi there John Smith"));
        Assert.assertTrue("Incorrect Level", log.level.equals("I"));
    }

    @Test
    public void warnfLog() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);
        runtime.warnf("Hi there %s %s", "John", "Smith");
        runtime.flush();
        LogRecord log = testReporter.reports.get(0).log_records.get(0);
        Assert.assertTrue("Incorrect Log", log.message.equals("Hi there John Smith"));
        Assert.assertTrue("Incorrect Level", log.level.equals("W"));
    }

    @Test
    public void errorfLog() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);
        runtime.errorf("Hi there %s %s", "John", "Smith");
        runtime.flush();
        LogRecord log = testReporter.reports.get(0).log_records.get(0);
        Assert.assertTrue("Incorrect Log", log.message.equals("Hi there John Smith"));
        Assert.assertTrue("Incorrect Level", log.level.equals("E"));
    }

    // -----------
    // SPAN TESTS
    // -----------
    @Test
    public void infofSpan() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);
        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        ActiveSpan activeSpan = runtime.span("Test Span Infof");
        activeSpan.infof("Hi there %s %s", "John", "Smith");
        activeSpan.end();
        runtime.flush();

        SpanRecord span = testReporter.reports.get(0).span_records.get(0);
        Assert.assertTrue("Incorrect Span", span.span_name.equals("Test Span Infof"));
        LogRecord log = testReporter.reports.get(0).log_records.get(0);
        Assert.assertTrue("Incorrect Log", log.message.equals("Hi there John Smith"));
        Assert.assertTrue("Incorrect Level", log.level.equals("I"));
        Assert.assertTrue("Incorrect Span Guid", log.span_guid.equals(span.span_guid));
    }

    @Test
    public void warnfSpan() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);
        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        ActiveSpan activeSpan = runtime.span("Test Span Warnf");
        activeSpan.warnf("Hi there %s %s", "John", "Smith");
        activeSpan.end();
        runtime.flush();

        SpanRecord span = testReporter.reports.get(0).span_records.get(0);
        Assert.assertTrue("Incorrect Span", span.span_name.equals("Test Span Warnf"));
        LogRecord log = testReporter.reports.get(0).log_records.get(0);
        Assert.assertTrue("Incorrect Log", log.message.equals("Hi there John Smith"));
        Assert.assertTrue("Incorrect Level", log.level.equals("W"));
        Assert.assertTrue("Incorrect Span Guid", log.span_guid.equals(span.span_guid));
    }

    @Test
    public void errorfSpan() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);
        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        ActiveSpan activeSpan = runtime.span("Test Span Errorf");
        activeSpan.errorf("Hi there %s %s", "John", "Smith");
        activeSpan.end();
        runtime.flush();

        SpanRecord span = testReporter.reports.get(0).span_records.get(0);
        Assert.assertTrue("Incorrect Span", span.span_name.equals("Test Span Errorf"));
        LogRecord log = testReporter.reports.get(0).log_records.get(0);
        Assert.assertTrue("Incorrect Log", log.message.equals("Hi there John Smith"));
        Assert.assertTrue("Incorrect Level", log.level.equals("E"));
        Assert.assertTrue("Incorrect Span Guid", log.span_guid.equals(span.span_guid));
    }

    // -------------
    // PAYLOAD TESTS
    // -------------
    @Test
    // Test: nullPayload()
    // -------------------
    public void nullPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.log("Null Log Payload", null);
        runtime.flush();

        String payload = testReporter.reports.get(0).log_records.get(0).payload_json;
        Assert.assertTrue("Payload should be null", payload == null);
    }

    @Test
    // Test: charPayload()
    // -------------------
    public void charPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.log("Char Log Payload", 'a');
        runtime.flush();

        checkPayload("\"a\"", testReporter);
    }

    @Test
    // Test: booleanPayload()
    // ----------------------
    public void booleanPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.log("Boolean Log Payload", true);
        runtime.flush();

        checkPayload("true", testReporter);
    }

    @Test
    // Test: integerPayload()
    // ----------------------
    public void integerPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.log("Integer Log Payload", -324234234);
        runtime.flush();

        checkPayload("-324234234", testReporter);
    }

    @Test
    // Test: doublePayload()
    // ---------------------
    public void doublePayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.log("Double Log Payload", 3.13123);
        runtime.flush();

        checkPayload("3.13123", testReporter);
    }

    @Test
    // Test: stringPayload()
    // ---------------------
    public void stringPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        runtime.log("String Log Payload", "Payload String Test");
        runtime.flush();

        checkPayload("\"Payload String Test\"", testReporter);
    }

    @Test
    // Test: arrayPayload()
    // ---------------------
    public void arrayPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        String [] stringArrayPayload = {"item1", "item2"};
        runtime.log("Array Payload", stringArrayPayload);
        runtime.flush();

        checkPayload("[ \"item1\", \"item2\" ]", testReporter);
    }

    @Test
    // Test: objectPayload()
    // ---------------------
    public void objectPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        DummyObject obj = new DummyObject();
        runtime.log("Object Log Payload", obj);
        runtime.flush();

        String expectedPayload = "{\n  \"charPayload\" : \"a\",\n  \"booleanPayload\" : true,\n  \"intPayload\" : -324234234,\n  \"doublePayload\" : 3.13123,\n  \"stringPayload\" : \"Payload String Test\"\n}";
        checkPayload(expectedPayload, testReporter);
    }

    @Test
    // Test: stressTestObjectPayload()
    // -------------------------------
    public void stressTestObjectPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        StressTestObject obj = new StressTestObject(1500);
        runtime.log("StressTestObject Log Payload", obj);
        runtime.flush();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY);
        try {
            String expectedPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            checkPayload(expectedPayload, testReporter);
        } catch (JsonProcessingException e) {
            Assert.assertTrue("Test JSON should have been created", false);
        }
    }

    @Test
    // Test: cylicObjectPayload()
    // -------------------------------
    public void cylicObjectPayload() {
        io.traceguide.instrument.runtime.JavaRuntime runtime = new io.traceguide.instrument.runtime.JavaRuntime();
        TestRuntimeReport testReporter = new TestRuntimeReport();
        runtime.setDebugRuntimeReporter(testReporter);

        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");
        Assert.assertTrue("Runtime should have initialized correctly", success);

        CyclicObject1 obj1 = new CyclicObject1();
        CyclicObject2 obj2 = new CyclicObject2(obj1);
        runtime.log("cyclic object", obj1);
        runtime.flush();

        String expectedPayload = "\"<Invalid Payload\"";
        checkPayload(expectedPayload, testReporter);
    }

    // --------------
    // HELPER METHODS
    // --------------

    private void checkLogs(List<LogRecord> logRecords) {
        for (LogRecord log : logRecords) {
            Assert.assertTrue("Timestamp is 0 or less", log.timestamp_micros > 0);
            Assert.assertTrue("Log message is invalid", log.message.length() > 0);
        }
    }

    private void checkSpans(List<SpanRecord> spanRecords) {
        for (SpanRecord span : spanRecords) {
            Assert.assertTrue("Oldest Micros is 0 or less", span.oldest_micros > 0);
            Assert.assertTrue("Youngest Micros is 0 or less", span.youngest_micros > 0);
            Assert.assertTrue("Span name is invalid", span.span_name.length() > 0);
            Assert.assertTrue("Runtime GUID is invalid", span.runtime_guid.length() > 0);
        }
    }

    private void checkPayload(String expectedPayload, TestRuntimeReport testReporter) {
        String payload = testReporter.reports.get(0).log_records.get(0).payload_json;
        Assert.assertTrue("Payload should be " + expectedPayload, payload.equals(expectedPayload));
    }

    private void checkPayload(String expectedPayload, int logNum, TestRuntimeReport testReporter) {
        String payload = testReporter.reports.get(0).log_records.get(logNum).payload_json;
        Assert.assertTrue("Payload should be " + expectedPayload, payload.equals(expectedPayload));
    }

    private void printPayload(TestRuntimeReport testReporter) {
        String payload = testReporter.reports.get(0).log_records.get(0).payload_json;
        System.out.println(payload);
    }

    private void printPayload(TestRuntimeReport testReporter, int logNum) {
        String payload = testReporter.reports.get(0).log_records.get(logNum).payload_json;
        System.out.println(payload);
    }

    // --------------
    // INNER CLASSES
    // --------------

    // Class: TestRuntimeReport
    // ------------------------
    // TestRuntimeReport can be utilized for debugging to intercept ReportRequests
    // when they are flushed to the server
    class TestRuntimeReport implements RuntimeReporter {
        ArrayList<ReportRequest> reports = new ArrayList<ReportRequest>();

        public void Report (ReportRequest report) {
            this.reports.add(report);
        }

        public void Clear() {
            reports = new ArrayList<ReportRequest>();
        }
    }

    // Class: DummyObject
    // ------------------
    // Utilized to test JSON serialization
    public class DummyObject {
        public Object objPayload = null;
        public char charPayload = 'a';
        public boolean booleanPayload = true;
        public int intPayload = -324234234;
        public double doublePayload = 3.13123;
        public String stringPayload = "Payload String Test";
    }

    // Class: StressTestObject
    // -----------------------
    // Utilized to Test JSON serialization with an object
    // that contains a large data structure, i.e. Tree Map
    public class StressTestObject {
        public Object objPayload = null;
        public char charPayload = 'a';
        public boolean booleanPayload = true;
        public int intPayload = -324234234;
        public double doublePayload = 3.13123;
        public String stringPayload = "Payload String Test";

        public String [] stringArrayPayload = {"item1", "item2"};
        public TreeMap<String, Integer> treeMapPayload;

        public StressTestObject(int num) {
            treeMapPayload = new TreeMap<String, Integer>();
            for (int i = 0; i < num; i++) {
                treeMapPayload.put("Key" + i, i);
            }
        }
    }

    // Class: CyclicObject1
    // --------------------
    // Utilized along with CylicObject2 to test JSON
    // serialization with cylic pointers.
    public class CyclicObject1 {
        public String message = "This CyclicObject1";
        public CyclicObject2 obj;

        public CyclicObject1 (){
            obj = new CyclicObject2(this);
        }
    }

    // Class: CyclicObject2
    // --------------------
    // Utilized along with CyclicObject1 to test JSON
    // serialization with cyclic pointers.
    public class CyclicObject2 {
        public String message = "This CyclicObject2";
        public CyclicObject1 obj;

        public CyclicObject2 (CyclicObject1 obj) {
            this.obj = obj;
        }
    }

}
