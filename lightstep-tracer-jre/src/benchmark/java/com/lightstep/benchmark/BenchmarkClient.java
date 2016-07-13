package com.lightstep.benchmark;

// TODO several blocks of un-translated code (taken from goclient.go)

import com.fasterxml.jackson.databind.ObjectMapper;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

import io.opentracing.NoopTracer;
import io.opentracing.Tracer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

class BenchmarkClient {
    static final String clientName = "java";

    BenchmarkClient(JRETracer testTracer, String baseUrl) {
	this.baseUrl = baseUrl;
	this.testTracer = testTracer;
	this.objectMapper = new ObjectMapper();
    }

    String baseUrl;
    JRETracer testTracer;
    ObjectMapper objectMapper;

    static class Control {
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
    };

    static class Result {
	double runTime;
	double flushTime;
	ArrayList<Long> sleepNanos;
	long answer;
    };

    static class OneThreadResult {
	ArrayList<Long> sleepNanos;
	long answer;
    };

    InputStream getUrlReader(String path) {
	try {
	    URL u = new URL(baseUrl + path);
	    URLConnection c = u.openConnection();
	    c.setDoOutput(true);
	    c.setDoInput(true);
	    c.getOutputStream().close();
	    return c.getInputStream();
	} catch(MalformedURLException e) {
	    throw new RuntimeException(e);
	} catch(IOException e) {
	    throw new RuntimeException(e);
	}
    }

    <T> T postGetJson(String path, Class<T> cl) {
	try (BufferedReader br = new BufferedReader(new InputStreamReader(getUrlReader(path)))) {
	    return objectMapper.readValue(br, cl);
	} catch(IOException e) {
	    throw new RuntimeException(e);
	}
    }

    Control getControl() {
	return postGetJson("/control", Control.class);
    }

    void postResult(Result r) {
	StringBuilder rurl = new StringBuilder("/result?timing=");
	rurl.append(r.runTime);
	rurl.append("&flush=");
	rurl.append(r.flushTime);
	rurl.append("&a=");
	rurl.append(r.answer);
	rurl.append("&s=");
	for (Long sleep : r.sleepNanos) {
	    rurl.append(sleep.toString());
	    rurl.append(",");
	}
	try (BufferedReader br = new BufferedReader(new InputStreamReader(getUrlReader(rurl.toString())))) {
	} catch(IOException e) {
	    throw new RuntimeException(e);
	}
    }

// var (
// 	logPayloadStr string
// )
// func init() {
// 	lps := make([]byte, benchlib.LogsSizeMax)
// 	for i := 0; i < len(lps); i++ {
// 		lps[i] = 'A' + byte(i%26)
// 	}
// 	logPayloadStr = string(lps)
// }

// func work(n int64) int64 {
// 	const primeWork = 982451653
// 	x := int64(primeWork)
// 	for n != 0 {
// 		x *= primeWork
// 		n--
// 	}
// 	return x
// }

    OneThreadResult testBody(Control c, Tracer t) {
	OneThreadResult r = new OneThreadResult();
	r.sleepNanos = new ArrayList<Long>();
	
// 	var sleep_debt time.Duration
// 	var answer int64
// 	num_sleeps := (time.Duration(control.Repeat) * control.Sleep) / control.SleepInterval
// 	sleeps := make([]time.Duration, num_sleeps)
// 	sleep_cnt := 0
// 	for i := int64(0); i < control.Repeat; i++ {
// 		span := ot.StartSpan("span/test")
// 		answer = work(control.Work)
// 		for i := int64(0); i < control.NumLogs; i++ {
// 			span.LogEventWithPayload("testlog",
// 				logPayloadStr[0:control.BytesPerLog])
// 		}
// 		span.Finish()
// 		sleep_debt += control.Sleep
// 		if sleep_debt < control.SleepInterval {
// 			continue
// 		}
// 		begin := time.Now()
// 		time.Sleep(sleep_debt)
// 		elapsed := time.Now().Sub(begin)
// 		sleep_debt -= elapsed
// 		sleeps[sleep_cnt] = elapsed
// 		sleep_cnt++
// 	}

	return r;
    }

    Result runTest(Control c) {
	Tracer tracer;
	if (c.Trace) {
	    tracer = testTracer;
	} else {
	    tracer = new NoopTracer();
	}

	System.gc();

	Result res = new Result();
	int conc = c.Concurrent;

	ArrayList<OneThreadResult> results = new ArrayList<OneThreadResult>();
	long beginTest = System.currentTimeMillis();

	if (conc == 1) {
	    results.add(testBody(c, tracer));
	} else {
	    ArrayList<Thread> threads = new ArrayList<Thread>();
	    for (int i = 0; i < conc; i++) {
		Thread th = new Thread() {
			public void run() {
			    OneThreadResult tr = testBody(c, tracer);
			    synchronized (results) {
				results.add(tr);
			    }
			}};
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
 	long endTest = System.currentTimeMillis();
 	if (c.Trace) {
	    ((JRETracer)tracer).flush();
	    res.flushTime = (System.currentTimeMillis() - endTest) / 1000.0;
 	}

	res.runTime = (endTest - beginTest) / 1000.0;
	res.sleepNanos = new ArrayList<Long>();
	for (OneThreadResult r1 : results) {
	    res.sleepNanos.addAll(r1.sleepNanos);
	    res.answer += r1.answer;
	}
	return res;
    }

    void loop() {
	while (true) {
	    Control c = getControl();

	    if (c.Exit) {
		return;
	    }

	    postResult(runTest(c));
	}
    }

    public static void main(String[] args) {
	Options opts = new Options("notUsed").
	    withCollectorHost("localhost").
	    withCollectorPort(8000).
	    withCollectorEncryption(Options.Encryption.TLS);
	BenchmarkClient bc = new BenchmarkClient(new JRETracer(opts),
						 "http://" + opts.collectorHost + ":" + opts.collectorPort + "/");

	System.out.println("I Love LightStep-Java!");

	bc.loop();
    }
}
