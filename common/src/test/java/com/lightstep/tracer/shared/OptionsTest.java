package com.lightstep.tracer.shared;

import org.junit.Test;

import static com.lightstep.tracer.shared.Options.COLLECTOR_PATH;
import static com.lightstep.tracer.shared.Options.COMPONENT_NAME_KEY;
import static com.lightstep.tracer.shared.Options.DEFAULT_PLAINTEXT_PORT;
import static com.lightstep.tracer.shared.Options.DEFAULT_SECURE_PORT;
import static com.lightstep.tracer.shared.Options.GUID_KEY;
import static com.lightstep.tracer.shared.Options.HTTP;
import static com.lightstep.tracer.shared.Options.HTTPS;
import static com.lightstep.tracer.shared.Options.LEGACY_COMPONENT_NAME_KEY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_DEBUG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OptionsTest {

    private static final String ACCESS_TOKEN = "my-access-token";
    private static final String COLLECTOR_HOST = "my-collector-host";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String COMPONENT_NAME = "my-component";
    private static final int MAX_REPORTING_INTERVAL_MILLIS = 1001;
    private static final int MAX_BUFFERED_SPANS = 999;
    private static final String TAG_KEY = "my-tag-key";
    private static final String TAG_VALUE = "my-tag-value";
    private static final long GUID_VALUE = 123;

    /**
     * Basic test of OptionsBuilder that ensures if I set everything explicitly, that these values
     * are propagated to the Options object.
     */
    @Test
    public void testOptionsBuilder() throws Exception {
        Options options = createFullyPopulatedOptions();
        validateFullyPopulatedOptions(options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_invalidProtocol() {
        new Options.OptionsBuilder()
                .withCollectorProtocol("bogus");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_nullCollectorHost() {
        new Options.OptionsBuilder()
                .withCollectorHost(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_whitespaceCollectorHost() {
        new Options.OptionsBuilder()
                .withCollectorHost("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_emptyCollectorHost() {
        new Options.OptionsBuilder()
                .withCollectorHost("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_zeroCollectorPort() {
        new Options.OptionsBuilder()
                .withCollectorPort(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOptionsBuilder_negativeCollectorPort() {
        new Options.OptionsBuilder()
                .withCollectorPort(-1);
    }

    @Test
    public void testOptionsBuilder_httpsNoPortProvided() throws Exception {
        Options options = new Options.OptionsBuilder()
                .withCollectorProtocol(HTTPS)
                .build();

        assertEquals(DEFAULT_SECURE_PORT, options.collectorUrl.getPort());
    }

    @Test
    public void testOptionsBuilder_httpNoPortProvided() throws Exception {
        Options options = new Options.OptionsBuilder()
                .withCollectorProtocol(HTTP)
                .build();

        assertEquals(DEFAULT_PLAINTEXT_PORT, options.collectorUrl.getPort());
    }

    @Test
    public void testOptionsBuilder_fromExistingOptions() throws Exception {
        // create Options object with values configured
        Options options = createFullyPopulatedOptions();

        // create a new Options object from the other, all the values should be the same
        Options.OptionsBuilder builder = new Options.OptionsBuilder(options);
        Options newOptions = builder.build();
        assertNotSame(newOptions, options);
        validateFullyPopulatedOptions(newOptions);

        // verify that if we change something, it is changed in resulting Options
        Options modifiedOptions = builder.withMaxReportingIntervalMillis(222).build();
        assertEquals(222, modifiedOptions.maxReportingIntervalMillis);
    }

    @Test
    public void testOptionsBuilder_noComponentName() throws Exception {
        Options options = new Options.OptionsBuilder().build();
        Object componentName = options.tags.get(COMPONENT_NAME_KEY);
        assertNotNull(componentName);

        // the actual value will vary depending on how the tests are run, but should always
        // contain 'Main'
        assertTrue("Unexpected sun.java.command value", componentName.toString().contains("Main"));

        Object legacyComponentName = options.tags.get(LEGACY_COMPONENT_NAME_KEY);
        assertEquals(componentName, legacyComponentName);
    }

    @Test
    public void testOptionsBuilder_noGuid() throws Exception {
        Options options = new Options.OptionsBuilder().build();
        assertNotEquals(0L, options.getGuid());
    }

    @Test
    public void testSetDefaultReportingIntervalMillis_alreadySet() throws Exception {
        Options oldOptions = createFullyPopulatedOptions();
        Options newOptions = oldOptions.setDefaultReportingIntervalMillis(111);
        assertSame(oldOptions, newOptions);
        assertEquals(MAX_REPORTING_INTERVAL_MILLIS, newOptions.maxReportingIntervalMillis);
    }

    @Test
    public void testSetDefaultReportingIntervalMillis_notSet() throws Exception {
        Options oldOptions = new Options.OptionsBuilder().build();
        Options newOptions = oldOptions.setDefaultReportingIntervalMillis(111);
        assertNotSame(oldOptions, newOptions);
        assertEquals(111, newOptions.maxReportingIntervalMillis);
    }

    private Options createFullyPopulatedOptions() throws Exception {
        return new Options.OptionsBuilder()
                .withVerbosity(VERBOSITY_DEBUG)
                .withAccessToken(ACCESS_TOKEN)
                .withCollectorPort(123)
                .withCollectorHost(COLLECTOR_HOST)
                .withCollectorProtocol(HTTPS_PROTOCOL)
                .withComponentName(COMPONENT_NAME)
                .withDisableReportingLoop(true)
                .withResetClient(true)
                .withClockSkewCorrection(false)
                .withMaxReportingIntervalMillis(MAX_REPORTING_INTERVAL_MILLIS)
                .withMaxBufferedSpans(MAX_BUFFERED_SPANS)
                .withTag(TAG_KEY, TAG_VALUE)
                .withTag(GUID_KEY, GUID_VALUE)
                .build();
    }

    private void validateFullyPopulatedOptions(Options options) {
        assertEquals(VERBOSITY_DEBUG, options.verbosity);
        assertEquals(ACCESS_TOKEN, options.accessToken);
        assertEquals("https://my-collector-host:123" + COLLECTOR_PATH,
                options.collectorUrl.toString());
        assertEquals(COMPONENT_NAME, options.tags.get(COMPONENT_NAME_KEY));
        assertTrue(options.disableReportingLoop);
        assertTrue(options.resetClient);
        assertFalse(options.useClockCorrection);
        assertEquals(MAX_REPORTING_INTERVAL_MILLIS, options.maxReportingIntervalMillis);
        assertEquals(MAX_BUFFERED_SPANS, options.maxBufferedSpans);
        assertEquals(TAG_VALUE, options.tags.get(TAG_KEY));
        assertEquals(GUID_VALUE, options.getGuid());
    }
}