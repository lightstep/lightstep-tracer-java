package com.lightstep.tracer.shared;

import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TextMapPropagatorTest {

    @Test
    public void testExtract_mixedCaseIsLowered() {
        Map<String, String> mixedCaseHeaders = new HashMap<>();

        mixedCaseHeaders.put("Ot-tracer-spanid", Long.toHexString(1));
        mixedCaseHeaders.put("Ot-tracer-traceId", Long.toHexString(2));
        mixedCaseHeaders.put("ot-Tracer-sampled", "true");

        TextMapPropagator subject = new TextMapPropagator();

        SpanContext span = subject.extract(new TextMapExtractAdapter(mixedCaseHeaders));

        assertNotNull(span);
        assertEquals(span.getSpanId(), 1);
        assertEquals(span.getTraceId(), 2);
    }

    @Test
    public void testInjectAndExtractIds() {
        TextMapPropagator undertest = new TextMapPropagator();
        TextMap carrier = new TextMap() {
            final Map<String, String> textMap = new HashMap<>();

            public void put(String key, String value) {
                textMap.put(key, value);
            }

            public Iterator<Map.Entry<String, String>> iterator() {
                return textMap.entrySet().iterator();
            }
        };
        SpanContext spanContext = new SpanContext();
        undertest.inject(spanContext, carrier);

        SpanContext result = undertest.extract(carrier);

        assertEquals(spanContext.getTraceId(), result.getTraceId());
        assertEquals(spanContext.getSpanId(), result.getSpanId());
    }

    @Test
    public void testUnHex() {
        assertEquals(1, TextMapPropagator.unHex("1"));
        assertEquals(-1, TextMapPropagator.unHex(Long.toHexString(-1)));
        assertEquals(Long.MAX_VALUE, TextMapPropagator.unHex(Long.toHexString(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, TextMapPropagator.unHex(Long.toHexString(Long.MIN_VALUE)));
        assertEquals(0, TextMapPropagator.unHex(Long.toHexString(0)));
        long randomLong = Util.generateRandomGUID();
        assertEquals(randomLong, TextMapPropagator.unHex(Long.toHexString(randomLong)));
    }
}
