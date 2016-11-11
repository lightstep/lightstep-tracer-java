package com.lightstep.tracer.shared;

import java.util.LinkedList;

import static java.lang.Long.MAX_VALUE;

class ClockState {
    /**
     * How many updates before a sample is considered old. This happens to
     * be one less than the number of samples in our buffer but that's
     * somewhat arbitrary.
     */
    private static final int maxOffsetAge = 7;

    private class Sample {
        long delayMicros;
        long offsetMicros;

        Sample(long delayMicros, long offsetMicros) {
            this.delayMicros = delayMicros;
            this.offsetMicros = offsetMicros;
        }
    }

    private final Object mutex = new Object();
    private LinkedList<Sample> samples;
    private long currentOffsetMicros;
    private int currentOffsetAge;

    ClockState() {
        // The last eight samples, computed from timing information in
        // RPCs.
        samples = new LinkedList<>();
        currentOffsetMicros = 0;

        // How many updates since we've updated currentOffsetMicros.
        currentOffsetAge = maxOffsetAge + 1;

        // Update the current offset based on these data.
        update();
    }

    /**
     * Adds a new timing sample and update the offset.
     */
    void addSample(long originMicros,
                   long receiveMicros,
                   long transmitMicros,
                   long destinationMicros) {
        long latestDelayMicros = MAX_VALUE;
        long latestOffsetMicros = 0;
        // Ensure that all of the data are valid before using them. If not, we'll
        // push a {0, MAX} record into the queue.
        if (originMicros > 0 && receiveMicros > 0 &&
                transmitMicros > 0 && destinationMicros > 0) {
            latestDelayMicros = (destinationMicros - originMicros) -
                    (transmitMicros - receiveMicros);
            latestOffsetMicros = ((receiveMicros - originMicros) +
                    (transmitMicros - destinationMicros)) / 2;
        }

        synchronized (mutex) {
            // Discard the oldest sample and push the new one.
            if (samples.size() == maxOffsetAge + 1) {
                samples.removeFirst();
            }
            samples.push(new Sample(latestDelayMicros, latestOffsetMicros));
            currentOffsetAge++;
            update();
        }
    }

    /**
     * Updates the time offset based on the current samples.
     *
     * NOTE: this method assumes the caller already have locked the clock state.
     */
    private void update() {
        // This is simplified version of the clock filtering in Simple NTP. It
        // ignores precision and dispersion (frequency error). In brief, it keeps
        // the 8 (kMaxOffsetAge+1) most recent delay-offset pairs, and considers
        // the offset with the smallest delay to be the best one. However, it only
        // uses this new offset if the change (relative to the last offset) is
        // small compared to the estimated error.
        //
        // See:
        // https://tools.ietf.org/html/rfc5905#appendix-A.5.2
        // http://books.google.com/books?id=pdTcJBfnbq8C
        //   esp. section 3.5
        // http://www.eecis.udel.edu/~mills/ntp/html/filter.html
        // http://www.eecis.udel.edu/~mills/database/brief/algor/algor.pdf
        // http://www.eecis.udel.edu/~mills/ntp/html/stats.html

        // TODO: Consider huff-n'-puff if the delays are highly asymmetric.
        // http://www.eecis.udel.edu/~mills/ntp/html/huffpuff.html

        // Find the sample with the smallest delay; the corresponding offset is
        // the "best" one.
        long minDelayMicros = MAX_VALUE;
        long bestOffsetMicros = 0;
        for (Sample sample : samples) {
            if (sample.delayMicros < minDelayMicros) {
                minDelayMicros = sample.delayMicros;
                bestOffsetMicros = sample.offsetMicros;
            }
        }

        // No update.
        if (bestOffsetMicros == currentOffsetMicros) {
            return;
        }

        // Now compute the jitter, i.e. the error relative to the new offset were
        // we to use it.
        long jitter = 0;
        for (Sample sample : samples) {
            jitter += Math.pow(bestOffsetMicros - sample.offsetMicros, 2);
        }
        jitter = (long) Math.sqrt(jitter / samples.size());

        // Ignore spikes: only use the new offset if the change is not too
        // large... unless the current offset is too old. The "too old" condition
        // is also triggered when update() is called from the constructor.
        long SGATE = 3; // See RFC 5905
        if (currentOffsetAge > maxOffsetAge ||
                Math.abs(currentOffsetMicros - bestOffsetMicros) < SGATE * jitter) {
            currentOffsetMicros = bestOffsetMicros;
            currentOffsetAge = 0;
        }
    }

    /**
     * Returns the difference in microseconds between the server's clock and our
     * clock. This should be added to any local timestamps before sending them
     * to the server. Note that a negative offset means that the local clock is
     * ahead of the server's.
     */
    long offsetMicros() {
        synchronized (mutex) {
            return currentOffsetMicros;
        }
    }

    /**
     * Returns true if we've performed enough measurements to be confident
     * in the current offset.
     */
    boolean isReady() {
        synchronized (mutex) {
            return samples.size() > 3;
        }
    }

    int activeSampleCount() {
        synchronized (mutex) {
            return samples.size();
        }
    }
}
