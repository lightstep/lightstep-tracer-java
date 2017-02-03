package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.KeyValue;

import com.lightstep.tracer.grpc.Reference;
import com.lightstep.tracer.grpc.Reference.Relationship;
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
    private final Map<String, String> stringTags;
    private final Map<String, Boolean> boolTags;
    private final AbstractTracer tracer;
    private SpanContext parent;
    private long startTimestampMicros;
    private final com.lightstep.tracer.grpc.Span.Builder grpcSpan = com.lightstep.tracer.grpc.Span.newBuilder();

    SpanBuilder(String operationName, AbstractTracer tracer) {
        this.operationName = operationName;
        this.tracer = tracer;
        stringTags = new HashMap<>();
        boolTags = new HashMap<>();
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
            Reference.Builder refBuilder = Reference.newBuilder();
            refBuilder.setSpanContext(parent.getInnerSpanCtx());
            if (CHILD_OF.equals(type)) {
                refBuilder.setRelationship(Relationship.CHILD_OF);
            } else {
                refBuilder.setRelationship(Relationship.FOLLOWS_FROM);
            }
            grpcSpan.addReferences(refBuilder);
        }
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, String value) {
        stringTags.put(key, value);
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, boolean value) {
        boolTags.put(key, value);
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, Number value) {
        // TODO use int or double value
        stringTags.put(key, value.toString());
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

        grpcSpan.setOperationName(operationName);
        grpcSpan.setStartTimestamp(Util.epochTimeMicrosToProtoTime(startTimestampMicros));

        Long traceId = this.traceId;
        if (parent != null) {
            traceId = parent.getTraceId();
            grpcSpan.addTags(KeyValue.newBuilder().setKey(PARENT_SPAN_GUID_KEY)
                .setStringValue(Long.toHexString(parent.getSpanId())));
        }
        SpanContext newSpanContext;
        if (traceId != null && spanId != null) {
            newSpanContext = new SpanContext(traceId, spanId);
        } else if (traceId != null) {
            newSpanContext = new SpanContext(traceId);
        } else {
            newSpanContext = new SpanContext();
        }

        // Set the SpanContext of the span
        grpcSpan.setSpanContext(newSpanContext.getInnerSpanCtx());

        Span span = new Span(tracer, newSpanContext, grpcSpan, startTimestampRelativeNanos);
        for (Map.Entry<String, String> pair : stringTags.entrySet()) {
            span.setTag(pair.getKey(), pair.getValue());
        }
        for (Map.Entry<String, Boolean> pair : boolTags.entrySet()) {
            span.setTag(pair.getKey(), pair.getValue());
        }
        return span;
    }
}