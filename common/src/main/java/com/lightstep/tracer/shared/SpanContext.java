package com.lightstep.tracer.shared;

import java.util.Map;
import com.lightstep.tracer.grpc.SpanContext.Builder;

public class SpanContext implements io.opentracing.SpanContext {
    private final Builder ctxBuilder = com.lightstep.tracer.grpc.SpanContext.newBuilder();

    public SpanContext() {
        ctxBuilder.setTraceId(Util.generateRandomGUID());
        ctxBuilder.setSpanId(Util.generateRandomGUID());
    }

    public SpanContext(long traceId, long spanId) {
        ctxBuilder.setTraceId(traceId);
        ctxBuilder.setSpanId(spanId);
    }

    SpanContext(long traceId, long spanId, Map<String, String> baggage) {
        this(traceId, spanId);
        ctxBuilder.putAllBaggage(baggage);
    }

    SpanContext(long traceId, Map<String, String> baggage) {
        ctxBuilder.setTraceId(traceId);
        ctxBuilder.setSpanId(Util.generateRandomGUID());
        ctxBuilder.putAllBaggage(baggage);
    }

    SpanContext(long traceId) {
        this(traceId, null);
    }

    public long getSpanId() {
        return ctxBuilder.getSpanId();
    }

    public long getTraceId() {
        return ctxBuilder.getTraceId();
    }

    String getBaggageItem(String key) {
        return ctxBuilder.getBaggageOrDefault(key, null);
    }

    SpanContext withBaggageItem(String key, String value) {
        ctxBuilder.putBaggage(key, value);
        return this;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return ctxBuilder.getBaggageMap().entrySet();
    }

    public Builder getInnerSpanCtx() {
        return ctxBuilder;
    }
}
