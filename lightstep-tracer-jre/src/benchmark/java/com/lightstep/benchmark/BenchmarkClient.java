package com.lightstep.test;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

class BenchmarkClient {
    public static final String clientName = "java";

    private String baseUrl;
    private JRETracer testTracer;

    public static void main(String[] args) {
	BenchmarkClient bc = new BenchmarkClient();
	Options opts = new Options("notUsed").
	    withCollectorHost("localhost").
	    withCollectorPort(8000).
	    withCollectorEncryption(Options.Encryption.TLS);
	bc.testTracer = new JRETracer(opts);
	bc.baseUrl = "http://" + opts.collectorHost + ":" + opts.collectorPort;
	System.out.println("I Love LightStep-Java!");

	bc.loop();
    }

    private void loop() {
	while (true) {
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

// func (t *testClient) loop() {
// 	for {
// 		body := t.getURL(benchlib.ControlPath)

// 		control := benchlib.Control{}
// 		if err := json.Unmarshal(body, &control); err != nil {
// 			glog.Fatal("Bench control parse error: ", err)
// 		}
// 		if control.Exit {
// 			return
// 		}
// 		timing, flusht, sleeps, answer := t.run(&control)
// 		var sleeps_buf bytes.Buffer
// 		for _, s := range sleeps {
// 			sleeps_buf.WriteString(fmt.Sprint(int64(s)))
// 			sleeps_buf.WriteString(",")
// 		}
// 		t.getURL(fmt.Sprint(
// 			benchlib.ResultPath,
// 			"?timing=",
// 			timing.Seconds(),
// 			"&flush=",
// 			flusht.Seconds(),
// 			"&s=",
// 			sleeps_buf.String(),
// 			"&a=",
// 			answer))
// 	}
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

// func main() {
// 	flag.Parse()
// 	tc := &testClient{
// 		baseURL: fmt.Sprint("http://",
// 			benchlib.ControllerHost, ":",
// 			benchlib.ControllerPort),
// 		tracer: ls.NewTracer(ls.Options{
// 			AccessToken: benchlib.ControllerAccessToken,
// 			Collector: ls.Endpoint{
// 				Host:      benchlib.ControllerHost,
// 				Port:      benchlib.ControllerPort,
// 				Plaintext: true},
// 		})}
// 	tc.loop()
// }
