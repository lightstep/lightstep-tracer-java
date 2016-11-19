package com.lightstep.tracer.shared;

import io.opentracing.propagation.TextMapExtractAdapter;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;

public class TextMapPropagatorTest {

    @Test
    public void testExtract_mixedCaseIsLowered() {
        Map<String, String> mixedCaseHeaders = new HashMap<>();

        mixedCaseHeaders.put("Ot-tracer-spanid", "spanid");
        mixedCaseHeaders.put("Ot-tracer-traceId", "traceid");
        mixedCaseHeaders.put("ot-Tracer-sampled", "true");

        TextMapPropagator subject = new TextMapPropagator();

        SpanContext span = subject.extract(new TextMapExtractAdapter(mixedCaseHeaders));

        assertNotNull(span);
        assertEquals(span.getSpanId(), "spanid");
        assertEquals(span.getTraceId(), "traceid");
    }
}
