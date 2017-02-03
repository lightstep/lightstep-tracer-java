package com.lightstep.tracer.shared;

import com.google.protobuf.Timestamp;
import java.util.Random;

class Util {

    static long generateRandomGUID() {
        // Note that ThreadLocalRandom is a singleton, thread safe Random Generator
        return random.get().nextLong();
    }

    /**
     * Thread-specific random number generators. Each is seeded with the thread
     * ID, so the sequence of pseudo-random numbers are unique between threads.
     *
     * See http://stackoverflow.com/questions/2546078/java-random-long-number-in-0-x-n-range
     */
    private static ThreadLocal<Random> random = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            // It'd be nice to get the process ID into the mix, but there's no clear
            // cross-platform, Java 6-compatible way to determine that
            return new Random(
                    System.currentTimeMillis() *
                            (System.nanoTime() % 1000000) *
                            Thread.currentThread().getId() *
                            (long) (1024 * Math.random()));
        }
    };

    static long protoTimeToEpochMicros(Timestamp timestamp) {
        return timestamp.getSeconds() * 1000 * 1000 + timestamp.getNanos() / 1000;
    }

    static Timestamp epochTimeMicrosToProtoTime(long micros) {
        Timestamp.Builder builder = Timestamp.newBuilder();
        builder.setSeconds(micros / 1000 / 1000);
        builder.setNanos( (int) (micros % (1000 * 1000) * 1000));
        return builder.build();
    }
}
