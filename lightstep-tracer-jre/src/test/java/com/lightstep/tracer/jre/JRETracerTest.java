package com.lightstep.tracer.jre;

import com.lightstep.tracer.grpc.KeyValue;
import com.lightstep.tracer.grpc.Span.Builder;
import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.shared.Status;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.TextMapExtractAdapter;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.opentracing.propagation.Format.Builtin.HTTP_HEADERS;
import static org.junit.Assert.*;

public class JRETracerTest {
    private static final String PREFIX_TRACER_STATE = "ot-tracer-";

    static final String FIELD_NAME_TRACE_ID = PREFIX_TRACER_STATE + "traceid";
    static final String FIELD_NAME_SPAN_ID = PREFIX_TRACER_STATE + "spanid";

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
                        try(Scope activeScope = tracer.buildSpan("test_span").startActive(true)) {
                            SpanContext ctx = activeScope.span().context();
                            long id = ((com.lightstep.tracer.shared.SpanContext) ctx).getSpanId();
                            assertEquals(m.containsKey(id), false);
                            m.put(Long.toHexString(id), true);
                        }
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

        Span span = tracer.buildSpan("test_span").startManual();
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
                .startManual();
        span.finish();

        assertSpanHasTag(span, "my_key", "my_value");
    }

    @Test
    public void activeSpanTryWithResources() throws Exception {
        Tracer tracer = new JRETracer(
                new Options.OptionsBuilder().withAccessToken("{your_access_token}").build());

       try(Scope activeScope = tracer
                .buildSpan("test_span")
                .startActive(true)) {
           activeScope.span().setTag("test", "test");
           assertNotNull(tracer.scopeManager().active());
       }
       assertNull(tracer.scopeManager().active());
    }

    @Test
    public void spansDroppedCounterTest() throws Exception {
        JRETracer tracer = new JRETracer(
                new Options.OptionsBuilder()
                        .withAccessToken("{your_access_token}")
                        .build());

        Status status = tracer.status();
        assertEquals(status.getSpansDropped(), 0);
        for (int i = 0; i < Options.DEFAULT_MAX_BUFFERED_SPANS; i++) {
            try(Scope ignored = tracer.buildSpan("test_span").startActive(true)){}
        }
        status = tracer.status();
        assertEquals(status.getSpansDropped(), 0);
        for (int i = 0; i < 10; i++) {
            try(Scope ignored = tracer.buildSpan("test_span").startActive(true)){}
        }
        status = tracer.status();
        assertEquals(status.getSpansDropped(), 10);
    }

    @Test
    public void extractOnOpenTracingTracer() throws Exception {
        JRETracer tracer = new JRETracer(
                new Options.OptionsBuilder().withAccessToken("{your_access_token}").build());

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(FIELD_NAME_TRACE_ID, "1");
        headerMap.put(FIELD_NAME_SPAN_ID, "123");
        SpanContext parentCtx = tracer.extract(HTTP_HEADERS, new TextMapExtractAdapter(headerMap));

        try(Scope ignored = tracer.buildSpan("test_span")
                .asChildOf(parentCtx)
                .startActive(true)){}
    }

    private void assertSpanHasTag(Span span, String key, String value) {
        com.lightstep.tracer.shared.Span lsSpan = (com.lightstep.tracer.shared.Span) span;
        Builder record = lsSpan.getGrpcSpan();

        assertNotNull("Tags are currently written the attributes", record.getTagsList());

        boolean found = false;
        for (KeyValue pair : record.getTagsList()) {
            if (pair.getKey().equals(key) && pair.getStringValue().equals(value)) {
                found = true;
            }
        }
        assertEquals(found, true);
    }
}
