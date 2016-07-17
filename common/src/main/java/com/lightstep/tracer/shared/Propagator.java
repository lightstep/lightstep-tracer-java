package com.lightstep.tracer.shared;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.HttpHeaderReader;
import io.opentracing.propagation.HttpHeaderWriter;
import io.opentracing.propagation.TextMapReader;
import io.opentracing.propagation.TextMapWriter;

public interface Propagator {
  boolean matchesInjectCarrier(Object carrier);
  boolean matchesExtractCarrier(Object carrier);
  void inject(SpanContext spanContext, Object carrier);
  SpanContext extract(Object carrier);

  // The three supported Propagators.
  static final Propagator TEXT_MAP = new Propagator() {
    private static final String PREFIX_TRACER_STATE = "ot-tracer-";
    private static final String PREFIX_BAGGAGE      = "ot-baggage-";

    private static final int TRACER_STATE_FIELD_COUNT = 3;
    private static final String FIELD_NAME_TRACE_ID   = PREFIX_TRACER_STATE + "traceid";
    private static final String FIELD_NAME_SPAN_ID    = PREFIX_TRACER_STATE + "spanid";
    private static final String FIELD_NAME_SAMPLED    = PREFIX_TRACER_STATE + "sampled";

    public boolean matchesInjectCarrier(Object carrier) {
      return carrier instanceof TextMapWriter;
    }
    public boolean matchesExtractCarrier(Object carrier) {
      return carrier instanceof TextMapReader;
    }

    public void inject(SpanContext spanContext, Object carrier) {
      final TextMapWriter textMapWriter = (TextMapWriter)carrier;
      textMapWriter.put(FIELD_NAME_TRACE_ID, spanContext.getTraceId());
      textMapWriter.put(FIELD_NAME_SPAN_ID, spanContext.getSpanId());
      textMapWriter.put(FIELD_NAME_SAMPLED, "true");
      spanContext.forEachBaggageItem(new SpanContext.BaggageItemReader() {
        public void readBaggageItem(String key, String value) {
          textMapWriter.put(PREFIX_BAGGAGE + key, value);
        }
      });
    }

    public SpanContext extract(Object carrier) {
      int requiredFieldCount = 0;
      String traceId = null, spanId = null;
      Map<String,String> decodedBaggage = null;
      Iterator<Map.Entry<String,String>> entries = ((TextMapReader)carrier).getEntries();
      while (entries.hasNext()) {
        Map.Entry<String,String> entry = entries.next();
        switch (entry.getKey()) {
          case FIELD_NAME_TRACE_ID: {
            requiredFieldCount++;
            traceId = entry.getValue();
            break;
          }
          case FIELD_NAME_SPAN_ID: {
            requiredFieldCount++;
            spanId = entry.getValue();
            break;
          }
          default: {
            String key = entry.getKey();
            if (key.startsWith(PREFIX_BAGGAGE)) {
              if (decodedBaggage == null) {
                decodedBaggage = new HashMap<String,String>();
                decodedBaggage.put(key.substring(PREFIX_BAGGAGE.length()), entry.getValue());
              }
            }
          }
        }
      }
      if (requiredFieldCount == 0) {
        return null;
      } else if (requiredFieldCount < 2) {
        // TODO: log a warning via the AbstractTracer?
        return null;
      }

      // Success.
      return new SpanContext(traceId, spanId, decodedBaggage);
    }
  };

  // TODO: HTTP_HEADER presently blindly delegates to
  // TEXT_MAP; adopt BasicTracer's HTTP carrier encoding once it's
  // been defined.
  static final Propagator HTTP_HEADER = new Propagator() {
    public boolean matchesInjectCarrier(Object carrier) {
      return carrier instanceof HttpHeaderWriter;
    }
    public boolean matchesExtractCarrier(Object carrier) {
      return carrier instanceof HttpHeaderReader;
    }
    public void inject(SpanContext spanContext, Object carrier) {
      final HttpHeaderWriter headerWriter = (HttpHeaderWriter)carrier;
      TEXT_MAP.inject(spanContext, new TextMapWriter() {
        public void put(String key, String value) {
          headerWriter.put(key, value);
        };
      });
    }
    public SpanContext extract(Object carrier) {
      final HttpHeaderReader headerReader = (HttpHeaderReader)carrier;
      return TEXT_MAP.extract(new TextMapReader() {
        public Iterator<Map.Entry<String,String>> getEntries() {
          return headerReader.getEntries();
        };
      });
    }
  };

  static final Propagator BINARY = new Propagator() {
    public boolean matchesInjectCarrier(Object carrier) {
      return carrier instanceof ByteBuffer;
    }
    public boolean matchesExtractCarrier(Object carrier) {
      return carrier instanceof ByteBuffer;
    }
    public void inject(SpanContext spanContext, Object carrier) {
      // XXX: implement
    }
    public SpanContext extract(Object carrier) {
      // XXX: implement
      return null;
    }
  };
}
