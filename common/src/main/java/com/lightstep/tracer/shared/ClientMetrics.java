package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.InternalMetrics;
import com.lightstep.tracer.grpc.MetricsSample;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks client metrics for internal purposes.
 */
class ClientMetrics {

    /**
     * For capacity allocation purposes, keep this in sync with the number of counts actually being
     * tracked.
     */
    private static final int NUMBER_OF_COUNTS = 1;
    private final AtomicLong spansDropped;

    ClientMetrics() {
        spansDropped = new AtomicLong(0);
    }

    void dropSpans(int size) {
        spansDropped.addAndGet(size);
    }

    InternalMetrics toGrpcAndReset() {
        long val = spansDropped.getAndSet(0);
        ArrayList<MetricsSample> counts = new ArrayList<>(NUMBER_OF_COUNTS);
        counts.add(MetricsSample.newBuilder().setName("spans.dropped")
                .setIntValue(val).build());
        return InternalMetrics.newBuilder().addAllCounts(counts).build();
    }

    long getSpansDropped() {
        return spansDropped.get();
    }
}
