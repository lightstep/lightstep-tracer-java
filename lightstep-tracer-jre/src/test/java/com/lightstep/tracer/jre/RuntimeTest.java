package com.lightstep.tracer.jre;

import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.shared.Status;
import com.lightstep.tracer.thrift.KeyValue;
import com.lightstep.tracer.thrift.SpanRecord;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.TextMapExtractAdapter;

import static com.lightstep.tracer.shared.AbstractTracer.DEFAULT_MAX_BUFFERED_SPANS;
import static io.opentracing.propagation.Format.Builtin.HTTP_HEADERS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RuntimeTest {

    @Test
    public void tracerHasStandardTags() throws Exception {
        JRETracer tracer = new JRETracer(
                new Options.OptionsBuilder().withAccessToken("{your_access_token}").build());

        Status status = tracer.status();

        // Check standard tags
        assertTrue(status.hasTag("lightstep.component_name"));
        assertTrue(status.hasTag("lightstep.guid"));
        assertEquals("jre", status.getTag("lightstep.tracer_platform"));
        assertTrue(status.hasTag("lightstep.tracer_platform_version"));
        assertTrue(status.hasTag("lightstep.tracer_version"));
        assertFalse(status.hasTag("lightstep.this_doesnt_exist"));
    }

    @Test
    public void tracerSupportsWithComponentName() throws Exception {
        Options options = new Options.OptionsBuilder()
                .withAccessToken("{your_access_token}")
                .withComponentName("my_component")
                .build();
        JRETracer tracer = new JRETracer(options);

        Status status = tracer.status();
        assertEquals("my_component", status.getTag("lightstep.component_name"));
    }

    @Test
    public void tracerOptionsAreSupported() throws Exception {
        // Ensure all the expected option methods are there and support
        // chaining.
        Options options =
                new Options.OptionsBuilder()
                        .withAccessToken("{your_access_token}")
                        .withCollectorHost("localhost")
                        .withCollectorPort(4321)
                        .withCollectorProtocol("https")
                        .withVerbosity(2)
                        .withTag("my_tracer_tag", "zebra_stripes")
                        .withMaxReportingIntervalMillis(30000)
                        .build();

        new JRETracer(options);
    }

    /**
     * Note: this test *can* generate a false-positive, but the probability is
     * so low (the probability of a collision of 8k values in a 2^64 range) that
     * in practice this seems a reasonable way to make sure the GUID generation
     * is not totally broken.
     */
    @Test
    public void spanUniqueGUIDsTestt() throws Exception {
        final JRETracer tracer = new JRETracer(
                new Options.OptionsBuilder().withAccessToken("{your_access_token}").build());

        final ConcurrentHashMap<String, Boolean> m = new ConcurrentHashMap<>();
        Thread[] t = new Thread[8];
        for (int j = 0; j < 8; j++) {
            t[j] = new Thread() {
                public void run() {
                    for (int i = 0; i < 1024; i++) {
                        Span span = tracer.buildSpan("test_span").start();
                        SpanContext ctx = span.context();
                        long id = ((com.lightstep.tracer.shared.SpanContext) ctx).getSpanId();
                        assertEquals(m.containsKey(id), false);
                        m.put(Long.toHexString(id), true);
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
    public void spanSetTagTest() throws Exception {
        final String testStr = ")/forward\\back+%20/<operation>|⅕⚜±♈ 🌠🍕/%%%20%14\n\'\"@+!=#$%$^%   &^() '";

        Tracer tracer = new JRETracer(
                new Options.OptionsBuilder().withAccessToken("{your_access_token}").build());

        Span span = tracer.buildSpan("test_span").start();
        span.setTag("my_key", "my_value");
        span.setTag("key2", testStr);
        span.setTag(testStr, "my_value2");
        span.finish();

        assertSpanHasTag(span, "my_key", "my_value");
        assertSpanHasTag(span, "key2", testStr);
        assertSpanHasTag(span, testStr, "my_value2");
    }

    @Test
    public void spanBuilderWithTagTest() throws Exception {
        Tracer tracer = new JRETracer(
                new Options.OptionsBuilder().withAccessToken("{your_access_token}").build());

        Span span = tracer
                .buildSpan("test_span")
                .withTag("my_key", "my_value")
                .start();
        span.finish();

        assertSpanHasTag(span, "my_key", "my_value");
    }

    @Test
    public void spansDroppedCounterTest() throws Exception {
        JRETracer tracer = new JRETracer(
                new Options.OptionsBuilder()
                        .withAccessToken("{your_access_token}")
                        .build());

        Status status = tracer.status();
        assertEquals(status.getSpansDropped(), 0);
        for (int i = 0; i < DEFAULT_MAX_BUFFERED_SPANS; i++) {
            Span span = tracer.buildSpan("test_span").start();
            span.finish();
        }
        status = tracer.status();
        assertEquals(status.getSpansDropped(), 0);
        for (int i = 0; i < 10; i++) {
            Span span = tracer.buildSpan("test_span").start();
            span.finish();
        }
        status = tracer.status();
        assertEquals(status.getSpansDropped(), 10);
    }

    @Test
    public void extractOnOpenTracingTracer() throws Exception {
        JRETracer tracer = new JRETracer(
                new Options.OptionsBuilder().withAccessToken("{your_access_token}").build());

        Map<String, String> headerMap = new HashMap<>();
        SpanContext parentCtx = tracer.extract(HTTP_HEADERS, new TextMapExtractAdapter(headerMap));

        Span span = tracer.buildSpan("test_span")
                .asChildOf(parentCtx)
                .start();
        span.finish();
    }

    private void assertSpanHasTag(Span span, String key, String value) {
        com.lightstep.tracer.shared.Span lsSpan = (com.lightstep.tracer.shared.Span) span;
        SpanRecord record = lsSpan.getRecord();

        assertNotNull("Tags are currently written the attributes", record.attributes);

        boolean found = false;
        for (KeyValue pair : record.attributes) {
            if (pair.Key.equals(key) && pair.Value.equals(value)) {
                found = true;
            }
        }
        assertEquals(found, true);
    }
}
