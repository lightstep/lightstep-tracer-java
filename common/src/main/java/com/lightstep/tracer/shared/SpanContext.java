package com.lightstep.tracer.shared;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class SpanContext implements io.opentracing.SpanContext {
    private final long traceId;
    private final long spanId;
    private Map<String, String> baggage;

    public SpanContext() {
        this.traceId = RandomUtil.generateGUID();
        this.spanId = RandomUtil.generateGUID();
    }

    public SpanContext(long traceId, long spanId) {
        this.traceId = traceId;
        this.spanId = spanId;
    }

    SpanContext(long traceId, long spanId, Map<String, String> baggage) {
        this(traceId, spanId);
        this.baggage = baggage;
    }

    SpanContext(long traceId, Map<String, String> baggage) {
        this.traceId = traceId;
        this.spanId = RandomUtil.generateGUID();
        this.baggage = baggage;
    }

    SpanContext(long traceId) {
        this(traceId, null);
    }

    public long getSpanId() {
        return spanId;
    }

    public long getTraceId() {
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
