# lightstep-tracer-java

[ ![Download](https://api.bintray.com/packages/lightstep/maven/lightstep-tracer-android/images/download.svg) ](https://bintray.com/lightstep/maven/) [![MIT license](http://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

LightStep implementation of the [OpenTracing API](http://opentracing.io/) for Android and the standard Java JRE.

## Getting started

### Using the Android library

The published Android AAR library is available on Bintray at [lightstep/maven/lightstep-tracer-android](https://bintray.com/lightstep/maven/lightstep-tracer-android/view).


**Gradle**

*Replace `<VERSION>` below with the current version of the library.*

```
repositories {
    jcenter()
    maven {
        url  "http://dl.bintray.com/lightstep/maven"
    }
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

### Using the JRE library

The published JRE library is available on Bintray at [lightstep/maven/lightstep-tracer-jre](https://bintray.com/lightstep/maven/lightstep-tracer-jre/view).

**Gradle**

*Replace `<VERSION>` below with the current version of the library.*

```
repositories {
    jcenter()
    maven {
        url  "http://dl.bintray.com/lightstep/maven"
    }
}
dependencies {
    compile 'com.lightstep.tracer:lightstep-tracer-jre:<VERSION>'
}
```

## Development info

See [DEV.md](DEV.md) for information on contributing to this instrumentation library.
