package com.lightstep.tracer.shared;

import io.opentracing.Span;

// A span which is returned when the tracer is disabled.
class NoopSpan implements Span {

    static final Span INSTANCE = new NoopSpan();

    private NoopSpan(){}

    @Override
    public void finish() {}

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
    public Span setBaggageItem(String key, String value) {
      return this;
    }

    @Override
    public String getBaggageItem(String key) {
      return null;
    }

    @Override
    public Span log(String message, /* @Nullable */ Object payload) {
      return this;
    }

    @Override
    public Span log(long instantMicroseconds, String message, /* @Nullable */ Object payload) {
      return this;
    }
}
