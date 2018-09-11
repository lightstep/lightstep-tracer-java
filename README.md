# lightstep-tracer-java

[ ![Download](https://api.bintray.com/packages/lightstep/maven/lightstep-tracer-jre/images/download.svg) ](https://bintray.com/lightstep/maven/) [![Circle CI](https://circleci.com/gh/lightstep/lightstep-tracer-java.svg?style=shield)](https://circleci.com/gh/lightstep/lightstep-tracer-java) [![MIT license](http://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

The LightStep distributed tracing library for the standard Java runtime environment.

* [Getting Started](#getting-started)
  * [JRE](#getting-started-jre)
* [API documentation](#apidocs)
* [Options](#options)

<a name="#getting-started"></a>
<a name="#getting-started-jre"></a>

## Getting started: JRE

The JRE library is hosted on Bintray, jcenter, and Maven Central. 
The Bintray [lightstep-tracer-jre](https://bintray.com/lightstep/maven/lightstep-tracer-jre/view) project contains 
additional installation and setup information for using the library with various build systems such as Ivy and Maven.

### Maven

```xml
<dependency>
    <groupId>com.lightstep.tracer</groupId>
    <artifactId>lightstep-tracer-jre</artifactId>
    <version> VERSION </version>
</dependency>

<dependency>
    <groupId>com.lightstep.tracer</groupId>
    <artifactId>tracer-grpc</artifactId>
    <version> VERSION </version>
</dependency>

<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.25</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty</artifactId>
    <version>1.14.0</version>
</dependency>
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-tcnative-boringssl-static</artifactId>
    <version>2.0.12.Final</version>
</dependency>
```

* Be sure to replace `VERSION` with the current version of the library
* LightStep libraries use provided scope for grpc-netty, netty-tcnative-boringssl-static and slf4j. In other words, LightStep tracer libraries will rely on whichever gRPC/Netty/sl4j version is currently available (i.e. pulled in at runtime) to avoid conflicting with existing versions within your project

### Gradle

In most cases, modifying your `build.gradle` with the below is all that is required:

```
repositories {
    mavenCentral() // OR jcenter()
}
dependencies {
    compile 'com.lightstep.tracer:lightstep-tracer-jre:VERSION'
    compile 'com.lightstep.tracer:tracer-grpc:VERSION'
}
```

* Be sure to replace `VERSION` with the current version of the library
* The artifact is published to both `jcenter()` and `mavenCentral()`. Use whichever you prefer.

### Initializing the LightStep Tracer

```java
// Important the OpenTracing interfaces
import io.opentracing.Span;
import io.opentracing.Tracer;

// ...

// Initialize the OpenTracing Tracer with LightStep's implementation
Tracer tracer = new com.lightstep.tracer.jre.JRETracer(
         new com.lightstep.tracer.shared.Options.OptionsBuilder()
            .withAccessToken("{your_access_token}")
            .build()
);

// Start and finish a Span
Span span = this.tracer.buildSpan("my_span").start();
this.doSomeWorkHere();
span.finish();
```

<a name="apidocs"></a>
## API Documentation

Tracing instrumentation should use the OpenTracing APIs to stay portable and in sync with the standard:

* [OpenTracing API (javadoc)](http://javadoc.io/doc/io.opentracing/opentracing-api)


For reference, the generated LightStep documentation is also available:

* [lightstep-tracer-jre (javadoc)](http://javadoc.io/doc/com.lightstep.tracer/lightstep-tracer-jre)

## Options

### Setting a custom component name

To set the name used in the LightStep UI for this instance of the Tracer, call `withComponentName()` on the `OptionsBuilder` object:

```java
options = new com.lightstep.tracer.shared.Options.OptionsBuilder()
                      .withAccessToken("{your_access_token}")
                      .withComponentName("your_custom_name")
                      .build();

```

### Disabling the reporting loop

By default, the Java library does a report of any buffered data on a fairly regular interval. To disable this behavior and rely only on explicit calls to `flush()` to report data, initialize with:

```java
options = new com.lightstep.tracer.shared.Options.OptionsBuilder()
                      .withAccessToken("{your_access_token}")
                      .withDisableReportingLoop(true)
                      .build();
```

To then manually flush by using the LightStep tracer object directly:

```java
// Flush any buffered tracing data
((com.lightstep.tracer.jre.JRETracer)tracer).flush();
```

### Flushing the report at exit

In order to send a final flush of the data prior to exit, clients should manually flush by using the LightStep tracer object as described above.

### Disabling default clock correction

By default, the Java library performs clock correction based on timestamp information provided in the spans. To disable this behavior, initialize with: 

```java
options = new com.lightstep.tracer.shared.Options.OptionsBuilder()
                      .withAccessToken("{your_access_token}")
                      .withClockSkewCorrection(false)
                      .build();
```

### Advanced Option: Transport and Serialization Protocols

By following the above configuration, the tracer will send information to LightStep using GRPC and Protocol Buffers which is the recommended configuration. If there are no specific transport protocol needs you have, there is no need to change this default.

There are two options for transport protocols:

- [Protocol Buffers](https://developers.google.com/protocol-buffers/) over [GRPC](https://grpc.io/) - The recommended, default, and most performant solution.
- [Protocol Buffers](https://developers.google.com/protocol-buffers/) over HTTP using [OkHttp](http://square.github.io/okhttp/) - New transport protocol supported for use cases where GRPC isn't an option. In order to enable HTTP you will need to configure the LightStep collectors receiving the data to accept HTTP traffic. Reach out to LightStep for support in this.

You can configure the tracer to support HTTP by replacing `com.lightstep.tracer:tracer-grpc` with `com.lightstep.tracer:tracer-okhttp` when including the tracer dependency. i.e.

#### Maven 

```xml
<dependency>
  <groupId>com.lightstep.tracer</groupId>
  <artifactId>lightstep-tracer-jre</artifactId>
  <version> VERSION </version>
</dependency>

<dependency>
   <groupId>com.lightstep.tracer</groupId>
   <artifactId>tracer-okhttp</artifactId>
   <version> VERSION </version>
</dependency>
```

#### Gradle

```
repositories {
    mavenCentral() // OR jcenter()
}
dependencies {
    compile 'com.lightstep.tracer:lightstep-tracer-jre:VERSION'
    compile 'com.lightstep.tracer:tracer-okhttp:VERSION'
}
```

## Development info

See [DEV.md](DEV.md) for information on contributing to this instrumentation library.
