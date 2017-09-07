package com.lightstep.tracer.shared;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Collections;
import java.util.Map;

// A span which is returned when the tracer is disabled.
class NoopSpan implements Span {

    static final Span INSTANCE = new NoopSpan();
    private static final SpanContext CONTEXT = new NoopSpanContext();

    private NoopSpan() {
    }

    @Override
    public SpanContext context() {
        return CONTEXT;
    }

    @Override
    public void finish() {
    }

    @Override
    public void finish(long timestamp) {
    }

    @Override
    public void close() {
    }

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
    public Span log(String message) {
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, String message) {
        return this;
    }

    @Override
    public Span log(String message, /* @Nullable */ Object payload) {
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, String message, /* @Nullable */ Object payload) {
        return this;
    }

    @Override
    public final Span log(Map<String, ?> fields) {
        return this;
    }

    @Override
    public final Span log(long timestampMicros, Map<String, ?> fields) {
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return null;
    }

    @Override
    public Span setBaggageItem(String key, String value) {
        return this;
    }

    public Span setOperationName(String operationName) {
        return this;
    }

    private static class NoopSpanContext implements SpanContext {
        @Override
        public Iterable<Map.Entry<String, String>> baggageItems() {
            return Collections.emptySet();
        }
    }
}
