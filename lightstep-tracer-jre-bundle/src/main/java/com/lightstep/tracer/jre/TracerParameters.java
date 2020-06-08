package com.lightstep.tracer.jre;

import com.google.common.base.Strings;
import com.lightstep.tracer.shared.B3Propagator;
import com.lightstep.tracer.shared.Options;
import io.opentracing.propagation.Format;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TracerParameters {
    private TracerParameters() {}

    private final static Logger logger = Logger.getLogger(TracerParameters.class.getName());

    final static String HTTP = "http";
    final static String HTTPS = "https";

    final static String DEFAULT_COLLECTOR_HOST = "collector.lightstep.com";
    final static String DEFAULT_COLLECTOR_PROTOCOL = HTTPS;
    final static int DEFAULT_COLLECTOR_PORT = 443;

    final static String VALUES_SEPARATOR = ",";
    final static String ASSIGN_CHAR = "=";

    // TODO: add metaEventLogging, propagator, scopeManager.
    public final static String ACCESS_TOKEN = "ls.accessToken";
    public final static String CLOCK_SKEW_CORRECTION = "ls.clockSkewCorrection";
    public final static String COMPONENT_NAME = "ls.componentName";
    public final static String COLLECTOR_CLIENT = "ls.collectorClient";
    public final static String COLLECTOR_HOST = "ls.collectorHost";
    public final static String COLLECTOR_PORT = "ls.collectorPort";
    public final static String COLLECTOR_PROTOCOL = "ls.collectorProtocol";
    public final static String DEADLINE_MILLIS = "ls.deadlineMillis";
    public final static String DISABLE_REPORTING_LOOP = "ls.disableReportingLoop";
    public final static String MAX_BUFFERED_SPANS = "ls.maxBufferedSpans";
    public final static String MAX_REPORTING_INTERVAL_MILLIS = "ls.maxReportingIntervalMillis";
    public final static String RESET_CLIENT = "ls.resetClient";
    public final static String VERBOSITY = "ls.verbosity";
    public final static String TAGS = "ls.tags";
    public final static String PROPAGATOR = "ls.propagator";
    public final static String SERVICE_VERSION = "ls.serviceVersion";
    public final static String DISABLE_METRICS_REPORTING = "ls.disableMetricsReporting";
    public final static String METRICS_URL = "ls.metricsUrl";

    public final static String [] ALL = {
        ACCESS_TOKEN,
        CLOCK_SKEW_CORRECTION,
        COMPONENT_NAME,
        COLLECTOR_CLIENT,
        COLLECTOR_HOST,
        COLLECTOR_PORT,
        COLLECTOR_PROTOCOL,
        DEADLINE_MILLIS,
        DISABLE_REPORTING_LOOP,
        MAX_BUFFERED_SPANS,
        MAX_REPORTING_INTERVAL_MILLIS,
        RESET_CLIENT,
        VERBOSITY,
        TAGS,
        PROPAGATOR,
        SERVICE_VERSION,
        DISABLE_METRICS_REPORTING,
        METRICS_URL
    };

    // NOTE: we could probably make this prettier
    // if we could use Java 8 Lambdas ;)
    public static Options.OptionsBuilder getOptionsFromParameters(Options.OptionsBuilder optionsBuilder) {
        Map<String, String> params = getParameters();
        if (!params.containsKey(ACCESS_TOKEN))
            return null;

        Options.OptionsBuilder opts = optionsBuilder
            .withAccessToken(params.get(ACCESS_TOKEN));

        // As we use the okhttp collector, do override default values properly:
        opts
            .withCollectorHost(DEFAULT_COLLECTOR_HOST)
            .withCollectorProtocol(DEFAULT_COLLECTOR_PROTOCOL)
            .withCollectorPort(DEFAULT_COLLECTOR_PORT);

        if (params.containsKey(CLOCK_SKEW_CORRECTION))
            opts.withClockSkewCorrection(toBoolean(params.get(CLOCK_SKEW_CORRECTION)));

        if (params.containsKey(COMPONENT_NAME))
            opts.withComponentName(params.get(COMPONENT_NAME));

        if (params.containsKey(COLLECTOR_CLIENT)) {
            String value = params.get(COLLECTOR_CLIENT);
            for (Options.CollectorClient client : Options.CollectorClient.values()) {
                if (client.name().toLowerCase().equals(value)) {
                    opts.withCollectorClient(client);
                }
            }
        }

        if (params.containsKey(COLLECTOR_HOST)) {
            String value = params.get(COLLECTOR_HOST);
            if (validateNonEmptyString(value))
                opts.withCollectorHost(value);
        }

        if (params.containsKey(COLLECTOR_PROTOCOL)) {
            String value = params.get(COLLECTOR_PROTOCOL);
            if (validateProtocol(value))
                opts.withCollectorProtocol(value);
        }

        if (params.containsKey(COLLECTOR_PORT)) {
            Integer value = toInteger(params.get(COLLECTOR_PORT));
            if (validatePort(value))
                opts.withCollectorPort(value);
        }

        if (params.containsKey(DEADLINE_MILLIS)) {
            Long value = toLong(params.get(DEADLINE_MILLIS));
            if (value != null)
                opts.withDeadlineMillis(value);
        }

        if (params.containsKey(DISABLE_REPORTING_LOOP))
            opts.withDisableReportingLoop(toBoolean(params.get(DISABLE_REPORTING_LOOP)));

        if (params.containsKey(MAX_BUFFERED_SPANS)) {
            Integer value = toInteger(params.get(MAX_BUFFERED_SPANS));
            if (value != null)
                opts.withMaxBufferedSpans(value);
        }

        if (params.containsKey(MAX_REPORTING_INTERVAL_MILLIS)) {
            Integer value = toInteger(params.get(MAX_REPORTING_INTERVAL_MILLIS));
            if (value != null)
                opts.withMaxReportingIntervalMillis(value);
        }

        if (params.containsKey(RESET_CLIENT))
            opts.withResetClient(toBoolean(params.get(RESET_CLIENT)));

        if (params.containsKey(VERBOSITY)) {
            Integer value = toInteger(params.get(VERBOSITY));
            if (value != null)
                opts.withVerbosity(value);
        }

        if (params.containsKey(TAGS)) {
            Map<String, Object> tags = toMap(params.get(TAGS));
            for (Map.Entry<String, Object> entry : tags.entrySet()) {
                opts.withTag(entry.getKey(), entry.getValue());
            }
        }

        if (params.containsKey(PROPAGATOR)) {
            String  propagator = params.get(PROPAGATOR);
            if ("b3".equalsIgnoreCase(propagator)) {
                opts.withPropagator(Format.Builtin.HTTP_HEADERS, new B3Propagator());
            }
        }

        if (params.containsKey(SERVICE_VERSION)) {
            String serviceVersion = params.get(SERVICE_VERSION);
            if (validateNonEmptyString(serviceVersion))
                opts.withServiceVersion(serviceVersion);
        }

        if (params.containsKey(DISABLE_METRICS_REPORTING)) {
            Boolean disableMetrics = toBoolean(params.get(DISABLE_METRICS_REPORTING));
            opts.withDisableMetricsReporting(disableMetrics);
        }

        if (params.containsKey(METRICS_URL)) {
            String metricsUrl = params.get(METRICS_URL);
            if (validateNonEmptyString(metricsUrl))
                opts.withMetricsUrl(metricsUrl);
        }

        return opts;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, String> getParameters() {
        Properties props = Configuration.loadConfigurationFile();
        loadSystemProperties(props);

        for (String propName : props.stringPropertyNames()) {
            String value = props.getProperty(propName);
            if (ACCESS_TOKEN.equals(propName) && value != null && value.length() >= 2) {
                value = hideString(value);
            }
            logger.log(Level.INFO, "Retrieved Tracer parameter " + propName + "=" + value);
        }

        // A Properties object is expected to only contain String keys/values.
        return (Map)props;
    }

    /**
     * Replace all characters by 'X' except first and last. E.g. 'abcde' becomes 'aXXXe'. If input
     * string has one or two characters then return 'X' or 'XX' respectively.
     */
    static String hideString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        if (input.length() <= 2) {
            return Strings.repeat("X", input.length());
        }

        return new StringBuilder(input).replace(1, input.length() - 1,
            Strings.repeat("X", input.length() - 2))
            .toString();
    }

    static void loadSystemProperties(Properties props) {
        for (String paramName: ALL) {
            String paramValue = System.getProperty(paramName);
            if (paramValue != null)
                props.setProperty(paramName, paramValue);
        }
    }

    static Integer toInteger(String value) {
        Integer integer = null;
        try {
            integer = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Failed to convert Tracer parameter value '" + value + "' to int");
        }

        return integer;
    }

    private static Long toLong(String value) {
        Long l = null;
        try {
            l = Long.valueOf(value);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Failed to convert Tracer parameter value '" + value + "' to long");
        }

        return l;
    }

    private static Boolean toBoolean(String value) {
        return Boolean.valueOf(value);
    }

    private static Map<String, Object> toMap(String value) {
        Map<String, Object> tagMap = new HashMap<>();

        for (String part : value.split(VALUES_SEPARATOR)) {
            String [] tagParts = part.split(ASSIGN_CHAR);
            if (tagParts.length != 2) {
                logger.log(Level.WARNING, "Failed to detect tag value '" + part + "'");
                continue;
            }

            tagMap.put(tagParts[0].trim(), parseStringValue(tagParts[1].trim()));
        }

        return tagMap;
    }

    private static boolean validateProtocol(String value) {
        if (!HTTPS.equals(value) && !HTTP.equals(value)) {
            logger.log(Level.WARNING, "Failed to validate protocol value '" + value + "'");
            return false;
        }

        return true;
    }

    private static boolean validatePort(Integer value) {
        if (value == null || value <= 0) {
            logger.log(Level.WARNING, "Failed to validate port value '" + value + "'");
            return false;
        }

        return true;
    }

    private static boolean validateNonEmptyString(String value) {
        if (value == null || value.trim().length() == 0) {
            logger.log(Level.WARNING, "Failed to validate Tracer parameter as non-empty String");
            return false;
        }

        return true;
    }

    // Try to detect the value from the tag. No warnings must be issued.
    private static Object parseStringValue(String value) {
        // Boolean (Boolean.parseBoolean() only detects 'true' properly).
        if (value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (value.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }

        // Long.
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
        }

        // Double.
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
        }

        // Fallback to String.
        return value;
    }
}
