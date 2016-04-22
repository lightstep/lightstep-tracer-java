# Java Tracer

### Build

To build from source use:

    ant compile

To build distribution jar files use:

    ant dist

Note: ant commands include those for running SpanLogsTests, ThreadTest, PayloadTest, and all unit-tests. To see all project ant commands use:

    ant -p

### Integration Notes

To use the Java Runtime you must:

  `import com.lightstep.tracer.*` 

  `import com.lightstep.tracer.runtime.*` - only needed where singleton must be retrieved

To retrieve a runtime instance:
  
  `com.lightstep.tracer.Runtime runtime = com.lightstep.tracer.runtime.JavaRuntime.getInstance();`    

### Directory Navigation

* dependencies - contains external jar files on which Tracer is dependent
* src - contains Instrumentation src, auto-generated Thrift code, and simple tests
* test - contains unit tests for Tracer
