package com.lightstep.tracer.jre;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;
import io.opentracing.Tracer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LightStepTracerFactoryTest
{
    Tracer tracer;

    @Before
    public void beforeTest() {
        // Clear all the parameters.
        System.clearProperty(Configuration.CONFIGURATION_FILE_KEY);
        for (String paramName: TracerParameters.ALL)
            System.clearProperty(paramName);

        // And set the only required parameter.
        System.setProperty(TracerParameters.ACCESS_TOKEN, "yourtoken");
    }

    @After
    public void afterTest() {
        if (tracer != null)
            ((JRETracer)tracer).close();
    }

    @Test
    public void getTracer_withNoAccessToken() {
        System.clearProperty(TracerParameters.ACCESS_TOKEN);
        tracer = new LightStepTracerFactory().getTracer();
        assertNull(tracer);
    }

    @Test
    public void getTracer_simple() {
        tracer = new LightStepTracerFactory().getTracer();
        assertTrue(tracer instanceof JRETracer);
    }

    @Test
    public void getTracer_withInvalidClockSkewCorrection() {
        System.setProperty(TracerParameters.CLOCK_SKEW_CORRECTION, "invalidbool");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidCollectorHost() {
        System.setProperty(TracerParameters.COLLECTOR_HOST, "   ");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidCollectorProtocol() {
        System.setProperty(TracerParameters.COLLECTOR_PROTOCOL, "ftp");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidCollectorPort() {
        System.setProperty(TracerParameters.COLLECTOR_PORT, "abc");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withNegativeCollectorPort() {
        System.setProperty(TracerParameters.COLLECTOR_PORT, "-5");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidDeadlineMillis() {
        System.setProperty(TracerParameters.DEADLINE_MILLIS, "false");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidDisableReportingLoop() {
        System.setProperty(TracerParameters.DISABLE_REPORTING_LOOP, "abc");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidMaxBufferedSpans() {
        System.setProperty(TracerParameters.MAX_BUFFERED_SPANS, "abc");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidMaxReportingIntervalMillis() {
        System.setProperty(TracerParameters.MAX_REPORTING_INTERVAL_MILLIS, "abc");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidVerbosity() {
        System.setProperty(TracerParameters.VERBOSITY, "false");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidResetClient() {
        System.setProperty(TracerParameters.RESET_CLIENT, "abc");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withEmptyTags() {
        System.setProperty(TracerParameters.TAGS, "");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withInvalidTags() {
        System.setProperty(TracerParameters.TAGS, " ,,invalid,value,,name=value, ");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer); // No errors.
    }

    @Test
    public void getTracer_withSingleTag() {
        System.setProperty(TracerParameters.TAGS, "name=value");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer);
    }

    @Test
    public void getTracer_withTags() {
        System.setProperty(TracerParameters.TAGS, "name=value,name2=value2");
        tracer = new LightStepTracerFactory().getTracer();
        assertNotNull(tracer);
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

            tracer = new LightStepTracerFactory().getTracer();
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
            tracer = new LightStepTracerFactory().getTracer();
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
        tracer = new LightStepTracerFactory().getTracer();
        assertNull(tracer); // No errors.
    }
}
