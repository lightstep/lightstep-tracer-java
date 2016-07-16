package com.lightstep.tracer.shared;

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

  @Override
  public synchronized String getBaggageItem(String key) {
    if (this.baggage == null) {
      return null;
    }
    return this.baggage.get(key);
  }

  @Override
  public synchronized SpanContext setBaggageItem(String key, String value) {
    if (this.baggage == null) {
      this.baggage = new HashMap<String, String>();
    }
    this.baggage.put(key, value);
    return this;
  }

  public interface BaggageItemReader {
    public void readBaggageItem(String key, String value);
  }
  public synchronized void forEachBaggageItem(BaggageItemReader reader) {
    if (this.baggage == null)
      return;
    for (Map.Entry<String, String> entry : this.baggage.entrySet()) {
      reader.readBaggageItem(entry.getKey(), entry.getValue());
    }
  }
}
