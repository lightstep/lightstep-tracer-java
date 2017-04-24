package com.lightstep.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import io.opentracing.NoopTracerFactory;
import io.opentracing.Span;
import io.opentracing.Tracer;

class BenchmarkClient {
    private static final long PRIME_WORK = 982451653;

    private static final String LOG_PAYLOAD_STR;

    static {
        StringBuilder b = new StringBuilder();
        for (long i = 0; i < 1 << 20; i++) {
            b.append('A');
        }
        LOG_PAYLOAD_STR = b.toString();
    }

    private final String baseUrl;
    private final JRETracer testTracer;
    private final ObjectMapper objectMapper;

    private BenchmarkClient(JRETracer testTracer, String baseUrl) {
        this.baseUrl = baseUrl;
        this.testTracer = testTracer;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Members must be public for Jackson.
     */
    @SuppressWarnings("WeakerAccess")
    private static class Control {
        public int Concurrent;
        public long Work;
        public long Repeat;
        public long Sleep;
        public long SleepInterval;
        public long BytesPerLog;
        public long NumLogs;
        public boolean Trace;
        public boolean Exit;
        public boolean Profile;
    }

    private static class Result {
        double runTime;
        double flushTime;
        long sleepNanos;
        long answer;
    }

    private static class OneThreadResult {
        long sleepNanos;
        long answer;
    }

    private InputStream getUrlReader(String path) {
        try {
            URL u = new URL(baseUrl + path);
            URLConnection c = u.openConnection();
            c.setDoOutput(true);
            c.setDoInput(true);
            c.getOutputStream().close();
            return c.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T postGetJson(String path, Class<T> cl) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getUrlReader(path)))) {
            return objectMapper.readValue(br, cl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Control getControl() {
        return postGetJson("/control", Control.class);
    }

    private void postResult(Result r) {
        StringBuilder rurl = new StringBuilder("/result?timing=");
        rurl.append(r.runTime);
        rurl.append("&flush=");
        rurl.append(r.flushTime);
        rurl.append("&s=");
        rurl.append(r.sleepNanos / 1e9);
        rurl.append("&a=");
        rurl.append(r.answer);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getUrlReader(rurl.toString())))) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long work(long n) {
        long x = PRIME_WORK;
        for (; n != 0; --n) {
            x *= PRIME_WORK;
        }
        return x;
    }

    private OneThreadResult testBody(Control c, Tracer t) {
        OneThreadResult r = new OneThreadResult();
        r.sleepNanos = 0;

        long sleepDebt = 0;

        for (long i = 0; i < c.Repeat; i++) {
            Span span = t.buildSpan("span/test").start();
            r.answer = work(c.Work);

            for (long l = 0; l < c.NumLogs; l++) {
                span.log("testlog", LOG_PAYLOAD_STR.substring(0, (int) c.BytesPerLog));
            }

            span.finish();
            sleepDebt += c.Sleep;

            if (sleepDebt <= c.SleepInterval) {
                continue;
            }

            long beginSleep = System.nanoTime();
            try {
                long millis = sleepDebt / 1000000;
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                // do nothing
            }

            long endSleep = System.nanoTime();
            long slept = endSleep - beginSleep;
            sleepDebt -= slept;
            r.sleepNanos += slept;
        }

        return r;
    }

    private Result runTest(final Control c) {
        final Tracer tracer;
        if (c.Trace) {
            tracer = testTracer;
        } else {
            tracer = NoopTracerFactory.create();
        }

        System.gc();

        Result res = new Result();
        int conc = c.Concurrent;

        final ArrayList<OneThreadResult> results = new ArrayList<>();
        long beginTest = System.nanoTime();

        if (conc == 1) {
            results.add(testBody(c, tracer));
        } else {
            ArrayList<Thread> threads = new ArrayList<>();
            for (int i = 0; i < conc; i++) {
                Thread th = new Thread() {
                    public void run() {
                        OneThreadResult tr = testBody(c, tracer);
                        synchronized (results) {
                            results.add(tr);
                        }
                    }
                };
                th.start();
                threads.add(th);
            }
            for (Thread th : threads) {
                try {
                    th.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        long endTest = System.nanoTime();
        if (c.Trace) {
            ((JRETracer) tracer).flush(Long.MAX_VALUE);
            res.flushTime = (System.nanoTime() - endTest) / 1e9;
        }

        res.runTime = (endTest - beginTest) / 1e9;
        res.sleepNanos = 0;
        for (OneThreadResult r1 : results) {
            res.sleepNanos += r1.sleepNanos;
            res.answer += r1.answer;
        }
        return res;
    }

    private void loop() {
        while (true) {
            Control c = getControl();

            if (c.Exit) {
                return;
            }

            postResult(runTest(c));
        }
    }

    public static void main(String[] args) throws Exception {
        String collectorHost = "localhost";
        int controllerPort = 8000;
        int grpcPort = 8001;
        Options opts = new Options.OptionsBuilder().
                withAccessToken("notUsed").
                withCollectorHost(collectorHost).
                withCollectorPort(grpcPort).
                withCollectorProtocol("http").
                withVerbosity(3).
                build();
        BenchmarkClient bc = new BenchmarkClient(new JRETracer(opts),
                "http://" + collectorHost + ":" + controllerPort + "/");
        bc.loop();
    }
}
