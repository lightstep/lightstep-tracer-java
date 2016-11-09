package com.lightstep.tracer.shared;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.propagation.TextMap;

public interface Propagator<C> {
  void inject(SpanContext spanContext, C carrier);
  SpanContext extract(C carrier);



  // The three supported Propagators.
  static final Propagator<TextMap> TEXT_MAP = new Propagator<TextMap>() {
    private static final String PREFIX_TRACER_STATE = "ot-tracer-";
    private static final String PREFIX_BAGGAGE      = "ot-baggage-";

    private static final int TRACER_STATE_FIELD_COUNT = 3;
    private static final String FIELD_NAME_TRACE_ID   = PREFIX_TRACER_STATE + "traceid";
    private static final String FIELD_NAME_SPAN_ID    = PREFIX_TRACER_STATE + "spanid";
    private static final String FIELD_NAME_SAMPLED    = PREFIX_TRACER_STATE + "sampled";

    public void inject(SpanContext spanContext, final TextMap carrier) {
      carrier.put(FIELD_NAME_TRACE_ID, spanContext.getTraceId());
      carrier.put(FIELD_NAME_SPAN_ID, spanContext.getSpanId());
      carrier.put(FIELD_NAME_SAMPLED, "true");
      for (Map.Entry<String, String> e : spanContext.baggageItems()) {
        carrier.put(PREFIX_BAGGAGE + e.getKey(), e.getValue());
      }
    }

    public SpanContext extract(TextMap carrier) {
      int requiredFieldCount = 0;
      String traceId = null, spanId = null;
      Map<String,String> decodedBaggage = null;

      for (Map.Entry<String,String> entry : carrier) {
        final String key = entry.getKey();
        if (key == FIELD_NAME_TRACE_ID) {
          requiredFieldCount++;
          traceId = entry.getValue();
        } else if (key == FIELD_NAME_SPAN_ID) {
          requiredFieldCount++;
          spanId = entry.getValue();
        } else {
          if (key.startsWith(PREFIX_BAGGAGE)) {
            if (decodedBaggage == null) {
              decodedBaggage = new HashMap<String,String>();
              decodedBaggage.put(key.substring(PREFIX_BAGGAGE.length()), entry.getValue());
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

  // TODO: HTTP_HEADERS presently blindly delegates to
  // TEXT_MAP; adopt BasicTracer's HTTP carrier encoding once it's
  // been defined.
  static final Propagator<TextMap> HTTP_HEADERS = new Propagator<TextMap>() {
    public void inject(SpanContext spanContext, TextMap carrier) {
      TEXT_MAP.inject(spanContext, carrier);
    }
    public SpanContext extract(TextMap carrier) {
      return TEXT_MAP.extract(carrier);
    }
  };

  static final Propagator<ByteBuffer> BINARY = new Propagator<ByteBuffer>() {
    public void inject(SpanContext spanContext, ByteBuffer carrier) {
      // TODO: implement
    }
    public SpanContext extract(ByteBuffer carrier) {
      // TODO: implement
      return null;
    }
  };
}
