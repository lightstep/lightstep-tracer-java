package com.lightstep.tracer.shared;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class SpanContext implements io.opentracing.SpanContext {
    private final String traceId;
    private final String spanId;
    private Map<String, String> baggage;

    public SpanContext(String traceId, String spanId) {
        this.traceId = traceId == null ? RandomUtil.generateGUID() : traceId;
        this.spanId = spanId == null ? RandomUtil.generateGUID() : spanId;
    }

    SpanContext(String traceId, String spanId, Map<String, String> baggage) {
        this(traceId, spanId);
        this.baggage = baggage;
    }

    SpanContext(String traceId) {
        this(traceId, null, null);
    }

    public String getSpanId() {
        return spanId;
    }

    public String getTraceId() {
        return traceId;
    }

    String getBaggageItem(String key) {
        if (baggage == null) {
            return null;
        }
        return baggage.get(key);
    }

    SpanContext withBaggageItem(String key, String value) {
        Map<String, String> baggageCopy;
        if (baggage == null) {
            baggageCopy = new HashMap<>();
        } else {
            baggageCopy = new HashMap<>(baggage);
        }
        baggageCopy.put(key, value);
        return new SpanContext(traceId, spanId, baggageCopy);
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        if (baggage == null) {
            return Collections.emptySet();
        } else {
            return baggage.entrySet();
        }
    }
}
