package com.lightstep.tracer.shared;

import java.util.LinkedList;

public class ClockState {
  /** How many updates before a sample is considered old. This happens to
   * be one less than the number of samples in our buffer but that's
   * somewhat arbitrary. */
  private static final int maxOffsetAge = 7;

  private static final long storedSamplesTTLMicros = 60 * 60 * 1000 * 1000; // 1 hour

  private class Sample {
    long delayMicros;
    long offsetMicros;
    Sample (long delayMicros, long offsetMicros) {
      this.delayMicros = delayMicros;
      this.offsetMicros = offsetMicros;
    }
  }

  private LinkedList<Sample> samples;
  private long currentOffsetMicros;
  private int currentOffsetAge;

  ClockState () {
    // The last eight samples, computed from timing information in
    // RPCs.
    this.samples = new LinkedList<Sample>();
    this.currentOffsetMicros = 0;

    // How many updates since we've updated currentOffsetMicros.
    this.currentOffsetAge = maxOffsetAge + 1;

    // Update the current offset based on these data.
    this.update();
  }

  /**
   * Adds a new timing sample and update the offset.
   */
  void addSample(long originMicros,
                 long receiveMicros,
                 long transmitMicros,
                 long destinationMicros) {
    long latestDelayMicros = Long.MAX_VALUE;
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

    // Discard the oldest sample and push the new one.
    if (this.samples.size() == maxOffsetAge+1) {
      this.samples.removeFirst();
    }
    this.samples.push(new Sample(latestDelayMicros, latestOffsetMicros));
    this.currentOffsetAge++;
    this.update();
  }

  /**
   * Updates the time offset based on the current samples.
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
    long minDelayMicros = Long.MAX_VALUE;
    long bestOffsetMicros = 0;
    for (Sample sample : this.samples) {
      if (sample.delayMicros < minDelayMicros) {
        minDelayMicros = sample.delayMicros;
        bestOffsetMicros = sample.offsetMicros;
      }
    }

    // No update.
    if (bestOffsetMicros == this.currentOffsetMicros) {
      return;
    }

    // Now compute the jitter, i.e. the error relative to the new offset were
    // we to use it.
    long jitter = 0;
    for (Sample sample : this.samples) {
      jitter += Math.pow(bestOffsetMicros - sample.offsetMicros, 2);
    }
    jitter = (long) Math.sqrt(jitter / this.samples.size());

    // Ignore spikes: only use the new offset if the change is not too
    // large... unless the current offset is too old. The "too old" condition
    // is also triggered when update() is called from the constructor.
    long SGATE = 3; // See RFC 5905
    if (this.currentOffsetAge > maxOffsetAge ||
        Math.abs(this.currentOffsetMicros - bestOffsetMicros) < SGATE * jitter) {
      this.currentOffsetMicros = bestOffsetMicros;
      this.currentOffsetAge = 0;
    }
  }

  /**
   * Returns the difference in microseconds between the server's clock and our
   * clock. This should be added to any local timestamps before sending them
   * to the server. Note that a negative offset means that the local clock is
   * ahead of the server's.
   */
  long offsetMicros () {
    return this.currentOffsetMicros;
  }

  /**
   * Returns true if we've performed enough measurements to be confident
   * in the current offset.
   */
  boolean isReady () {
    return this.samples.size() > 3;
  }

  int activeSampleCount () {
    return this.samples.size();
  }
}
