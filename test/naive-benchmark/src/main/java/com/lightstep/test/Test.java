package com.lightstep.test;

import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

public class Test {

    public static final void main(String[] args) {
	JRETracer tracer = new JRETracer(new Options("access_token"));

	long count = 10000000;
	long before = System.nanoTime();
	for (int i = 0; i < count; i++) {
	    tracer.buildSpan("span/test").start().finish();
	}
	long after = System.nanoTime();
	System.err.println("Time per op = " + (after-before)/1e9/count + " seconds");

	tracer.shutdown();
    }
    
}

