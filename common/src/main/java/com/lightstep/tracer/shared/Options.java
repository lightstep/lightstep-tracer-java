package com.lightstep.tracer.shared;

import java.util.HashMap;
import java.util.Map;

import static com.lightstep.tracer.shared.AbstractTracer.COMPONENT_NAME_KEY;

/**
 * Options control behaviors specific to the LightStep tracer.
 */
public final class Options implements Cloneable {

    /**
     * all internal log statements, including debugging details
     */
    public static final int VERBOSITY_DEBUG = 4;

    /**
     * all errors, warnings, and info statements are echoed locally
     */
    public static final int VERBOSITY_INFO = 3;

    /**
     * all errors are echoed locally
     */
    public static final int VERBOSITY_ERRORS_ONLY = 2;

    /**
     * only the first error encountered will be echoed locally
     */
    public static final int VERBOSITY_FIRST_ERROR_ONLY = 1;

    /**
     * never produce local output
     */
    public static final int VERBOSITY_NONE = 0;

    /**
     * The unique identifier for this application.
     */
    String accessToken;

    /**
     * The host to which the tracer will send data.  If null, the default will be used.
     */
    String collectorHost;

    /**
     * The port to which the tracer will send data.  If 0, the default will be
     * used.
     */
    int collectorPort;

    /**
     * Encryption describes methods of protecting data sent from the tracer to
     * the collector.
     */
    public enum Encryption {
        /**
         * Use TLS to encrypt sent data to the collector.
         */
        TLS,
        /**
         * Do not use encryption to protect data sent to the collector.  This
         * should only be used when the tracer and collector are running on a
         * trusted network.
         */
        NONE
    }

    /**
     * Determines how the tracer communicates with the collector.
     */
    Encryption collectorEncryption = Encryption.TLS;

    /**
     * User-defined key-value pairs that should be associated with all of the
     * data produced by this tracer.
     */
    Map<String, Object> tags = new HashMap<>();

    /**
     * UNSUPPORTED API: intended for unit testing purposes only and should
     * not be used in production code.
     *
     * Production control of limits will be via a throughput-based limit on
     * resource use.
     */
    int maxBufferedSpans;

    /**
     * Maximum interval between reports. If zero, the default will be used.
     */
    int maxReportingIntervalMillis;

    /**
     * Controls the amount of local output produced by the tracer.  It does not
     * affect which spans are sent to the collector.  It is useful for
     * diagnosing problems in the tracer itself. The default value is 1.
     *
     * 0 - never produce local output
     * 1 - only the first error encountered will be echoed locally
     * 2 - all errors are echoed locally
     * 3 - all errors, warnings, and info statements are echoed locally
     * 4 - all internal log statements, including debugging details
     */
    int verbosity;

    /**
     * If true, the background reporting loop will be disabled. Reports will
     * only occur on explicit calls to Flush();
     */
    boolean disableReportingLoop;

    /**
     * If true, the library will *not* attempt an automatic report at process exit.
     */
    boolean disableReportOnExit;

    public Options(String accessToken) {
        this.accessToken = accessToken;
        this.verbosity = 1;
        this.disableReportingLoop = false;
        this.disableReportOnExit = false;
    }

    @SuppressWarnings("unused")
    public Options withAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    @SuppressWarnings("unused")
    public Options withCollectorHost(String collectorHost) {
        this.collectorHost = collectorHost;
        return this;
    }

    @SuppressWarnings("unused")
    public Options withCollectorPort(int collectorPort) {
        this.collectorPort = collectorPort;
        return this;
    }

    @SuppressWarnings("unused")
    public Options withCollectorEncryption(Encryption collectorEncryption) {
        this.collectorEncryption = collectorEncryption;
        return this;
    }

    public Options withComponentName(String name) {
        return this.withTag(COMPONENT_NAME_KEY, name);
    }

    @SuppressWarnings("WeakerAccess")
    public Options withTag(String key, Object value) {
        this.tags.put(key, value);
        return this;
    }

    @SuppressWarnings("WeakerAccess")
    public Options withMaxReportingIntervalMillis(int maxReportingIntervalMillis) {
        this.maxReportingIntervalMillis = maxReportingIntervalMillis;
        return this;
    }

    public Options withVerbosity(int verbosity) {
        this.verbosity = verbosity;
        return this;
    }

    @SuppressWarnings("unused")
    public Options withDisableReportingLoop(boolean disable) {
        this.disableReportingLoop = true;
        return this;
    }

    @SuppressWarnings("unused")
    public Options withDisableReportOnExit(boolean disable) {
        this.disableReportOnExit = true;
        return this;
    }

    public Options clone() {
        try {
            return (Options) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * UNSUPPORTED API: intended for unit testing purposes only.
     */
    @SuppressWarnings("unused")
    public Options withMaxBufferedSpans(int max) {
        this.maxBufferedSpans = max;
        return this;
    }
}
