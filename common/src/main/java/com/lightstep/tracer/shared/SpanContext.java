package com.lightstep.tracer.shared;

import java.util.Map;
import java.util.HashMap;

public class SpanContext implements io.opentracing.SpanContext {
  private final String traceId;
  private final String spanId;
  private Map<String, String> baggage;

  public SpanContext(String traceId) {
    if (traceId == null) {
      this.traceId = "XXX";
    } else {
      this.traceId = traceId;
    }
    this.spanId = "YYY";
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
}
