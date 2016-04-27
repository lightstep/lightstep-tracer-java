# Development Notes

## Makefile

`Makefile`s are used to encapsulate the various tools in the toolchain:

```bash
make build      # builds Android and JRE versions locally
make publish    # increment versions and publish the artifacts to bintray
```

###  Directory structure

```
Makefile                    # Top-level Makefile to encapsulate tool-specifics
common/                     # Shared source code for JRE and Android    
lightstep-tracer-android/   # Android instrumentation library
lightstep-tracer-jre/       # JRE instrumentation library
examples/                   # Sample code for both JRE and Android
```

## Miscellaneous notes

*An unorganized list of things that are likely "good to know" if you're working on the development of these libraries:*

* The Android library currently depends on the JRE JAR. This is not desired, but it the current situation. If updating the Android code, the JRE code should be rebuilt and republished as well.
* The Gradle wrapper is used as this locks Gradle to a specific version for the build despite what is installed locally. This is important as there are problems with different versions of plugins running with different versions of Gradle, especially with Android.
* Android Studio does not (currently) have an option to create an AAR for a pure library (not an app); the gradle file used to build the AAR has been assembled manually.
* The directory name `lightstep-tracer-android` is used as it's currently difficult to get the Gradle Android plug-in to name the AAR anything other than the default of using the host directory name
* There is *intentionally no* top-level Gradle file: understanding the Groovy code / Gradle settings is hard enough without inter-project dependencies
