package com.lightstep.tracer.jre;

import java.io.PrintWriter;
import java.util.Map;

import io.opentracing.Tracer;
import io.opentracing.Span;
import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.thrift.SpanRecord;
import com.lightstep.tracer.thrift.KeyValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class RuntimeTest {

    @Test
    public void tracerHasStandardTags() throws Exception {
        JRETracer tracer = new JRETracer(
            new com.lightstep.tracer.shared.Options("{your_access_token}"));

        JRETracer.Status status = tracer.status();

        // Check standard tags
        assertEquals(status.tags.containsKey("lightstep.component_name"), true);
        assertEquals(status.tags.containsKey("lightstep.guid"), true);
        assertEquals(status.tags.get("lightstep.tracer_platform"), "jre");
        assertEquals(status.tags.containsKey("lightstep.tracer_platform_version"), true);
        assertEquals(status.tags.containsKey("lightstep.tracer_version"), true);
        assertEquals(status.tags.containsKey("lightstep.this_doesnt_exist"), false);
    }

    @Test
    public void tracerOptionsAreSupported() {
        // Ensure all the expected option methods are there and support
        // chaining.
        com.lightstep.tracer.shared.Options options =
            new com.lightstep.tracer.shared.Options("{your_access_token}")
                .withCollectorHost("localhost")
                .withCollectorPort(4321)
                .withCollectorEncryption(com.lightstep.tracer.shared.Options.Encryption.NONE)
                .withVerbosity(2)
                .withTag("my_tracer_tag", "zebra_stripes")
                .withMaxReportingIntervalSeconds(30);

        JRETracer tracer = new JRETracer(options);
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
