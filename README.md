# lightstep-tracer-java

[ ![Download](https://api.bintray.com/packages/lightstep/maven/lightstep-tracer-android/images/download.svg) ](https://bintray.com/lightstep/maven/) [![Circle CI](https://circleci.com/gh/lightstep/lightstep-tracer-java.svg?style=shield)](https://circleci.com/gh/lightstep/lightstep-tracer-java) [![MIT license](http://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

LightStep implementation of the [OpenTracing API](http://opentracing.io/) for Android and the standard Java JRE.

* [Getting Started](#getting-started)
  * [Android](#getting-started-android)
  * [JRE](#getting-started-jre)
* [API documentation](#apidocs)
* [Options](#options)

<a name="#getting-started"></a>
<a name="#getting-started-android"></a>

## Getting Started: Android

The Android library is hosted on Bintray, jcenter, and Maven Central. The Bintray [lightstep-tracer-android](https://bintray.com/lightstep/maven/lightstep-tracer-android/view) project contains additional installation and setup information for using the library with various build systems such as Ivy and Maven.

### Gradle

In most cases, modifying your `build.gradle` with the below is all that is required:

```
repositories {
    jcenter() // OR mavenCentral()
}
dependencies {
    compile 'com.lightstep.tracer:lightstep-tracer-android:VERSION'
}
```

* Be sure to replace `VERSION` with the current version of the library
* The artifact is published to both `jcenter()` and `mavenCentral()`. Use whichever you prefer.

### Update your AndroidManifest.xml

Ensure the app's `AndroidManifest.xml` has the following (under the `<manifest>` tag):

```xml
<!-- Permissions required to make http calls -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Initializing the LightStep Tracer


```java
// Important the OpenTracing interfaces
import io.opentracing.Span;
import io.opentracing.Tracer;

// ...

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize LightStep tracer implementation in the main activity
    // (or anywhere with a valid android.content.Context).
    this.tracer = new com.lightstep.tracer.android.Tracer(
         this,
         new com.lightstep.tracer.shared.Options("{your_access_token}"));

    // Start and finish a Span
    Span span = this.tracer.buildSpan("my_span").start();
    this.doSomeWorkHere();
    span.finish();
```

<a name="#getting-started-jre"></a>

## Getting Started: JRE

The JRE library is hosted on Bintray, jcenter, and Maven Central. The Bintray [lightstep-tracer-jre](https://bintray.com/lightstep/maven/lightstep-tracer-jre/view) project contains additional installation and setup information for using the library with various build systems such as Ivy and Maven.

### Maven

```xml
<dependency>
  <groupId>com.lightstep.tracer</groupId>
  <artifactId>lightstep-tracer-jre</artifactId>
  <version> VERSION </version>
  <type>pom</type>
</dependency>
```

* Be sure to replace `VERSION` with the current version of the library

### Gradle

In most cases, modifying your `build.gradle` with the below is all that is required:

```
repositories {
    mavenCentral() // OR jcenter()
}
dependencies {
    compile 'com.lightstep.tracer:lightstep-tracer-jre:VERSION'
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
    new com.lightstep.tracer.shared.Options("{your_access_token}")
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

* [lightstep-tracer-android (javadoc)](http://javadoc.io/doc/com.lightstep.tracer/lightstep-tracer-android)
* [lightstep-tracer-jre (javadoc)](http://javadoc.io/doc/com.lightstep.tracer/lightstep-tracer-jre)

## Options

### Setting a custom component name

To set the name used in the LightStep UI for this instance of the Tracer, call `withComponentName()` on the `Options` object:

```java
options = new com.lightstep.tracer.shared.Options("{your_access_token}")
    .withComponentName("your_custom_name");
```

### Disabling the reporting loop

By default, the Java library does a report of any buffered data on a fairly regular interval. To disable this behavior and rely only on explicit calls to `flush()` to report data, initialize with:

```java
options = new com.lightstep.tracer.shared.Options("{your_access_token}")
    .withDisableReportingLoop(true);
```

To then explicitly control reporting, manually flush by using the LightStep tracer object directly:

```java
// Flush any buffered tracing data
((com.lightstep.tracer.android.Tracer)tracer).flush();
```

### Disabling the report at exit

By default, the library will send a final report of any remaining buffered data at process exit. To disable this behavior, set the following option:

```java
options = new com.lightstep.tracer.shared.Options("{your_access_token}")
    .withDisableReportAtExit(true);
```

## Development info

See [DEV.md](DEV.md) for information on contributing to this instrumentation library.
