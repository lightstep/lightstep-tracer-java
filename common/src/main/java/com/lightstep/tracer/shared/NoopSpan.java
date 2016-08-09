package com.lightstep.tracer.shared;

import io.opentracing.Span;
import io.opentracing.SpanContext;

// A span which is returned when the tracer is disabled.
class NoopSpan implements Span {

    static final Span INSTANCE = new NoopSpan();
    static final SpanContext CONTEXT = new NoopSpanContext();

    private NoopSpan(){}

    @Override
    public SpanContext context() { return CONTEXT; }

    @Override
    public void finish() {}
    public void finish(long timestamp) {}

    @Override
    public void close() {}

    @Override
    public Span setTag(String key, String value) {
      return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
      return this;
    }

    @Override
    public Span setTag(String key, Number value) {
      return this;
    }

    @Override
    public Span log(String message, /* @Nullable */ Object payload) {
      return this;
    }

    @Override
    public Span log(long instantMicroseconds, String message, /* @Nullable */ Object payload) {
      return this;
    }

    private static class NoopSpanContext implements SpanContext {
      @Override
      public String getBaggageItem(String key) { return null; }

      @Override
      public SpanContext setBaggageItem(String key, String value) { return this; }
    }
}
