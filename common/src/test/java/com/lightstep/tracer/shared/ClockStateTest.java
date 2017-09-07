package com.lightstep.tracer.shared;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClockStateTest {

    private static final long OFFSET_MICROS = 2;
    private static final long SEND_DURATION_MICROS = 4;
    private static final long ORIGIN_MICROS = 1;
    private static final long RECEIVE_MICROS = ORIGIN_MICROS + OFFSET_MICROS + SEND_DURATION_MICROS;
    private static final long TRANSMIT_MICROS = RECEIVE_MICROS;
    private static final long DESTINATION_MICROS = ORIGIN_MICROS + SEND_DURATION_MICROS * 2;
    private static final long MIN_SAMPLE_SIZE = 3;
    private ClockState clockState;

    @Test
    public void testNotReady() {
        clockState = new ClockState();
        assertFalse(clockState.isReady());
        for (int i = 0; i < MIN_SAMPLE_SIZE; i++) {
            clockState.addSample(ORIGIN_MICROS, RECEIVE_MICROS, TRANSMIT_MICROS, DESTINATION_MICROS);
        }
        assertFalse(clockState.isReady());
        clockState.addSample(ORIGIN_MICROS, RECEIVE_MICROS, TRANSMIT_MICROS, DESTINATION_MICROS);
        assertTrue(clockState.isReady());
    }

    @Test
    public void testClockCorrection() {
        clockState = new ClockState();
        for (int i = 0; i < MIN_SAMPLE_SIZE + 1; i++) {
            clockState.addSample(ORIGIN_MICROS, RECEIVE_MICROS, TRANSMIT_MICROS, DESTINATION_MICROS);
        }
        assertTrue(clockState.isReady());
        assertEquals(MIN_SAMPLE_SIZE + 1, clockState.activeSampleCount());
        assertEquals(OFFSET_MICROS, clockState.offsetMicros());
    }

    @Test
    public void testNoClockCorrection() {
        clockState = new ClockState.NoopClockState();
        for (int i = 0; i < MIN_SAMPLE_SIZE + 1; i++) {
            clockState.addSample(ORIGIN_MICROS, RECEIVE_MICROS, TRANSMIT_MICROS, DESTINATION_MICROS);
        }
        assertTrue(clockState.isReady());
        assertEquals(0, clockState.activeSampleCount());
        assertEquals(0, clockState.offsetMicros());
    }

    @Test
    public void testActiveSampleCount() {
        clockState = new ClockState();
        for (int i = 0; i < MIN_SAMPLE_SIZE + 1; i++) {
            assertEquals(i, clockState.activeSampleCount());
            clockState.addSample(ORIGIN_MICROS, RECEIVE_MICROS, TRANSMIT_MICROS, DESTINATION_MICROS);
        }
    }
}
