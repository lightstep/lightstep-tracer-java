package com.lightstep.tracer.jre;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class TracerParametersTest {

    final static String ACCESS_TOKEN = "1234567890";
    final static String COMPONENT_NAME = "mycomponent";
    final static String COLLECTOR_CLIENT = "grpc";
    final static String COLLECTOR_HOST = "127.0.0.1";
    final static String COLLECTOR_PROTOCOL = "http";
    final static String COLLECTOR_PORT = "666";
    final static String METRICS_URL = "https://localhost/metrics";
    final static String DISABLE_METRICS = "true";

    @Before
    public void beforeTest() {
        // Clear all the parameters.
        System.clearProperty(Configuration.CONFIGURATION_FILE_KEY);
        for (String paramName: TracerParameters.ALL)
            System.clearProperty(paramName);
    }

    @Test
    public void testHideString() {
        assertNull(TracerParameters.hideString(null));
        assertEquals("", TracerParameters.hideString(""));
        assertEquals("X", TracerParameters.hideString("a"));
        assertEquals("XX", TracerParameters.hideString("ab"));
        assertEquals("aXc", TracerParameters.hideString("abc"));
        assertEquals("aXXd", TracerParameters.hideString("abcd"));
        assertEquals("aXXXe", TracerParameters.hideString("abcde"));
        assertEquals("1XXXXXXX9", TracerParameters.hideString("123456789"));
    }

    @Test
    public void getParameters_fromSystemProperties() {
        System.setProperty(TracerParameters.ACCESS_TOKEN, ACCESS_TOKEN);
        System.setProperty(TracerParameters.COMPONENT_NAME, COMPONENT_NAME);
        System.setProperty(TracerParameters.COLLECTOR_CLIENT, COLLECTOR_CLIENT);
        System.setProperty(TracerParameters.COLLECTOR_HOST, COLLECTOR_HOST);
        System.setProperty(TracerParameters.COLLECTOR_PROTOCOL, COLLECTOR_PROTOCOL);
        System.setProperty(TracerParameters.COLLECTOR_PORT, COLLECTOR_PORT);
        System.setProperty(TracerParameters.METRICS_URL, METRICS_URL);
        System.setProperty(TracerParameters.DISABLE_METRICS_REPORTING, DISABLE_METRICS);

        assertValidParameters(TracerParameters.getParameters());
    }

    @Test
    public void getParameters_fromConfigurationFile() throws Exception {
        Properties props = new Properties();
        props.setProperty(TracerParameters.ACCESS_TOKEN, ACCESS_TOKEN);
        props.setProperty(TracerParameters.COMPONENT_NAME, COMPONENT_NAME);
        props.setProperty(TracerParameters.COLLECTOR_CLIENT, COLLECTOR_CLIENT);
        props.setProperty(TracerParameters.COLLECTOR_HOST, COLLECTOR_HOST);
        props.setProperty(TracerParameters.COLLECTOR_PROTOCOL, COLLECTOR_PROTOCOL);
        props.setProperty(TracerParameters.COLLECTOR_PORT, COLLECTOR_PORT);
        System.setProperty(TracerParameters.METRICS_URL, METRICS_URL);
        System.setProperty(TracerParameters.DISABLE_METRICS_REPORTING, DISABLE_METRICS);

        File file = null;
        try {
            file = Utils.savePropertiesToTempFile(props);
            System.setProperty(Configuration.CONFIGURATION_FILE_KEY, file.getAbsolutePath());

            assertValidParameters(TracerParameters.getParameters());

        } finally {
            if (file != null)
                file.delete();
        }
    }

    static void assertValidParameters(Map<String, String> params) {
        assertNotNull(params);
        assertEquals(ACCESS_TOKEN, params.get(TracerParameters.ACCESS_TOKEN));
        assertEquals(COMPONENT_NAME, params.get(TracerParameters.COMPONENT_NAME));
        assertEquals(COLLECTOR_CLIENT, params.get(TracerParameters.COLLECTOR_CLIENT));
        assertEquals(COLLECTOR_HOST, params.get(TracerParameters.COLLECTOR_HOST));
        assertEquals(COLLECTOR_PROTOCOL, params.get(TracerParameters.COLLECTOR_PROTOCOL));
        assertEquals(COLLECTOR_PORT, params.get(TracerParameters.COLLECTOR_PORT));
        assertEquals(METRICS_URL, params.get(TracerParameters.METRICS_URL));
        assertEquals(DISABLE_METRICS, params.get(TracerParameters.DISABLE_METRICS_REPORTING));
    }
}
