# Development Notes

## Makefile

`Makefile`s are used to encapsulate the various tools in the toolchain:

```bash
make build      # builds JRE libraries and examples
make publish    # increment versions and publish the artifacts to bintray
```

NOTE: to publish, `BINTRAY_USER` and `BINTRAY_API_KEY` need to be set in the shell environment.

###  Directory structure

```
Makefile                    # Top-level Makefile to encapsulate tool-specifics
lightstep-tracer-jre/       # JRE instrumentation library
examples/                   # Sample code for JRE
```

## Formatting

The project uses the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) as a formatting standard. Configurations for your IDE can be downloaded from [github.com/google/styleguide](https://github.com/google/styleguide).