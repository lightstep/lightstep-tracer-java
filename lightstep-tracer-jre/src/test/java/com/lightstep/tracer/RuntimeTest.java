package com.lightstep.tracer.jre;

import io.opentracing.Tracer;
import io.opentracing.Span;
import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.thrift.SpanRecord;
import com.lightstep.tracer.thrift.KeyValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class RuntimeTest {

    // Simple placeholder JUnit test
    @Test
    public void sanityCheckTest() {
        int result = 2 + 4;
        assertEquals("Sanity check: 2 + 4 = 6", result, 6);
    }

    @Test
    public void spanSetTagTest() {
        Tracer tracer = new JRETracer(
            new com.lightstep.tracer.shared.Options("{your_access_token}"));

        Span span = tracer.buildSpan("test_span").start();
        span.setTag("my_key", "my_value");
        span.finish();

        this.assertSpanHasTag(span, "my_key", "my_value");
    }

    @Test
    public void spanBuilderWithTagTest() {
        Tracer tracer = new JRETracer(
            new com.lightstep.tracer.shared.Options("{your_access_token}"));

        Span span = tracer
            .buildSpan("test_span")
            .withTag("my_key", "my_value")
            .start();
        span.finish();

        this.assertSpanHasTag(span, "my_key", "my_value");
    }

    protected void assertSpanHasTag(Span span, String key, String value) {
        com.lightstep.tracer.shared.Span lsSpan = (com.lightstep.tracer.shared.Span)span;
        SpanRecord record = lsSpan.thriftRecord();

        assertNotNull("Tags are currently written the attributes", record.attributes);

        boolean found = false;
        for (KeyValue pair : record.attributes) {
            if (pair.Key == key && pair.Value == value) {
                found = true;
            }
        }
        assertEquals(found, true);
    }
}
