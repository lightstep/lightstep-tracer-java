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
   * @deprecated will be replaced by a throughput-based limit on resource use
   */
  @Deprecated
  public int maxBufferedSpans;

  /**
   * Maximum interval between reports. If zero, the default will be used.
   */
  public int maxReportingIntervalSeconds;

  /**
   * Controls the amount of local output produced by the tracer.  It does not
   * affect which spans are sent to the collector.  It is useful for
   * diagnosing problems in the tracer itself.
   */
  public int verbose;

  public Options(String accessToken) {
    this.accessToken = accessToken;
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

  public Options withTag(String key, Object value) {
    this.tags.put(key, value);
    return this;
  }

  public Options withMaxReportingIntervalSeconds(int maxReportingIntervalSeconds) {
    this.maxReportingIntervalSeconds = maxReportingIntervalSeconds;
    return this;
  }
}
