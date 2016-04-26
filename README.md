# Work-in-progress

**NOTE: this instrumentation library is still in active development and is not ready for production use.**

## Getting started

### Using the Android library

*TBD*

### Using the JRE library

*TBD*

**Gradle**

Include the below in your `build.gradle` (see the [bintray repository](https://bintray.com/lightstep/maven/lightstep-tracer-jre/view) for details):

```
repositories {
    jcenter()
    maven {
        url  "http://dl.bintray.com/lightstep/maven"
    }
}
dependencies {
    compile 'com.lightstep.tracer:lightstep-tracer-jre:0.1.1'
}
```


## Development info

`Makefile`s are used to encapsulate the various tools in the toolchain:

```bash
make build      # builds Android and JRE versions
make publish    # publish the artifacts to bintray
```

###  Directory structure

```
Makefile        # Top-level Makefile to encapsulate tools specifics
common/         # Shared source code for JRE and Android    
android/        # Android instrumentation library source
jre/            # JRE instrumentation library source
samples/        # Sample code for both JRE and Android
```
