package com.lightstep.tracer.shared;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class SpanContext implements io.opentracing.SpanContext {
  private final String traceId;
  private final String spanId;
  private Map<String, String> baggage;

  SpanContext(String traceId, String spanId, Map<String, String> baggage) {
    this.traceId = traceId == null ? AbstractTracer.generateGUID() : traceId;
    this.spanId = spanId == null ? AbstractTracer.generateGUID() : spanId;
    this.baggage = baggage;
  }

  SpanContext(String traceId) {
    this(traceId, null, null);
  }

  public String getSpanId() {
    return this.spanId;
  }
  public String getTraceId() {
    return this.traceId;
  }
  public String getBaggageItem(String key) {
    if (this.baggage == null) {
      return null;
    }
    return this.baggage.get(key);
  }

  public SpanContext withBaggageItem(String key, String value) {
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
      return Collections.EMPTY_SET;
    } else {
      return baggage.entrySet();
    }
  }
}
