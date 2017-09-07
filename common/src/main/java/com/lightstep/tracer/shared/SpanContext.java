package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.SpanContext.Builder;

import java.util.HashMap;
import java.util.Map;

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
        if (baggage != null) {
            ctxBuilder.putAllBaggage(baggage);
        }
    }

    SpanContext(long traceId, Map<String, String> baggage) {
        ctxBuilder.setTraceId(traceId);
        ctxBuilder.setSpanId(Util.generateRandomGUID());
        if (baggage != null) {
            ctxBuilder.putAllBaggage(baggage);
        }
    }

    SpanContext(long traceId) {
        this(traceId, null);
    }

    @SuppressWarnings("WeakerAccess")
    public long getSpanId() {
        return ctxBuilder.getSpanId();
    }

    @SuppressWarnings("WeakerAccess")
    public long getTraceId() {
        return ctxBuilder.getTraceId();
    }

    String getBaggageItem(String key) {
        return ctxBuilder.getBaggageOrDefault(key, null);
    }

    SpanContext withBaggageItem(String key, String value) {
        Map<String, String> baggageCopy;
        if (ctxBuilder.getBaggageMap() != null) {
            baggageCopy = new HashMap<>(ctxBuilder.getBaggageMap());
        } else {
            baggageCopy = new HashMap<>();
        }
        baggageCopy.put(key, value);
        ctxBuilder.putBaggage(key, value);
        return new SpanContext(ctxBuilder.getTraceId(), ctxBuilder.getSpanId(), baggageCopy);
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return ctxBuilder.getBaggageMap().entrySet();
    }

    @SuppressWarnings("WeakerAccess")
    public Builder getInnerSpanCtx() {
        return ctxBuilder;
    }
}
