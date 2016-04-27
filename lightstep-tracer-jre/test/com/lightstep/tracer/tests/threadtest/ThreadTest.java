package io.traceguide.tests.ThreadTest;

import java.util.concurrent.*;
import java.util.ArrayList; 
import io.traceguide.instrument.*;
import io.traceguide.instrument.runtime.*;

public class ThreadTest {
    // ---------
    // CONSTANTS
    // ---------
    private static final int MYTHREADS = 15;
    private static final int MYSPANS = 1000;
    private static final int MYLOGS = 1000;  
    static ExecutorService pool = Executors.newFixedThreadPool(MYTHREADS);

    // ----------------
    // Callable CLASSES
    // ----------------
    public static class CallableLog implements Callable<String> {
        private final String log;
        private final io.traceguide.instrument.Runtime runtime;

        // Constructor
        CallableLog(String log, io.traceguide.instrument.Runtime runtime) {
            this.log = log;
            this.runtime = runtime;
        }

        @Override
        public String call() {
            try {
                runtime.log(log);
                String result = "Thread id: " + Thread.currentThread().getId() + " submitted " + log;
                return result;
            } catch (Exception e) {
                String result = "Error: " + log + "was not transmitted correctly.";
                return result;
            }
        }
    }

    public static class CallableSpan implements Callable<String> {
        private final String spanName;
        private final io.traceguide.instrument.Runtime runtime;

        // Constructor
        CallableSpan(String spanName, io.traceguide.instrument.Runtime runtime) {
            this.spanName = spanName;
            this.runtime = runtime;
        }

        @Override
        public String call() {
            try {
                ActiveSpan span = runtime.span(spanName);
                String result = "Thread id: " + Thread.currentThread().getId() + " submitted " + spanName;
                span.addJoinId("end_user_id", "Sahil");
                span.end();
                return result;
            } catch (Exception e) {
                String result = "Error: " + spanName + "was not transmitted correctly.";
                return result;
            }
        }
    }

    // -----
    // TESTS
    // -----

    public static void testLogs(io.traceguide.instrument.Runtime runtime) {
        // Launch log requests across multiple threads
        ArrayList<Future<String>> logFutures = new ArrayList<Future<String>>(MYLOGS);
        for (int i = 0; i < MYLOGS; i++) {
            Callable<String> callableLog = new CallableLog("Log " + i, runtime);
            logFutures.add(pool.submit(callableLog));
        }

        // Print out results from the log requests
        for (Future<String> logFuture : logFutures) {
            try {
                System.out.println(logFuture.get());
            } catch (ExecutionException ee) {
                System.err.println("Callable through exception: " + ee.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void testSpans(io.traceguide.instrument.Runtime runtime) {
        // Launch span requests across multiple threads
        ArrayList<Future<String>> spanFutures = new ArrayList<Future<String>>(MYSPANS);
        for (int i = 0; i < MYSPANS; i++) {
            Callable<String> callableSpan = new CallableSpan("Span: " + i, runtime);
            spanFutures.add(pool.submit(callableSpan));
        }

        // Print out results from the span requests
        for (Future<String> spanFuture : spanFutures) {
            try {
                System.out.println(spanFuture.get());
            } catch (ExecutionException ee) {
                System.err.println("Callable through exception: " + ee.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main (String[] args) throws Exception{
        // Initialize runtime
        io.traceguide.instrument.Runtime runtime = io.traceguide.instrument.runtime.JavaRuntime.getInstance();
        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");  

        if (success) {
            testLogs(runtime);
            testSpans(runtime); 
        }

        pool.shutdown();
    }
}