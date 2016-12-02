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
    private Long traceId = null;
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

    /**
     * Sets the traceId and the spanId for the span being created. If the span has a parent, the
     * traceId of the parent will override this traceId value.
     */
    public Tracer.SpanBuilder withTraceIdAndSpanId(long traceId, long spanId) {
        this.traceId = traceId;
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

        Long traceId = this.traceId;
        if (parent != null) {
            traceId = parent.getTraceId();
            record.addToAttributes(new KeyValue(
                    PARENT_SPAN_GUID_KEY,
                    Long.toHexString(parent.getSpanId())));
        }
        SpanContext newSpanContext;
        if (traceId != null && spanId != null) {
            newSpanContext = new SpanContext(traceId, spanId);
        } else if (traceId != null) {
            newSpanContext = new SpanContext(traceId);
        }    else {
            newSpanContext = new SpanContext();
        }

        // Record the eventual TraceId and SpanId in the SpanRecord.
        record.setTrace_guid(Long.toHexString(newSpanContext.getTraceId()));
        record.setSpan_guid(Long.toHexString(newSpanContext.getSpanId()));

        Span span = new Span(tracer, newSpanContext, record, startTimestampRelativeNanos);
        for (Map.Entry<String, String> pair : tags.entrySet()) {
            span.setTag(pair.getKey(), pair.getValue());
        }
        return span;
    }
}