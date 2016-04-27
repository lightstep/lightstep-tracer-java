# Work-in-progress

**NOTE: this instrumentation library is still in active development and is not ready for production use.**

## Getting started

### Using the Android library

The published Android AAR library is available on Bintray at [lightstep/maven/lightstep-tracer-android](https://bintray.com/lightstep/maven/lightstep-tracer-android/view).


**Gradle**

*TBD*

### Using the JRE library

The published JRE library is available on Bintray at [lightstep/maven/lightstep-tracer-jre](https://bintray.com/lightstep/maven/lightstep-tracer-jre/view).

**Gradle**

```
repositories {
    jcenter()
    maven {
        url  "http://dl.bintray.com/lightstep/maven"
    }
}
dependencies {
    compile 'com.lightstep.tracer:lightstep-tracer-jre:<DESIRED_VERSION>'
}
```
