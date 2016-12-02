package com.lightstep.tracer.shared;

import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.SpanRecord;
import com.lightstep.tracer.thrift.TraceJoinId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.lightstep.tracer.shared.SpanBuilder.PARENT_SPAN_GUID_KEY;
import static io.opentracing.References.FOLLOWS_FROM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpanBuilderTest {
    private static final String OPERATION_NAME = "myOperation";
    private static final String TRACE_ID = "myTraceId";
    private static final String SPAN_ID = "mySpanId";

    @Mock
    private AbstractTracer tracer;

    @Mock
    private SpanContext context;

    private SpanBuilder undertest;
    private Iterable<Map.Entry<String, String>> baggageItems;

    @Before
    public void setup() {
        baggageItems = Collections.<String, String>emptyMap().entrySet();
        when(context.baggageItems()).thenReturn(baggageItems);

        when(context.getTraceId()).thenReturn(TRACE_ID);
        when(context.getSpanId()).thenReturn(SPAN_ID);

        undertest = new SpanBuilder(OPERATION_NAME, tracer);
    }

    /**
     * Confirms aspects of the resulting Span when the builder is given a parent Span.
     */
    @Test
    public void testStart_asChildOfSpan() throws Exception {
        Span span = new Span(null, context, null, 0L);
        undertest.asChildOf(span);
        verifySettingsFromParent();
    }

    /**
     * Confirms aspects of the resulting Span when the builder is given a parent SpanContext.
     */
    @Test
    public void testStart_asChildOfSpanContext() throws Exception {
        undertest.asChildOf(context);
        verifySettingsFromParent();
    }

    /**
     * Confirms aspects of the resulting Span when the builder is given a parent SpanContext.
     */
    @Test
    public void testStart_asFollowsFromSpanContext() throws Exception {
        undertest.addReference(FOLLOWS_FROM, context);
        verifySettingsFromParent();
    }

    @Test
    public void testStart_isDisabled() {
        when(tracer.isDisabled()).thenReturn(true);

        io.opentracing.Span result = undertest.start();
        assertSame(NoopSpan.INSTANCE, result);
    }

    /**
     * Confirms that the tags set on the builder are passed onto the Span.
     */
    @Test
    public void testTags() {
        // add one of each type of tag
        undertest.withTag("key1", "value1");
        undertest.withTag("key2", true);
        undertest.withTag("key3", 1001);
        undertest.withTag("join:key4", "value4");

        // start the Span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        SpanRecord record = lsSpan.getRecord();

        List<KeyValue> attributes = record.getAttributes();
        assertTrue(attributes.contains(new KeyValue("key1", "value1")));
        assertTrue(attributes.contains(new KeyValue("key2", "true")));
        assertTrue(attributes.contains(new KeyValue("key3", "1001")));

        List<TraceJoinId> joinIds = record.getJoin_ids();
        assertTrue(joinIds.contains(new TraceJoinId("join:key4", "value4")));

        verifyResultingSpan(lsSpan);
    }

    /**
     * When no parent is set, baggage items should be non-null and empty.
     */
    @Test
    public void testBaggageItems_noParent() {
        Iterable<Map.Entry<String, String>> result = undertest.baggageItems();
        assertNotNull(result);
        Iterator<Map.Entry<String, String>> iterator = result.iterator();
        assertFalse(iterator.hasNext());
    }

    /**
     * If no start timestamp is provided, the span's start time should be set to current and the
     * record's oldest micros should also be set to current.
     */
    @Test
    public void testStart_noStartTimeProvided() {
        // start the span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        assertTrue(lsSpan.getStartTimestampRelativeNanos() + " was not greater than zero",
                lsSpan.getStartTimestampRelativeNanos() > 0);
        assertTrue(lsSpan.getRecord().getOldest_micros() + " was not greater than zero",
                lsSpan.getRecord().getOldest_micros() > 0);

        verifyResultingSpan(lsSpan);
    }

    /**
     * If start timestamp is provided, the span's start time should not be set and the record's
     * oldest micros should be set to the value provided.
     */
    @Test
    public void testStart_startTimeProvided() {
        undertest.withStartTimestamp(2002L);

        // start the span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        assertEquals(-1, lsSpan.getStartTimestampRelativeNanos());
        assertEquals(2002L, lsSpan.getRecord().getOldest_micros());

        verifyResultingSpan(lsSpan);
    }

    /**
     * If start timestamp is provided, the span's start time should not be set and the record's
     * oldest micros should be set to the value provided.
     */
    @Test
    public void testStart_spanIdProvided() {
        undertest.withSpanId("123");

        // start the span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        assertEquals("123", lsSpan.context().getSpanId());

        verifyResultingSpan(lsSpan);
    }

    private void verifySettingsFromParent() {
        // verify that getBaggage returns baggage from parent
        Iterable<Map.Entry<String, String>> actualBaggageItems = undertest.baggageItems();
        assertSame(baggageItems, actualBaggageItems);

        // start the span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        // verify that parent's trace id is set on context in returned span
        SpanContext spanContext = lsSpan.context();
        assertEquals(TRACE_ID, spanContext.getTraceId());

        // verify that record has span id set
        SpanRecord spanRecord = lsSpan.getRecord();
        List<KeyValue> attributes = spanRecord.getAttributes();
        assertTrue(attributes.contains(new KeyValue(PARENT_SPAN_GUID_KEY, SPAN_ID)));

        verifyResultingSpan(lsSpan);
        assertEquals(TRACE_ID, context.getTraceId());
    }

    /**
     * Verify values that should be set on the result span regardless of other state.
     */
    private void verifyResultingSpan(Span resultingSpan) {
        SpanRecord record = resultingSpan.getRecord();
        SpanContext context = resultingSpan.context();

        assertNotEquals(SPAN_ID, context.getSpanId());

        assertEquals(OPERATION_NAME, record.getSpan_name());
        assertEquals(context.getTraceId(), record.getTrace_guid());
        assertEquals(context.getSpanId(), record.getSpan_guid());
    }
}