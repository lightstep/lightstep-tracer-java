package com.lightstep.tracer.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * Options control behaviors specific to the LightStep tracer.
 */
public class Options {

  /**
   * The unique identifier for this application.
   */
  public String accessToken;

  /**
   * The host to which the tracer will send data.  If null, the default will be used.
   */
  public String collectorHost;

  /**
   * The port to which the tracer will send data.  If 0, the default will be
   * used.
   */
  public int collectorPort;

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
  public Encryption collectorEncryption = Encryption.TLS;

  /**
   * User-defined key-value pairs that should be associated with all of the
   * data produced by this tracer.
   */
  public Map<String, Object> tags = new HashMap<String, Object>();

  /**
   * UNSUPPORTED API: intended for unit testing purposes only and should
   * not be used in production code.
   *
   * Production control of limits will be via a throughput-based limit on
   * resource use.
   */
  public int maxBufferedSpans;

  /**
   * Maximum interval between reports. If zero, the default will be used.
   */
  public int maxReportingIntervalSeconds;

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
  public int verbosity;

  public Options(String accessToken) {
    this.accessToken = accessToken;
    this.verbosity = 1;
  }

  public Options withAccessToken(String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

  public Options withCollectorHost(String collectorHost) {
    this.collectorHost = collectorHost;
    return this;
  }

  public Options withCollectorPort(int collectorPort) {
    this.collectorPort = collectorPort;
    return this;
  }

  public Options withCollectorEncryption(Encryption collectorEncryption) {
    this.collectorEncryption = collectorEncryption;
    return this;
  }

  public Options withComponentName(String name) {
    return this.withTag(AbstractTracer.COMPONENT_NAME_KEY, name);
  }

  public Options withTag(String key, Object value) {
    this.tags.put(key, value);
    return this;
  }

  public Options withMaxReportingIntervalSeconds(int maxReportingIntervalSeconds) {
    this.maxReportingIntervalSeconds = maxReportingIntervalSeconds;
    return this;
  }

  public Options withVerbosity(int verbosity) {
    this.verbosity = verbosity;
    return this;
  }

  /**
   * UNSUPPORTED API: intended for unit testing purposes only.
   */
  public Options withMaxBufferedSpans(int max) {
    this.maxBufferedSpans = max;
    return this;
  }
}
