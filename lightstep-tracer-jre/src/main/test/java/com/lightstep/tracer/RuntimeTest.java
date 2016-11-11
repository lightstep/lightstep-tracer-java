package com.lightstep.tracer.jre;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.SpanRecord;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;

import static com.lightstep.tracer.shared.Options.Encryption.NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static io.opentracing.propagation.Format.Builtin.HTTP_HEADERS;

public class RuntimeTest {

    @Test
    public void tracerHasStandardTags() throws Exception {
        JRETracer tracer = new JRETracer(
                new Options("{your_access_token}"));

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
    public void tracerSupportsWithComponentName() throws Exception {
        JRETracer tracer = new JRETracer(
                new Options("{your_access_token}")
                        .withComponentName("my_component"));

        JRETracer.Status status = tracer.status();
        assertEquals(status.tags.get("lightstep.component_name"), "my_component");
    }

    @Test
    public void tracerOptionsAreSupported() {
        // Ensure all the expected option methods are there and support
        // chaining.
        Options options =
                new Options("{your_access_token}")
                        .withCollectorHost("localhost")
                        .withCollectorPort(4321)
                        .withCollectorEncryption(NONE)
                        .withVerbosity(2)
                        .withTag("my_tracer_tag", "zebra_stripes")
                        .withMaxReportingIntervalMillis(30000);

        JRETracer tracer = new JRETracer(options);
    }

    /**
     * Note: this test *can* generate a false-positive, but the probability is
     * so low (the probability of a collision of 8k values in a 2^64 range) that
     * in practice this seems a reasonable way to make sure the GUID generation
     * is not totally broken.
     */
    @Test
    public void spanUniqueGUIDsTestt() {
        final Tracer tracer = new JRETracer(
                new Options("{your_access_token}"));

        final ConcurrentHashMap m = new ConcurrentHashMap();
        Thread[] t = new Thread[8];
        for (int j = 0; j < 8; j++) {
            t[j] = new Thread() {
                public void run() {
                    for (int i = 0; i < 1024; i++) {
                        Span span = tracer.buildSpan("test_span").start();
                        SpanContext ctx = span.context();
                        String id = ((com.lightstep.tracer.shared.SpanContext) ctx).getSpanId();
                        assertEquals(m.containsKey(id), false);
                        m.put(id, true);
                        span.finish();
                    }
                }
            };
        }
        for (int j = 0; j < 8; j++) {
            t[j].start();
        }
        for (int j = 0; j < 8; j++) {
            try {
                t[j].join();
            } catch (InterruptedException ie) {
                assertEquals(true, false);
            }
        }
    }

    @Test
    public void spanSetTagTest() {
        final String testStr = ")/forward\\back+%20/<operation>|⅕⚜±♈ 🌠🍕/%%%20%14\n\'\"@+!=#$%$^%   &^() '";

        Tracer tracer = new JRETracer(
                new Options("{your_access_token}"));

        Span span = tracer.buildSpan("test_span").start();
        span.setTag("my_key", "my_value");
        span.setTag("key2", testStr);
        span.setTag(testStr, "my_value2");
        span.finish();

        this.assertSpanHasTag(span, "my_key", "my_value");
        this.assertSpanHasTag(span, "key2", testStr);
        this.assertSpanHasTag(span, testStr, "my_value2");
    }

    @Test
    public void spanBuilderWithTagTest() {
        Tracer tracer = new JRETracer(
                new Options("{your_access_token}"));

        Span span = tracer
                .buildSpan("test_span")
                .withTag("my_key", "my_value")
                .start();
        span.finish();

        this.assertSpanHasTag(span, "my_key", "my_value");
    }

    @Test
    public void spansDroppedCounterTest() {
        JRETracer tracer = new JRETracer(
                new Options("{your_access_token}")
                        .withMaxBufferedSpans(10));

        JRETracer.Status status = tracer.status();
        assertEquals(status.clientMetrics.spansDropped, 0);
        for (int i = 0; i < 10; i++) {
            Span span = tracer.buildSpan("test_span").start();
            span.finish();
        }
        status = tracer.status();
        assertEquals(status.clientMetrics.spansDropped, 0);
        for (int i = 0; i < 10; i++) {
            Span span = tracer.buildSpan("test_span").start();
            span.finish();
        }
        status = tracer.status();
        assertEquals(status.clientMetrics.spansDropped, 10);
    }

    @Test
    public void extractOnOpenTracingTracer() {
        final io.opentracing.Tracer tracer = new JRETracer(
                new Options("{your_access_token}"));

        Map<String, String> headerMap = new HashMap<String, String>();
        SpanContext parentCtx = tracer.extract(HTTP_HEADERS, new TextMapExtractAdapter(headerMap));

        Span span = tracer.buildSpan("test_span")
                .asChildOf(parentCtx)
                .start();
        span.finish();
    }

    protected void assertSpanHasTag(Span span, String key, String value) {
        com.lightstep.tracer.shared.Span lsSpan = (com.lightstep.tracer.shared.Span) span;
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
