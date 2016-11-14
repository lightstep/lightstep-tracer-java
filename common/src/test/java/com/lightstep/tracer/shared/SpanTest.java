package com.lightstep.tracer.shared;

import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.LogRecord;
import com.lightstep.tracer.thrift.SpanRecord;
import com.lightstep.tracer.thrift.TraceJoinId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.lightstep.tracer.shared.Span.LOG_KEY_EVENT;
import static com.lightstep.tracer.shared.Span.LOG_KEY_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpanTest {
    private static final String ACCESS_TOKEN = "access123";
    private static final String TRACE_ID = "trace456";
    private static final String SPAN_ID = "span789";

    private SpanContext spanContext;
    private SpanRecord spanRecord;

    @Mock
    private AbstractTracer abstractTracer;

    private Span undertest;

    @Before
    public void setup() {
        when(abstractTracer.getAccessToken()).thenReturn(ACCESS_TOKEN);

        spanContext = new SpanContext(TRACE_ID, SPAN_ID, null);
        spanRecord = new SpanRecord();
        undertest = new Span(abstractTracer, spanContext, spanRecord, 0L);
    }

    @Test
    public void testContext() {
        assertSame(spanContext, undertest.context());
    }

    @Test
    public void testFinish_withoutFinishTime() {
        assertEquals(0L, spanRecord.getYoungest_micros());
        undertest.finish();
        assertNotEquals(0L, spanRecord.getYoungest_micros());
        verify(abstractTracer).addSpan(spanRecord);
    }

    @Test
    public void testFinish_withFinishTime() {
        long finishTimeMicros = 123L;
        undertest.finish(finishTimeMicros);
        assertEquals(finishTimeMicros, spanRecord.getYoungest_micros());
        verify(abstractTracer).addSpan(spanRecord);
    }

    @Test
    public void testSetTag_stringTypeNullValue() {
        Span result = undertest.setTag("k", (String) null);
        assertSame(result, undertest);
        verify(abstractTracer).debug("key (k) or value (null) is null, ignoring");
        assertNull("When value is null, should not be added to join ids", spanRecord.getJoin_ids());
        assertNull("When value is null, should not be added to attributes", spanRecord.getAttributes());
    }

    @Test
    public void testSetTag_stringTypeNullKey() {
        Span result = undertest.setTag(null, "v");
        assertSame(result, undertest);
        verify(abstractTracer).debug("key (null) or value (v) is null, ignoring");
        assertNull("When key is null, should not be added to join ids", spanRecord.getJoin_ids());
        assertNull("When key is null, should not be added to attributes", spanRecord.getAttributes());
    }

    @Test
    public void testSetTag_stringTypeJoinKey() {
        Span result = undertest.setTag("join:me", "v");
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(spanRecord.getJoin_ids());
        assertEquals(1, spanRecord.getJoin_idsSize());
        assertEquals(new TraceJoinId("join:me", "v"), spanRecord.getJoin_ids().get(0));
        assertNull("Join tags should not be added to attributes", spanRecord.getAttributes());
    }

    @Test
    public void testSetTag_stringTypeNotAJoinKey() {
        Span result = undertest.setTag("a-key", "v");
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(spanRecord.getAttributes());
        assertEquals(1, spanRecord.getAttributesSize());
        assertEquals(new KeyValue("a-key", "v"), spanRecord.getAttributes().get(0));
        assertNull("Non-join tags should not be added to join ids", spanRecord.getJoin_ids());
    }

    @Test
    public void testSetTag_booleanTypeNullKey() {
        Span result = undertest.setTag(null, false);
        assertSame(result, undertest);
        verify(abstractTracer).debug("key is null, ignoring");
        assertNull("When key is null, should not be added to join ids", spanRecord.getJoin_ids());
        assertNull("When key is null, should not be added to attributes", spanRecord.getAttributes());
    }

    @Test
    public void testSetTag_booleanTypeJoinKey() {
        Span result = undertest.setTag("join:me", true);
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(spanRecord.getJoin_ids());
        assertEquals(1, spanRecord.getJoin_idsSize());
        assertEquals(new TraceJoinId("join:me", "true"), spanRecord.getJoin_ids().get(0));
        assertNull("Join tags should not be added to attributes", spanRecord.getAttributes());
    }

    @Test
    public void testSetTag_booleanTypeNotAJoinKey() {
        Span result = undertest.setTag("a-key", true);
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(spanRecord.getAttributes());
        assertEquals(1, spanRecord.getAttributesSize());
        assertEquals(new KeyValue("a-key", "true"), spanRecord.getAttributes().get(0));
        assertNull("Non-join tags should not be added to join ids", spanRecord.getJoin_ids());
    }

    @Test
    public void testSetTag_numberTypeNullValue() {
        Span result = undertest.setTag("k", (Number) null);
        assertSame(result, undertest);
        verify(abstractTracer).debug("key (k) or value (null) is null, ignoring");
        assertNull("When value is null, should not be added to join ids", spanRecord.getJoin_ids());
        assertNull("When value is null, should not be added to attributes", spanRecord.getAttributes());
    }

    @Test
    public void testSetTag_numberTypeNullKey() {
        Span result = undertest.setTag(null, 1);
        assertSame(result, undertest);
        verify(abstractTracer).debug("key (null) or value (1) is null, ignoring");
        assertNull("When key is null, should not be added to join ids", spanRecord.getJoin_ids());
        assertNull("When key is null, should not be added to attributes", spanRecord.getAttributes());
    }

    @Test
    public void testSetTag_numberTypeJoinKey() {
        Span result = undertest.setTag("join:me", 2);
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(spanRecord.getJoin_ids());
        assertEquals(1, spanRecord.getJoin_idsSize());
        assertEquals(new TraceJoinId("join:me", "2"), spanRecord.getJoin_ids().get(0));
        assertNull("Join tags should not be added to attributes", spanRecord.getAttributes());
    }

    @Test
    public void testSetTag_numberTypeNotAJoinKey() {
        Span result = undertest.setTag("a-key", 3);
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(spanRecord.getAttributes());
        assertEquals(1, spanRecord.getAttributesSize());
        assertEquals(new KeyValue("a-key", "3"), spanRecord.getAttributes().get(0));
        assertNull("Non-join tags should not be added to join ids", spanRecord.getJoin_ids());
    }

    @Test
    public void testGetBaggageItem() {
        // returns null when no baggage
        assertNull(undertest.getBaggageItem("a-key"));

        // returns value if found in baggage
        spanContext = spanContext.withBaggageItem("a-key", "v");
        undertest = new Span(abstractTracer, spanContext, spanRecord, 0L);
        assertEquals("v", undertest.getBaggageItem("a-key"));

        // returns null when baggage exists, but key is missing
        assertNull(undertest.getBaggageItem("bogus"));
    }

    @Test
    public void testSetBaggageItem() {
        Span result = undertest.setBaggageItem("a-key", "v");
        assertSame(result, undertest);
        assertEquals("v", undertest.getBaggageItem("a-key"));
        assertNotSame(spanContext, undertest.context());
    }

    @Test
    public void testSetOperationName() {
        Span result = undertest.setOperationName("my-operation");
        assertSame(result, undertest);
        assertEquals("my-operation", spanRecord.getSpan_name());
    }

    @Test
    public void testClose() {
        assertEquals(0L, spanRecord.getYoungest_micros());
        undertest.close();
        assertNotEquals(0L, spanRecord.getYoungest_micros());
        verify(abstractTracer).addSpan(spanRecord);
    }

    @Test
    public void testIsJoinKey_nullKey() {
        assertFalse(Span.isJoinKey(null));
    }

    @Test
    public void testIsJoinKey_emptyKey() {
        assertFalse(Span.isJoinKey(""));
    }

    @Test
    public void testIsJoinKey_whitespaceKey() {
        assertFalse(Span.isJoinKey("    "));
    }

    @Test
    public void testIsJoinKey_notAJoinKey() {
        assertFalse(Span.isJoinKey("jon:"));
    }

    @Test
    public void testIsJoinKey_isAJoinKey() {
        assertTrue(Span.isJoinKey("join:"));
    }

    @Test
    public void testLog_fieldsOnly_eventProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put(LOG_KEY_EVENT, "my-key-event");
        fields.put("foo", "bar");

        Span result = undertest.log(fields);

        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("my-key-event", logRecord.getMessage());
        assertEquals("\"{foo=bar}\"", logRecord.getPayload_json());
        assertNotNull(logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_fieldsOnly_messageProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put(LOG_KEY_MESSAGE, "my-key-message");
        fields.put("foo", "bar");

        Span result = undertest.log(fields);

        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("my-key-message", logRecord.getMessage());
        assertEquals("\"{foo=bar}\"", logRecord.getPayload_json());
        assertNotNull(logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_fieldsOnly_noEventOrMessageProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put("foo", "bar");

        Span result = undertest.log(fields);

        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("1 key-value pair", logRecord.getMessage());
        assertEquals("\"{foo=bar}\"", logRecord.getPayload_json());
        assertNotNull(logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_timeAndFields_eventProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put(LOG_KEY_EVENT, "my-key-event");
        fields.put("foo", "bar");

        Span result = undertest.log(100L, fields);

        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("my-key-event", logRecord.getMessage());
        assertEquals("\"{foo=bar}\"", logRecord.getPayload_json());
        assertEquals(100L, logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_timeAndFields_messageProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put(LOG_KEY_MESSAGE, "my-key-message");
        fields.put("foo", "bar");

        Span result = undertest.log(100L, fields);

        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("my-key-message", logRecord.getMessage());
        assertEquals("\"{foo=bar}\"", logRecord.getPayload_json());
        assertEquals(100L, logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_timeAndFields_noEventOrMessageProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put("foo", "bar");

        Span result = undertest.log(100L, fields);

        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("1 key-value pair", logRecord.getMessage());
        assertEquals("\"{foo=bar}\"", logRecord.getPayload_json());
        assertEquals(100L, logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_messageOnly() {
        Span result = undertest.log("my message");
        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("my message", logRecord.getMessage());
        assertNull(logRecord.getPayload_json());
        assertNotNull(logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_timeAndMessage() {
        Span result = undertest.log(100L, "my message");
        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("my message", logRecord.getMessage());
        assertNull(logRecord.getPayload_json());
        assertEquals(100L, logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_messageAndPayload() {
        Span result = undertest.log("my message", "{aKey:1}");
        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("my message", logRecord.getMessage());
        assertEquals("\"{aKey:1}\"", logRecord.getPayload_json());
        assertNotNull(logRecord.getTimestamp_micros());
    }

    @Test
    public void testLog_timeMessageAndPayload() {
        Span result = undertest.log(100L, "my message", "{aKey:1}");
        assertSame(result, undertest);
        assertEquals(1, spanRecord.getLog_recordsSize());
        LogRecord logRecord = spanRecord.getLog_records().get(0);
        assertEquals("my message", logRecord.getMessage());
        assertEquals("\"{aKey:1}\"", logRecord.getPayload_json());
        assertEquals(100L, logRecord.getTimestamp_micros());
    }

    @Test
    public void testGenerateTraceURL() {
        String result = undertest.generateTraceURL();
        assertTrue("Unexpected trace url: " + result,
                result.startsWith("https://app.lightstep.com/" + ACCESS_TOKEN + "/trace?span_guid="
                        + SPAN_ID + "&at_micros="));
    }

    @Test
    public void testStringToJSONValue() {
        assertEquals("\"\\\"quoted\\\"\"", Span.stringToJSONValue("\"quoted\""));
        assertEquals("\"\\\\back-slashed\\\\\"", Span.stringToJSONValue("\\back-slashed\\"));
        assertEquals("\"\\/fwd-slashed\\/\"", Span.stringToJSONValue("/fwd-slashed/"));
        assertEquals("\"\\ttabbed\"", Span.stringToJSONValue("\ttabbed"));
        assertEquals("\"\\bbackspace\"", Span.stringToJSONValue("\bbackspace"));
        assertEquals("\"\\nnewline\"", Span.stringToJSONValue("\nnewline"));
        assertEquals("\"\\rreturn\"", Span.stringToJSONValue("\rreturn"));
        assertEquals("\"\\ffeed\"", Span.stringToJSONValue("\ffeed"));
    }
}