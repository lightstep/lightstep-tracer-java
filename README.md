# lightstep-tracer-java

[ ![Download](https://api.bintray.com/packages/lightstep/maven/lightstep-tracer-android/images/download.svg) ](https://bintray.com/lightstep/maven/) [![Circle CI](https://circleci.com/gh/lightstep/lightstep-tracer-java.svg?style=shield)](https://circleci.com/gh/lightstep/lightstep-tracer-java) [![MIT license](http://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

LightStep implementation of the [OpenTracing API](http://opentracing.io/) for Android and the standard Java JRE.

## Getting started

### Using the Android library

The published Android AAR library is available on Bintray at [lightstep/maven/lightstep-tracer-android](https://bintray.com/lightstep/maven/lightstep-tracer-android/view).


**Gradle**

* *Replace `<VERSION>` below with the current version of the library.*
* The artifact is published to both `jcenter()` and `mavenCentral()`. Use whichever you prefer.

```
repositories {
    jcenter() // OR mavenCentral()
}
dependencies {
    compile 'com.lightstep.tracer:lightstep-tracer-android:<VERSION>'
}
```

* Note: ensure the app's `AndroidManifest.xml` has the following (under the `<manifest>` tag):

```xml
    <!-- Permissions required to make http calls -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
* Initializing the Tracer and create a Span

```java
// Important the OpenTracing interfaces
import io.opentracing.Span;
import io.opentracing.Tracer;

...

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize LightStep tracer implementation in the main activity
    // (or anywhere with a valid android.content.Context).
    this.tracer = new com.lightstep.tracer.android.Tracer(
         this,
         new com.lightstep.tracer.shared.Options("{your_access_token}"));
    ...

    // Start and finish a Span
    Span span = this.tracer.buildSpan("hello_span").start();
    this.doSomeWorkHere();
    span.finish();
```

### Using the JRE library

The published JRE library is available on Bintray at [lightstep/maven/lightstep-tracer-jre](https://bintray.com/lightstep/maven/lightstep-tracer-jre/view).

**Gradle**

* *Replace `<VERSION>` below with the current version of the library.*
* The artifact is published to both `jcenter()` and `mavenCentral()`. Use whichever you prefer.

build.gradle
```
repositories {
  mavenCentral() // OR jcenter()
}
dependencies {
  compile 'com.lightstep.tracer:lightstep-tracer-jre:<VERSION>'
}
```

## Options

### Setting a custom component name

To set the name used in the LightStep UI for this instance of the Tracer, call `withComponentName()` on the `Options` object:

```
options = new com.lightstep.tracer.shared.Options("{your_access_token}")
    .withComponentName("your_custom_name");
```

## Development info

See [DEV.md](DEV.md) for information on contributing to this instrumentation library.
