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

public class Span implements com.lightstep.tracer.Span {

  private final Object mutex = new Object();
  private final AbstractTracer tracer;
  private final SpanRecord record;
  private final String traceID;
  private final ObjectMapper objectToJsonMapper;

  Span (AbstractTracer tracer, SpanRecord record, String traceID) {
    this.tracer = tracer;
    this.record = record;
    this.traceID = traceID;

    this.objectToJsonMapper = new ObjectMapper();
    this.objectToJsonMapper.setSerializationInclusion(
      com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY);
  }

  public String getTraceID() {
    synchronized (this.mutex) {
      return this.traceID;
    }
  }

  public String getGUID() {
    synchronized (this.mutex) {
      return this.record.getSpan_guid();
    }
  }

  public AbstractTracer getTracer() {
    return this.tracer;
  }

  public void finish() {
    synchronized (this.mutex) {
      this.record.setYoungest_micros(System.currentTimeMillis() * 1000);
      this.tracer.addSpan(record);
    }
  }

  private static final boolean isJoinKey(String key) {
    return key.startsWith("join:");
  }

  public com.lightstep.tracer.Span setTag(String key, String value) {
    synchronized (this.mutex) {
      if (isJoinKey(key)) {
        this.record.addToJoin_ids(new TraceJoinId(key, value));
      } else {
        this.record.addToAttributes(new KeyValue(key, value));
      }
    }
    return this;
  }

  public com.lightstep.tracer.Span setTag(String key, boolean value){
    synchronized (this.mutex) {
      if (isJoinKey(key)) {
        this.record.addToJoin_ids(new TraceJoinId(key, value ? "true" : "false"));
      } else {
        this.record.addToAttributes(new KeyValue(key, value ? "true" : "false"));
      }
    }
    return this;
  }

  public com.lightstep.tracer.Span setTag(String key, Number value) {
    synchronized (this.mutex) {
      if (isJoinKey(key)) {
        this.record.addToJoin_ids(new TraceJoinId(key, value.toString()));
      } else {
        this.record.addToAttributes(new KeyValue(key, value.toString()));
      }
    }
    return this;
  }

  public com.lightstep.tracer.Span setBaggageItem(String key, String value) {
    // TODO implement baggage
    return this;
  }

  public String getBaggageItem(String key) {
    // TODO implement baggage
    return "";
  }

  public com.lightstep.tracer.Span log(String message, /* @Nullable */ Object payload) {
    return log(System.currentTimeMillis() * 1000, message, payload);
  }

  public com.lightstep.tracer.Span log(long timestampMicroseconds, String message, /* @Nullable */ Object payload) {
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
          // TODO and/or log this somewhere?
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
        "/trace?span_guid=" + this.getGUID() +
        "&at_micros=" + (System.currentTimeMillis() * 1000);
    }
  }

  public SpanRecord thriftRecord() {
    return this.record;
  }
}
