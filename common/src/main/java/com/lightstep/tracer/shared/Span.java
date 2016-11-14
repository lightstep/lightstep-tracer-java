package com.lightstep.tracer.shared;

import java.util.HashMap;
import java.util.Map;

import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.LogRecord;
import com.lightstep.tracer.thrift.SpanRecord;
import com.lightstep.tracer.thrift.TraceJoinId;

public class Span implements io.opentracing.Span {

    static final String LOG_KEY_EVENT = "event";
    static final String LOG_KEY_MESSAGE = "message";

    private final Object mutex = new Object();
    private final AbstractTracer tracer;
    private final SpanRecord record;
    private final long startTimestampRelativeNanos;

    private SpanContext context;

    Span(AbstractTracer tracer, SpanContext context, SpanRecord record, long startTimestampRelativeNanos) {
        this.context = context;
        this.tracer = tracer;
        this.record = record;
        this.startTimestampRelativeNanos = startTimestampRelativeNanos;
    }

    @Override
    public io.opentracing.SpanContext context() {
        return context;
    }

    @Override
    public void finish() {
        finish(nowMicros());
    }

    @Override
    public void finish(long finishTimeMicros) {
        synchronized (mutex) {
            record.setYoungest_micros(finishTimeMicros);
            tracer.addSpan(record);
        }
    }

    @Override
    public Span setTag(String key, String value) {
        if (key == null || value == null) {
            tracer.debug("key (" + key + ") or value (" + value + ") is null, ignoring");
            return this;
        }
        synchronized (mutex) {
            if (isJoinKey(key)) {
                record.addToJoin_ids(new TraceJoinId(key, value));
            } else {
                record.addToAttributes(new KeyValue(key, value));
            }
        }
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        if (key == null) {
            tracer.debug("key is null, ignoring");
            return this;
        }
        synchronized (mutex) {
            if (isJoinKey(key)) {
                record.addToJoin_ids(new TraceJoinId(key, value ? "true" : "false"));
            } else {
                record.addToAttributes(new KeyValue(key, value ? "true" : "false"));
            }
        }
        return this;
    }

    @Override
    public Span setTag(String key, Number value) {
        if (key == null || value == null) {
            tracer.debug("key (" + key + ") or value (" + value + ") is null, ignoring");
            return this;
        }
        synchronized (mutex) {
            if (isJoinKey(key)) {
                record.addToJoin_ids(new TraceJoinId(key, value.toString()));
            } else {
                record.addToAttributes(new KeyValue(key, value.toString()));
            }
        }
        return this;
    }

    @Override
    public synchronized String getBaggageItem(String key) {
        return context.getBaggageItem(key);
    }

    @Override
    public synchronized Span setBaggageItem(String key, String value) {
        context = context.withBaggageItem(key, value);
        return this;
    }

    public synchronized Span setOperationName(String operationName) {
        record.setSpan_name(operationName);
        return this;
    }

    public void close() {
        finish();
    }

    static boolean isJoinKey(String key) {
        return key != null && key.startsWith("join:");
    }

    public AbstractTracer getTracer() {
        return tracer;
    }

    public final Span log(Map<String, ?> fields) {
        return log(nowMicros(), fields);
    }

    public final Span log(long timestampMicros, Map<String, ?> fields) {
        String message = null;
        Map<String, String> payload = new HashMap<>(fields.size());
        for (Map.Entry<String, ?> kv : fields.entrySet()) {
            final String key = kv.getKey();
            final Object inValue = kv.getValue();
            if (message == null &&
                    (key.equals(LOG_KEY_EVENT) || key.equals(LOG_KEY_MESSAGE)) &&
                    inValue instanceof String) {
                message = (String) inValue;
                continue;
            }
            String outValue;
            if (inValue == null) {
                outValue = "<null>";
            } else {
                outValue = inValue.toString();
            }
            payload.put(key, outValue);
        }
        if (message == null) {
            message = "" + payload.size() + " key-value pair" + (payload.size() == 1 ? "" : "s");
        }
        return log(timestampMicros, message, payload);
    }

    public Span log(String message) {
        return log(nowMicros(), message, null);
    }

    public Span log(long timestampMicroseconds, String message) {
        return log(timestampMicroseconds, message, null);
    }

    @Override
    public Span log(String message, /* @Nullable */ Object payload) {
        return log(nowMicros(), message, payload);
    }

    @Override
    public Span log(long timestampMicroseconds, String message, /* @Nullable */ Object payload) {
        LogRecord log = new LogRecord();

        log.setTimestamp_micros(timestampMicroseconds);
        log.setMessage(message);

        if (payload != null) {
            log.setPayload_json(Span.stringToJSONValue(payload.toString()));
        }

        synchronized (mutex) {
            record.addToLog_records(log);
        }
        return this;
    }

    public String generateTraceURL() {
        synchronized (mutex) {
            return "https://app.lightstep.com/" + tracer.getAccessToken() +
                    "/trace?span_guid=" + context.getSpanId() +
                    "&at_micros=" + (System.currentTimeMillis() * 1000);
        }
    }

    private long nowMicros() {
        // Note that startTimestampRelativeNanos will be -1 if the user
        // provided an explicit start timestamp in the SpanBuilder.
        if (startTimestampRelativeNanos > 0) {
            long durationMicros = (System.nanoTime() - startTimestampRelativeNanos) / 1000;
            return record.getOldest_micros() + durationMicros;
        } else {
            return System.currentTimeMillis() * 1000;
        }
    }

    /**
     * Quotes a plain string into a valid JSON value.
     *
     * Adapted from https://android.googlesource.com/platform/dalvik/libcore/json/src/main/java/org/json/JSONStringer.java
     */
    static String stringToJSONValue(String value) {
        StringBuffer out = new StringBuffer(value.length() + 2);
        out.append("\"");
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
      /*
       * From RFC 4627, "All Unicode characters may be placed within the
       * quotation marks except for the characters that must be escaped:
       * quotation mark, reverse solidus, and the control characters
       * (U+0000 through U+001F)."
       */
            switch (c) {
                case '"':
                case '\\':
                case '/':
                    out.append('\\').append(c);
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                default:
                    if (c <= 0x1F) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                    break;
            }
        }
        out.append("\"");
        return out.toString();
    }
}
