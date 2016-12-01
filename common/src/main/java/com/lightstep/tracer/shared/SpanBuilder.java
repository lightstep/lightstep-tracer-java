package com.lightstep.tracer.shared;

import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.SpanRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.Tracer;

import static io.opentracing.References.CHILD_OF;
import static io.opentracing.References.FOLLOWS_FROM;

public class SpanBuilder implements Tracer.SpanBuilder {
    /**
     * The tag key used to record the relationship between child and parent
     * spans.
     */
    static final String PARENT_SPAN_GUID_KEY = "parent_span_guid";

    private final String operationName;
    private Long spanId = null;
    private final Map<String, String> tags;
    private final AbstractTracer tracer;
    private SpanContext parent;
    private long startTimestampMicros;

    SpanBuilder(String operationName, AbstractTracer tracer) {
        this.operationName = operationName;
        this.tracer = tracer;
        tags = new HashMap<>();
    }

    public Tracer.SpanBuilder asChildOf(io.opentracing.Span parent) {
        return asChildOf(parent.context());
    }

    public Tracer.SpanBuilder asChildOf(io.opentracing.SpanContext parent) {
        return addReference(CHILD_OF, parent);
    }

    public Tracer.SpanBuilder addReference(String type, io.opentracing.SpanContext referredTo) {
        if (CHILD_OF.equals(type) || FOLLOWS_FROM.equals(type)) {
            parent = (SpanContext) referredTo;
        }
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, boolean value) {
        tags.put(key, String.valueOf(value));
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, Number value) {
        tags.put(key, value.toString());
        return this;
    }

    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        startTimestampMicros = microseconds;
        return this;
    }

    public Tracer.SpanBuilder withSpanId(long spanId) {
        this.spanId = spanId;
        return this;
    }

    public Iterable<Map.Entry<String, String>> baggageItems() {
        if (parent == null) {
            return Collections.emptySet();
        } else {
            return parent.baggageItems();
        }
    }

    public io.opentracing.Span start() {
        if (tracer.isDisabled()) {
            return NoopSpan.INSTANCE;
        }

        long startTimestampRelativeNanos = -1;
        if (startTimestampMicros == 0) {
            startTimestampRelativeNanos = System.nanoTime();
            startTimestampMicros = AbstractTracer.nowMicrosApproximate();
        }

        SpanRecord record = new SpanRecord();
        record.setSpan_name(operationName);
        record.setOldest_micros(startTimestampMicros);

        String traceId = null;
        if (parent != null) {
            traceId = parent.getTraceId();
            record.addToAttributes(new KeyValue(
                    PARENT_SPAN_GUID_KEY,
                    parent.getSpanId()));
        }
        SpanContext newSpanContext;
        if (spanId != null) {
            newSpanContext = new SpanContext(traceId, spanId.toString(), null); // traceId may be null
        } else {
            newSpanContext = new SpanContext(traceId); // traceId may be null
        }
        // Record the eventual TraceId and SpanId in the SpanRecord.
        record.setTrace_guid(newSpanContext.getTraceId());
        record.setSpan_guid(newSpanContext.getSpanId());

        Span span = new Span(tracer, newSpanContext, record, startTimestampRelativeNanos);
        for (Map.Entry<String, String> pair : tags.entrySet()) {
            span.setTag(pair.getKey(), pair.getValue());
        }
        return span;
    }
}