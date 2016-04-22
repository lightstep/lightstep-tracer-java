# Java Instrumentation API

### Directory Structure
* `debug` contains the interface that must be implemented in order to capture logs and spans from Java Instrumentation while debugging.

* `shared` contains code that is shared across generic Java and Android Instrumentation.

* `Tracer` contains a Java implementation of the Runtime.

* Files in `com.lightstep.tracer` are interfaces intended to anticipate teh
