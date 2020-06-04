package com.lightstep.tracer.metrics;

import com.lightstep.tracer.shared.SafeMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeMetricsImpl implements SafeMetrics {
  private static final Logger logger = LoggerFactory.getLogger(SafeMetricsImpl.class);
  private static final boolean isJdk17 = System.getProperty("java.version").startsWith("1.7");

  @Override
  public Thread createMetricsThread(final String componentName, final String accessToken,
            final String serviceVersion, final String serviceUrl, final int samplePeriodSeconds) {
    if (isJdk17) {
      logger.warn("Metrics supports jdk1.8+");
      return null;
    }

    // TODO: Can we unify samplePeriodSeconds in a single place?
    final Sender<?,?> sender = new OkHttpSender(componentName, accessToken, serviceVersion,
                serviceUrl, samplePeriodSeconds * 1000, false);
    return new Metrics(sender, samplePeriodSeconds);
  }
}
