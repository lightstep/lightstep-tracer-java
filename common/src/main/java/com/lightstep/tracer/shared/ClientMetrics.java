package com.lightstep.tracer.shared;

import java.util.ArrayList;

import com.lightstep.tracer.thrift.Metrics;
import com.lightstep.tracer.thrift.MetricsSample;

public class ClientMetrics {

    // For capacity allocation purposes, keep this in sync with the number of
    // counts actually being tracked.
    private static final int NUMBER_OF_COUNTS = 1;

    public long spansDropped;

    public ClientMetrics() {
        this.spansDropped = 0;
    }

    public ClientMetrics(ClientMetrics that) {
        this.spansDropped = that.spansDropped;
    }

    public void merge(ClientMetrics that) {
        if (that == null) {
            return;
        }
        this.spansDropped += that.spansDropped;
    }

    public Metrics toThrift() {
        ArrayList<MetricsSample> counts = new ArrayList<MetricsSample>(NUMBER_OF_COUNTS);
        counts.add(new MetricsSample("spans.dropped").setInt64_value(spansDropped));
        return new Metrics().setCounts(counts);
    }
}
