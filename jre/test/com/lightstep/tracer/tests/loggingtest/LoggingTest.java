package io.traceguide.tests.LoggingTest;

import io.traceguide.instrument.*;
import io.traceguide.instrument.runtime.*;

public class LoggingTest{
    public static void main(String[] args) {
        // Initialize runtime
        io.traceguide.instrument.Runtime runtime = io.traceguide.instrument.runtime.JavaRuntime.getInstance();
        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");  

        runtime.infof("Hi there %s %s in the year %d", "John", "Smith", 2015);
        runtime.warnf("Hi there %s %s in the year %d", "John", "Smith", 2015);
        runtime.errorf("Hi there %s %s in the year %d", "John", "Smith", 2015);
        // runtime.fatalf("Hi there %s %s in the year %d", "John", "Smith", 2015);

        ActiveSpan span = runtime.span("Testing span logging");
        span.infof("Span: Hi there %s %s in the year %d", "John", "Smith", 2015);
        span.warnf("Span: Hi there %s %s in the year %d", "John", "Smith", 2015);
        span.errorf("Span: Hi there %s %s in the year %d", "John", "Smith", 2015);
        // span.fatalf("Span: Hi there %s %s in the year %d", "John", "Smith", 2015);
        span.end();

    }
}