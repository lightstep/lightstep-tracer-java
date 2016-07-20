# Development Notes

## Makefile

`Makefile`s are used to encapsulate the various tools in the toolchain:

```bash
make build      # builds Android, JRE libraries and examples
make publish    # increment versions and publish the artifacts to bintray
```

NOTE: to publish, `BINTRAY_USER` and `BINTRAY_API_KEY` need to be set in the shell environment.

###  Directory structure

```
Makefile                    # Top-level Makefile to encapsulate tool-specifics
common/                     # Shared source code for JRE and Android    
lightstep-tracer-android/   # Android instrumentation library
lightstep-tracer-jre/       # JRE instrumentation library
examples/                   # Sample code for both JRE and Android
```
