package com.lightstep.tracer.jre;

import com.lightstep.tracer.shared.B3Propagator;
import com.lightstep.tracer.shared.LightStepConstants;
import com.lightstep.tracer.shared.Options;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LightStepTracerFactoryTest {
    public static final String TOKEN = "yourtoken";

    Tracer tracer;
    private final Options.OptionsBuilder optionsBuilder = Mockito.spy(new Options.OptionsBuilder());
    private String expectedToken = TOKEN;

    @Before
    public void beforeTest() {
        // Clear all the parameters.
        System.clearProperty(Configuration.CONFIGURATION_FILE_KEY);
        for (String paramName : TracerParameters.ALL)
            System.clearProperty(paramName);

        // And set the only required parameter.
        System.setProperty(TracerParameters.ACCESS_TOKEN, TOKEN);
    }

    @After
    public void afterTest() throws MalformedURLException {
        if (expectedToken != null) {
            Mockito.verify(optionsBuilder).withAccessToken(expectedToken);
            Mockito.verify(optionsBuilder).withCollectorHost(TracerParameters.DEFAULT_COLLECTOR_HOST);
            Mockito.verify(optionsBuilder).withCollectorPort(TracerParameters.DEFAULT_COLLECTOR_PORT);
            Mockito.verify(optionsBuilder).withCollectorProtocol(TracerParameters.DEFAULT_COLLECTOR_PROTOCOL);
            Mockito.verify(optionsBuilder).withAccessToken(expectedToken);
            Mockito.verify(optionsBuilder).withComponentName(Mockito.anyString()); //dependent on the test runtime
            Mockito.verify(optionsBuilder).withTag(Mockito.eq(LightStepConstants.Tags.COMPONENT_NAME_KEY), Mockito.anyString()); //dependent on the test runtime
            Mockito.verify(optionsBuilder).withTag(Mockito.eq(LightStepConstants.Tags.GUID_KEY), Mockito.anyLong()); //random
            Mockito.verify(optionsBuilder).build();
        }

        Mockito.verifyNoMoreInteractions(optionsBuilder);
    }

    @Test
    public void getTracer_withNoAccessToken() {
        System.clearProperty(TracerParameters.ACCESS_TOKEN);
        expectedToken = null;

        tracer = createTracer();
        assertNull(tracer);
    }

    @Test
    public void getTracer_simple() {
        tracer = createTracer();
        assertTrue(tracer instanceof JRETracer);
    }

    @Test
    public void getTracer_withInvalidClockSkewCorrection() {
        System.setProperty(TracerParameters.CLOCK_SKEW_CORRECTION, "invalidbool");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withClockSkewCorrection(false);
    }

    @Test
    public void getTracer_withComponent() {
        System.setProperty(TracerParameters.COMPONENT_NAME, "comp");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withComponentName("comp");
    }

    @Test
    public void getTracer_withCollectorHost() {
        System.setProperty(TracerParameters.COLLECTOR_HOST, "example.com");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withCollectorHost("example.com");
    }

    @Test
    public void getTracer_withInvalidCollectorHost() {
        System.setProperty(TracerParameters.COLLECTOR_HOST, "   ");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withCollectorProtocol() {
        System.setProperty(TracerParameters.COLLECTOR_PROTOCOL, "http");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withCollectorProtocol("http");
    }

    @Test
    public void getTracer_withInvalidCollectorProtocol() {
        System.setProperty(TracerParameters.COLLECTOR_PROTOCOL, "ftp");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withCollectorPort() {
        System.setProperty(TracerParameters.COLLECTOR_PORT, "22");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withCollectorPort(22);
    }

    @Test
    public void getTracer_withInvalidCollectorPort() {
        System.setProperty(TracerParameters.COLLECTOR_PORT, "abc");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withNegativeCollectorPort() {
        System.setProperty(TracerParameters.COLLECTOR_PORT, "-5");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withDeadlineMillis() {
        System.setProperty(TracerParameters.DEADLINE_MILLIS, "22");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withDeadlineMillis(22);
    }

    @Test
    public void getTracer_withInvalidDeadlineMillis() {
        System.setProperty(TracerParameters.DEADLINE_MILLIS, "false");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidDisableReportingLoop() {
        System.setProperty(TracerParameters.DISABLE_REPORTING_LOOP, "abc");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withDisableReportingLoop(false);
    }

    @Test
    public void getTracer_withMaxBufferedSpans() {
        System.setProperty(TracerParameters.MAX_BUFFERED_SPANS, "22");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withMaxBufferedSpans(22);
    }

    @Test
    public void getTracer_withInvalidMaxBufferedSpans() {
        System.setProperty(TracerParameters.MAX_BUFFERED_SPANS, "abc");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withMaxReportingIntervalMillis() {
        System.setProperty(TracerParameters.MAX_REPORTING_INTERVAL_MILLIS, "22");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withMaxReportingIntervalMillis(22);
    }

    @Test
    public void getTracer_withInvalidMaxReportingIntervalMillis() {
        System.setProperty(TracerParameters.MAX_REPORTING_INTERVAL_MILLIS, "abc");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withVerbosity() {
        System.setProperty(TracerParameters.VERBOSITY, "3");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withVerbosity(3);
    }

    @Test
    public void getTracer_withInvalidVerbosity() {
        System.setProperty(TracerParameters.VERBOSITY, "false");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidResetClient() {
        System.setProperty(TracerParameters.RESET_CLIENT, "abc");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withResetClient(false);
    }

    @Test
    public void getTracer_withEmptyTags() {
        System.setProperty(TracerParameters.TAGS, "");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidTags() {
        System.setProperty(TracerParameters.TAGS, " ,,invalid,value,,name=value, ");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withTag("name", "value");
    }

    @Test
    public void getTracer_withSingleTag() {
        System.setProperty(TracerParameters.TAGS, "name=value");
        tracer = createTracer();
        assertNotNull(tracer);

        Mockito.verify(optionsBuilder).withTag("name", "value");
    }

    @Test
    public void getTracer_withTags() {
        System.setProperty(TracerParameters.TAGS, "name=value,name2=false,name3=3,name4=4.0,");
        tracer = createTracer();
        assertNotNull(tracer);

        Mockito.verify(optionsBuilder).withTag("name", "value");
        Mockito.verify(optionsBuilder).withTag("name2", false);
        Mockito.verify(optionsBuilder).withTag("name3", 3L);
        Mockito.verify(optionsBuilder).withTag("name4", 4.0);
    }

    @Test
    public void getTracer_withPropagator() {
        System.setProperty(TracerParameters.PROPAGATOR, "b3");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.

        Mockito.verify(optionsBuilder).withPropagator(Mockito.eq(Format.Builtin.HTTP_HEADERS), Mockito.any(B3Propagator.class));
    }

    @Test
    public void getTracer_withInvalidPropagator() {
        System.setProperty(TracerParameters.PROPAGATOR, "false");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidMetricsUrl() {
        System.setProperty(TracerParameters.METRICS_URL, "");
        tracer = createTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_ConfigurationFile() throws Exception {
        System.clearProperty(TracerParameters.ACCESS_TOKEN);

        Properties props = new Properties();
        props.setProperty(TracerParameters.ACCESS_TOKEN, "yourtoken");

        File file = null;
        try {
            file = Utils.savePropertiesToTempFile(props);
            System.setProperty(Configuration.CONFIGURATION_FILE_KEY, file.getAbsolutePath());

            tracer = createTracer();
            assertNotNull(tracer);

        } finally {
            if (file != null)
                file.delete();
        }
    }

    @Test
    public void getTracer_ConfigurationFilePropertyOverride() throws Exception {
        File file = null;
        try {
            // Have an empty configuration file.
            file = Utils.savePropertiesToTempFile(new Properties());
            System.setProperty(Configuration.CONFIGURATION_FILE_KEY, file.getAbsolutePath());

            // Should get a Tracer, as access token exists as a system property.
            tracer = createTracer();
            assertNotNull(tracer);

        } finally {
            if (file != null)
                file.delete();
        }
    }

    @Test
    public void getTracer_ConfigurationFileInvalid() {
        System.clearProperty(TracerParameters.ACCESS_TOKEN);

        System.setProperty(Configuration.CONFIGURATION_FILE_KEY, "/tmp/doesnotexist.123");
        tracer = createTracer();
        assertNull(tracer); // No errors.

        expectedToken = null;
    }

    private Tracer createTracer() {
        return new LightStepTracerFactory() {
            @Override
            protected Options.OptionsBuilder createOptionsBuilder() {
                return optionsBuilder;
            }
        }.getTracer();
    }
}
