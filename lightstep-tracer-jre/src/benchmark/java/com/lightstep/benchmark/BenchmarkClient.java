package com.lightstep.benchmark;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

class BenchmarkClient {
    public static final String clientName = "java";

    BenchmarkClient(JRETracer testTracer, String baseUrl) {
	this.baseUrl = baseUrl;
	this.testTracer = testTracer;
	this.objectMapper = new ObjectMapper();
    }

    private String baseUrl;
    private JRETracer testTracer;
    private ObjectMapper objectMapper;

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

    public static class Control {
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

    private <T> T getUrl(String path, Class<T> cl) {
	try {
	    URL u = new URL(baseUrl + path);
	    URLConnection c = u.openConnection();
	    c.setDoOutput(true);
	    c.setDoInput(true);
	    c.getOutputStream().close();

	    BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
	    T obj = objectMapper.readValue(in, cl);
	    in.close();
	    return obj;
	} catch(MalformedURLException e) {
	    throw new RuntimeException(e);
	} catch(IOException e) {
	    throw new RuntimeException(e);
	}
    }

    private void loop() {
	while (true) {
	    Control c = getUrl("/control", Control.class);

	    if (c.Exit) {
		return;
	    }

	    // 	timing, flusht, sleeps, answer := t.run(&control)
	    // 	var sleeps_buf bytes.Buffer
	    // 	for _, s := range sleeps {
	    // 		sleeps_buf.WriteString(fmt.Sprint(int64(s)))
	    // 		sleeps_buf.WriteString(",")
	    // 	}
	    // 	t.getURL(fmt.Sprint(
	    // 		benchlib.ResultPath,
	    // 		"?timing=",
	    // 		timing.Seconds(),
	    // 		"&flush=",
	    // 		flusht.Seconds(),
	    // 		"&s=",
	    // 		sleeps_buf.String(),
	    // 		"&a=",
	    // 		answer))
	}
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

// type testClient struct {
// 	baseURL string
// 	tracer  ot.Tracer
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

// func (t *testClient) getURL(path string) []byte {
// 	resp, err := http.Get(t.baseURL + path)
// 	if err != nil {
// 		glog.Fatal("Bench control request failed: ", err)
// 	}
// 	if resp.StatusCode != 200 {
// 		glog.Fatal("Bench control status != 200: ", resp.Status)
// 	}

// 	defer resp.Body.Close()
// 	body, err := ioutil.ReadAll(resp.Body)
// 	if err != nil {
// 		glog.Fatal("Bench error reading body: ", err)
// 	}
// 	return body
// }

// func testBody(control *benchlib.Control) ([]time.Duration, int64) {
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
// 	return sleeps, answer
// }

// func (t *testClient) run(control *benchlib.Control) (time.Duration, time.Duration, []time.Duration, int64) {
// 	if control.Trace {
// 		ot.InitGlobalTracer(t.tracer)
// 	} else {
// 		ot.InitGlobalTracer(ot.NoopTracer{})
// 	}
// 	conc := control.Concurrent
// 	runtime.GOMAXPROCS(conc)
// 	runtime.GC()

// 	sleeps := make([][]time.Duration, conc, conc)
// 	answer := make([]int64, conc, conc)

// 	beginTest := time.Now()
// 	if conc == 1 {
// 		sleeps[0], answer[0] = testBody(control)
// 	} else {
// 		start := &sync.WaitGroup{}
// 		finish := &sync.WaitGroup{}
// 		start.Add(conc)
// 		finish.Add(conc)
// 		for c := 0; c < conc; c++ {
// 			c := c
// 			go func() {
// 				start.Done()
// 				start.Wait()
// 				sleeps[c], answer[c] = testBody(control)
// 				finish.Done()
// 			}()
// 		}
// 		finish.Wait()
// 	}
// 	endTime := time.Now()
// 	flushDur := time.Duration(0)
// 	if control.Trace {
// 		recorder := t.tracer.(basictracer.Tracer).Options().Recorder.(*ls.Recorder)
// 		recorder.Flush()
// 		flushDur = time.Now().Sub(endTime)
// 	}
// 	var sleep_final []time.Duration
// 	var answer_final int64
// 	for c := 0; c < conc; c++ {
// 		for _, s := range sleeps[c] {
// 			if s != 0 {
// 				sleep_final = append(sleep_final, s)
// 			}
// 		}
// 		answer_final += answer[c]
// 	}
// 	return endTime.Sub(beginTest), flushDur, sleep_final, answer_final
// }
