package com.lightstep.tracer.metrics;

import com.lightstep.tracer.shared.MetricsProvider;
import com.lightstep.tracer.shared.SafeMetrics;

public class MetricsProviderImpl extends MetricsProvider {
    @Override
    public SafeMetrics create() {
        return new SafeMetricsImpl();
    }
}