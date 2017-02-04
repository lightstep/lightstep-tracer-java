package com.lightstep.tracer.shared;

import com.google.protobuf.Timestamp;
import com.lightstep.tracer.grpc.InternalMetrics;
import com.lightstep.tracer.grpc.MetricsSample;
import java.util.ArrayList;

/**
 * Tracks client metrics for internal purposes.
 */
class ClientMetrics {

    /**
     * For capacity allocation purposes, keep this in sync with the number of counts actually being
     * tracked.
     */
    private static final int NUMBER_OF_COUNTS = 1;
    long spansDropped;
    final Timestamp startTime;


    ClientMetrics(Timestamp startTime) {
        spansDropped = 0;
        this.startTime = startTime;
    }

    ClientMetrics(ClientMetrics that) {
        spansDropped = that.spansDropped;
        startTime = that.startTime;
    }

    void merge(ClientMetrics that) {
        if (that == null) {
            return;
        }
        spansDropped += that.spansDropped;
    }

    InternalMetrics toGrpc() {
        ArrayList<MetricsSample> counts = new ArrayList<>(NUMBER_OF_COUNTS);
        counts.add(MetricsSample.newBuilder().setName("spans.dropped")
            .setIntValue(spansDropped).build());
        return InternalMetrics.newBuilder().addAllCounts(counts).build();

    }
}
