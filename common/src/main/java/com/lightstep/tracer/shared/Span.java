package com.lightstep.tracer.shared;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.lightstep.tracer.shared.AbstractTracer;
import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.LogRecord;
import com.lightstep.tracer.thrift.SpanRecord;
import com.lightstep.tracer.thrift.TraceJoinId;

public class Span implements io.opentracing.Span {

  private final Object mutex = new Object();
  private final AbstractTracer tracer;
  private SpanContext context;
  private final SpanRecord record;
  private final long startTimestampRelativeNanos;
  private final ObjectMapper objectToJsonMapper;

  Span(AbstractTracer tracer, SpanContext context, SpanRecord record, long startTimestampRelativeNanos) {
    this.context = context;
    this.tracer = tracer;
    this.record = record;
    this.startTimestampRelativeNanos = startTimestampRelativeNanos;

    this.objectToJsonMapper = new ObjectMapper();
    this.objectToJsonMapper.setSerializationInclusion(
      com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY);
  }

  @Override
  public io.opentracing.SpanContext context() {
    return this.context;
  }

  @Override
  public void finish() {
    // Note that this.startTimestampRelativeNanos will be -1 if the user
    // provide an explicit start timestamp in the SpanBuilder.
    if (this.startTimestampRelativeNanos > 0) {
      long durationMicros = (System.nanoTime() - this.startTimestampRelativeNanos) / 1000;
      this.finish(this.record.getOldest_micros() + durationMicros);
    } else {
      this.finish(System.currentTimeMillis() * 1000);
    }
  }

  @Override
  public void finish(long finishTimeMicros) {
    synchronized (this.mutex) {
      this.record.setYoungest_micros(finishTimeMicros);
      this.tracer.addSpan(record);
    }
  }

  @Override
  public Span setTag(String key, String value) {
    if (key == null || value == null) {
      this.tracer.debug("key (" + key + ") or value (" + value + ") is null, ignoring");
      return this;
    }
    synchronized (this.mutex) {
      if (isJoinKey(key)) {
        this.record.addToJoin_ids(new TraceJoinId(key, value));
      } else {
        this.record.addToAttributes(new KeyValue(key, value));
      }
    }
    return this;
  }

  @Override
  public Span setTag(String key, boolean value) {
    synchronized (this.mutex) {
      if (isJoinKey(key)) {
        this.record.addToJoin_ids(new TraceJoinId(key, value ? "true" : "false"));
      } else {
        this.record.addToAttributes(new KeyValue(key, value ? "true" : "false"));
      }
    }
    return this;
  }

  @Override
  public Span setTag(String key, Number value) {
    if (key == null || value == null) {
      this.tracer.debug("key (" + key + ") or value (" + value + ") is null, ignoring");
      return this;
    }
    synchronized (this.mutex) {
      if (isJoinKey(key)) {
        this.record.addToJoin_ids(new TraceJoinId(key, value.toString()));
      } else {
        this.record.addToAttributes(new KeyValue(key, value.toString()));
      }
    }
    return this;
  }

  @Override
  public synchronized String getBaggageItem(String key) {
    return this.context.getBaggageItem(key);
  }

  @Override
  public synchronized Span setBaggageItem(String key, String value) {
    this.context = this.context.withBaggageItem(key, value);
    return this;
  }

  public void close() {
    this.finish();
  }

  private static final boolean isJoinKey(String key) {
    return key.startsWith("join:");
  }

  public AbstractTracer getTracer() {
    return this.tracer;
  }

  public Span log(String message, /* @Nullable */ Object payload) {
    return log(System.currentTimeMillis() * 1000, message, payload);
  }

  public Span log(long timestampMicroseconds, String message, /* @Nullable */ Object payload) {
    LogRecord log = new LogRecord();

    log.setTimestamp_micros(timestampMicroseconds);
    log.setMessage(message);

    if (payload != null) {
      // TODO perhaps if the payload is an exception, treat log as an error?
      if (payload instanceof JSONObject || payload instanceof JSONArray) {
        log.setPayload_json(payload.toString());
      } else {
        try {
          String payloadString = objectToJsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
          log.setPayload_json(payloadString);
        } catch (JsonProcessingException e) {
          // Just use a string.
          this.tracer.debug("Payload not serializable to JSON. Setting as String. (SpanId=" +
              ((SpanContext)this.context()).getSpanId() + ")");
          log.setPayload_json(payload.toString());
        }
      }
    }

    synchronized (this.mutex) {
      this.record.addToLog_records(log);
    }
    return this;
  }

  public String generateTraceURL() {
    synchronized (this.mutex) {
      return "https://app.lightstep.com/" + tracer.getAccessToken() +
        "/trace?span_guid=" + this.context.getSpanId() +
        "&at_micros=" + (System.currentTimeMillis() * 1000);
    }
  }

  public SpanRecord thriftRecord() {
    return this.record;
  }
}
