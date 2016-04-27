package io.traceguide.tests.SpansLogsTest;

import io.traceguide.instrument.*;
import io.traceguide.instrument.runtime.*;

public class SpansLogsTest{
    public static void main(String[] args) {
        // Initialize runtime
        io.traceguide.instrument.Runtime runtime = io.traceguide.instrument.runtime.JavaRuntime.getInstance();
        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");  

        if (success) {
            for (int i = 0; i < 100; i++) {
                System.out.println("Current Number: " + Integer.toString(i));
                try {
                    ActiveSpan span = runtime.span("Span" + Integer.toString(i));
                    span.addJoinId("end_user_id", "Sahil");
                    runtime.log("Just created a span for " + Integer.toString(i));
                    Thread.sleep(500);
                    span.end();
                    runtime.log("Just ended a span for " + Integer.toString(i));
                } catch  (InterruptedException ex) {
                    runtime.log("Encountered a Thread InterruptedException");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}