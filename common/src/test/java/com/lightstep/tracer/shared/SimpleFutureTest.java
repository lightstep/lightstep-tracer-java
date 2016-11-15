package com.lightstep.tracer.shared;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleFutureTest {

    private static final String EXPECTED_VALUE = "expected-value";

    @Test
    public void testGet_futureIsReady() throws Exception {
        SimpleFuture<String> undertest = new SimpleFuture<>(EXPECTED_VALUE);
        assertEquals(EXPECTED_VALUE, undertest.get());
    }

    @Test
    public void testGetWithTimeout_futureIsReady() throws Exception {
        SimpleFuture<String> undertest = new SimpleFuture<>(EXPECTED_VALUE);
        assertEquals(EXPECTED_VALUE, undertest.getWithTimeout(1L));
    }

    @Test
    public void testGet_futureIsNotReady() throws Exception {
        final SimpleFuture<String> undertest = new SimpleFuture<>();

        new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    fail("Unexpected test failure scenario");
                }
                undertest.set(EXPECTED_VALUE);
            }
        }.run();
        assertEquals(EXPECTED_VALUE, undertest.get());
    }

    @Test
    public void testGetWithTimeout_futureIsNotReady() throws Exception {
        SimpleFuture<String> undertest = new SimpleFuture<>();
        assertNull(undertest.getWithTimeout(100L));
    }
}