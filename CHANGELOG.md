<a name="Pending Release"></a>
## [Pending Release](https://github.com/lightstep/lightstep-tracer-java/compare/0.30.4...master)

<a name="0.30.4"></a>
## [0.30.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.30.3...0.30.4)
* Updated lightstep-tracer-common to 0.30.2.
* Updated java-metrics-reporter to 0.1.6.

<a name="0.30.3"></a>
## [0.30.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.30.2...0.30.3)
* Updated java-metrics-reporter to 0.1.5.

<a name="0.30.2"></a>
## [0.30.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.30.1...0.30.2)
* Updated lightstep-tracer-common to 0.30.1.

<a name="0.30.1"></a>
## [0.30.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.30.0...0.30.1)
* Don't recreate the sources jar in the bundle artifact.

<a name="0.30.0"></a>
## [0.30.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.20.1...0.30.0)
* Upgraded lightstep-tracer-common to 0.30.0.
* It's possible to select the collector from the bundle.
* Hide token during the logging.
* Misc bug fixes.

<a name="0.20.1"></a>
## [0.20.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.20.0...0.20.1)
* Upgraded lightstep-tracer-common to 0.21.1.

<a name="0.20.0"></a>
## [0.20.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.19.0...0.20.0)
* Upgraded lightstep-tracer-common to 0.21.0.

<a name="0.19.0"></a>
## [0.19.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.18.4...0.19.0)
* Upgraded lightstep-tracer-common to 0.20.0
* Allow the bundle artifact to specify the B3 propagator.

<a name="0.18.4"></a>
## [0.18.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.18.3...0.18.4)
* Upgraded lightstep-tracer-common to 0.19.3

<a name="0.18.3"></a>
## [0.18.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.18.2...0.18.3)
* Allow setting of tags for the bundle artifact.

<a name="0.18.2"></a>
## [0.18.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.18.1...0.18.2)
* Upgraded lightstep-tracer-common to 0.19.2.
  - 0.19.2 B3 format uses the new 0/1 format for the sampling flag.

<a name="0.18.1"></a>
## [0.18.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.18.0...0.18.1)
* Upgraded lightstep-tracer-common to 0.19.1.
  - 0.19.1 improved the B3 format support.

<a name="0.18.0"></a>
## [0.18.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.17.3...0.18.0)
* Upgraded lightstep-tracer-common to 0.19.0.
  - 0.19.0 updated grpc, protobuf and netty.

<a name="0.17.3"></a>
## [0.17.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.17.2...0.17.3)
* Revert netty to 2.0.8 for now. Upgrading it requires updates on grpc/protobuf as well.

<a name="0.17.2"></a>
## [0.17.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.17.1...0.17.2)
* tracerresolver version updated to 0.1.8.

<a name="0.17.1"></a>
## [0.17.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.17.0...0.17.1)
* Upgraded netty to 2.0.25.
* Fix our explicit OpenTracing version (used for internal items) to 0.33 (as done by the parent artifact).

<a name="0.17.0"></a>
## [0.17.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.16.4...0.17.0)
* Upgraded lightstep-tracer-common to 0.18.0.
  - 0.18.0 updates OpenTracing to 0.33.0.

<a name="0.16.4"></a>
## [0.16.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.16.3...0.16.4)
* Upgraded lightstep-tracer-common to 0.17.2.
  - 0.17.2 allows users to specify the DNS used with OkHttp.

<a name="0.16.3"></a>
## [0.16.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.16.2...0.16.3)
* Updated our bundle artifact to *not* include any OpenTracing artifacts.

<a name="0.16.2"></a>
## [0.16.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.16.1...0.16.2)
* Update jackson-databind to 2.9.9 to protect from a recent vulnerability.
* Set the bundle deps to provided-scope (for better interaction with the SpecialAgent).

<a name="0.16.1"></a>
## [0.16.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.16.0...0.16.1)
* Upgraded lightstep-tracer-common to 0.17.1

<a name="0.16.0"></a>
## [0.16.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.15.4...0.16.0)
* Upgraded lightstep-tracer-common to 0.17.0
  - 0.17.0 conforms to OT 0.32.0 and enables accesstoken-less usage.

<a name="0.15.4"></a>
## [0.15.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.15.3...0.15.4)
* Remove slf4j-simple from our bundle jar.
* Update jackson-databind to 2.9.8 to protect from a recent vulnerability.

<a name="0.15.3"></a>
## [0.15.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.15.2...0.15.3)
* Second try to publish the new fat-jar with the OkHttp collector and TracerFactory included.

<a name="0.15.2"></a>
## [0.15.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.15.1...0.15.2)
* Exposed a fat-jar with the OkHttp collector and TracerFactory included.
  - This is expected to be used with the SpecialAgent.

<a name="0.15.1"></a>
## [0.15.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.15.0...0.15.1)
* Upgraded lightstep-tracer-common to 0.16.2
  - 0.16.2 allows setting Component Name per Span and instructs gRPC to do load balancing.

<a name="0.15.0"></a>
## [0.15.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.8...0.15.0)
* Upgraded lightstep-tracer-common to 0.16.1
  - 0.16.1 adds support for meta events and makes AbstractTracer closeable.

<a name="0.14.8"></a>
## [0.14.8](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.7...0.14.8)
* Upgraded lightstep-tracer-common to 0.15.10
  - 0.15.10 Handle empty SpanContext headers upon extraction.

<a name="0.14.7"></a>
## [0.14.7](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.6...0.14.7)
* Upgraded lightstep-tracer-common to 0.15.9
  - 0.15.9 lets users specify the ScopeManager instance.

<a name="0.14.6"></a>
## [0.14.6](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.5...0.14.6)
* Upgraded lightstep-tracer-common to 0.15.8
  - 0.15.7 Fixed a bug regarding parent SpanContext's baggage handling.
  - 0.15.8 Replaced `googleapis-common-protos:0.0.3` dependency with `grpc-google-common-protos:1.12.0`

<a name="0.14.5></a>
## [0.14.5](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.4...0.14.5)
* Upgraded lightstep-tracer-common to 0.15.6
  - 0.15.5 Exposes custom propagators support, and support for B3 headers.

<a name="0.14.4></a>
## [0.14.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.3...0.14.4)
* Upgraded lightstep-tracer-common to 0.15.5
  - 0.15.5 Exposes deadlineMillis in OptionsBuilder.

<a name="0.14.3></a>
## [0.14.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.2...0.14.3)
* Updated the compiled protos. Changed type of clock correction offset from int to long.
  - Included new dependency required by proto upgrade com.google.api.grpc:googleapis-common-protos:0.0.3
  - Fixed bug where large clock corrections caused crashes.

<a name="0.14.2></a>
## [0.14.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.1...0.14.2)
* Upgrade dependencies (#140)    
  - com.fasterxml.jackson.core:jackson-databind from 2.8.9 to 2.9.5
  - com.lightstep.tracer from 0.15.1 to 0.15.2
  - io.grpc from 1.4.0 to 1.11.0
  - io.netty from 2.0.5.Final to 2.0.8.Final

<a name="0.14.1"></a>
## [0.14.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.14.0...0.14.1)
* Bugfixes

<a name="0.14.0"></a>
## [0.14.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.13.1...0.14.0)
* Upgraded to io.opentracing 0.31.0, for more information see 
[Announcing Java OpenTracing v0.31](https://medium.com/opentracing/announcing-java-v0-31-release-candidate-6e1f1a922d2e)

### BREAKING CHANGES
* BaseSpan and ActiveSpan are simplified into a single Span class.
* Scope replaces ActiveSpan, removing the continuation concept.
* ScopeManager replaces ActiveSpanSource
* ThreadLocalScopeManager replaces ThreadLocalActiveSpanSource

<a name="0.13.0"></a>
## [0.13.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.15...0.13.0)
* Bumped to 0.14.0 of lightstep-tracer-common.
* Split grpc transport support into a separate dependency.

We are splitting out our transport dependency from our main tracer to making binaries smaller.

For the tracer to work, you will now need to provide a transport dependency with your tracer.

### Maven

```
<dependency>
   <groupId>com.lightstep.tracer</groupId>
   <artifactId>tracer-grpc</artifactId>
   <version>${com.lightstep.version}</version>
</dependency>
```

### Gradle

```
dependencies {
    ...
    compile 'com.lightstep.tracer:tracer-grpc:VERSION'
    ...
}
```

<a name="0.12.15"></a>
## [0.12.15](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.14...0.12.15)
* Fixed issue where shadow jar was not published to Maven

<a name="0.12.14"></a>
## [0.12.14](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.10...0.12.14)
* Fixed issue where parent pom referenced by jar was not available in Maven

<a name="0.12.10"></a>
## [0.12.10](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.9...0.12.10)
* Upgraded java-common to 0.13.2 (Addresses major issue with GRPC call deadline. Previous version was setting a single global deadline, so all calls after 30s would
exceed the deadline. This new version sets a 30s deadline from the time of each call.)

<a name="0.12.9"></a>
## [0.12.9](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.8...0.12.9)
* Built with Java 8
* Upgraded java-common to 0.13.1 (Fixes a potential deadlock)

<a name="0.12.8"></a>
## [0.12.8](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.7...0.12.8)
* Fixed bug in publish script, previous build 0.12.7 would report client library version as 0.12.6

<a name="0.12.7"></a>
## [0.12.7](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.6...0.12.7)
* Moved shadow project to it's own Maven module for more flexible jar creation
* Fixed miscellaneous IntelliJ IDE warnings
* Upgraded Jackson from 2.7.4 to 2.8.9
* Made Netty dependency scope 'provided'
* Made benchmark.jar an uber jar

<a name="0.12.6"></a>
## [0.12.6](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.5...0.12.6)
* Added null checks in SpanBuilder
* Removed common and android code from the repo
* Converted project from Gradle to Maven
* Added slf4j-simple to the pom for executable projects
* Upgraded java-common to 0.12.6

<a name="0.12.5"></a>
## [0.12.5](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.4...0.12.5)
* Bugfix: upgrade to grpc:1.2.0 everywhere
* Initialize collector client once during AbstractTracer construction

<a name="0.12.4"></a>
## [0.12.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.2...0.12.4)
* Upgrade to grpc-netty:1.2.0

<a name="0.12.2"></a>
## [0.12.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.1...0.12.2)
* Bug fix for constructing new OptionBuilder from existing options

<a name="0.12.1"></a>
## [0.12.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.1...0.12.0.RC1)
* Upgraded to io.opentracing 0.30.0
* Add option to turn off default clock correction

<a name="0.12.0.RC1"></a>
## [0.12.0.RC1](https://github.com/lightstep/lightstep-tracer-java/compare/0.12.0.RC1...0.11.0)

* Upgraded to io.opentracing 0.30.0.RC2

<a name="0.11.0"></a>
## [0.11.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.11.0...0.10.0)

### BREAKING CHANGES
* Thrift protocol has been removed in favor of protobuf (GRPC)
* Many API changes in Span, SpanBuilder and SpanContext

<a name="0.10.0"></a>
## [0.10.0](https://github.com/lightstep/lightstep-tracer-java/compare/0.10.0...0.9.28)

### BREAKING CHANGES
* Changed spanId and traceId from String to Long

<a name="0.9.28"></a>
## [0.9.28](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.28...0.9.27)

* Upgraded to io.opentracing 0.20.0

<a name="0.9.27"></a>
## [0.9.27](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.27...0.9.26)

### BUG FIXES
* Fixes issue where pom file for java-common and lightstep-tracer-jre (generated during local Maven publish) incorrectly referenced dependencies with 'runtime' scope when it should be 'compile' scope.
* Handle potential for ClassCastException in SpanContext

<a name="0.9.26"></a>
## [0.9.26](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.26...0.9.25)
Bugfix: Handle when SpanContext keys have mixed case like: Ot-tracer-spanId

<a name="0.9.25"></a>
## [0.9.25](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.25...0.9.21) (2016-11-18)

### BREAKING CHANGES

* *withDisableReportOnExit* method has been removed from Options. The standard behavior now is NOT to disable report on exit. Clients should instead call *AbstractTracer.flush()* on exit.
* Options can no longer be constructed directly by clients. You must use the Options.OptionsBuilder which will ensure your settings are valid and will set defaults where needed.
 
```java
Options options = new Options.OptionsBuilder()
                             .withAccessToken("{your_access_token}")
                             .build()
```

<a name="0.9.21"></a>
## [0.9.21](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.21...0.9.20) (2016-11-15)

<a name="0.9.20"></a>
## [0.9.20](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.20...0.9.19) (2016-11-15)

<a name="0.9.19"></a>
## [0.9.19](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.19...0.9.18) (2016-11-10)

<a name="0.9.18"></a>
## [0.9.18](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.18...0.9.17) (2016-10-26)

<a name="0.9.17"></a>
## [0.9.17](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.17...0.9.16) (2016-10-26)

<a name="0.9.16"></a>
## [0.9.16](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.16...0.9.15) (2016-10-21)

<a name="0.9.15"></a>
## [0.9.15](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.15...0.9.14) (2016-10-13)

<a name="0.9.14"></a>
## [0.9.14](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.14...0.9.13) (2016-10-13)

<a name="0.9.13"></a>
## [0.9.13](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.13...0.9.12) (2016-10-10)

<a name="0.9.12"></a>
## [0.9.12](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.12...0.9.11) (2016-09-21)

<a name="0.9.11"></a>
## [0.9.11](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.11...0.9.10) (2016-09-21)

<a name="0.9.10"></a>
## [0.9.10](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.10...0.9.9) (2016-09-21)

<a name="0.9.9"></a>
## [0.9.9](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.9...0.9.8) (2016-09-16)

<a name="0.9.8"></a>
## [0.9.8](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.8...0.9.7) (2016-09-16)

<a name="0.9.7"></a>
## [0.9.7](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.7...0.9.6) (2016-08-29)

<a name="0.9.6"></a>
## [0.9.6](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.6...0.9.4) (2016-08-25)

<a name="0.9.4"></a>
## [0.9.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.4...0.9.3) (2016-08-05)

<a name="0.9.3"></a>
## [0.9.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.3...0.9.2) (2016-08-04)

<a name="0.9.2"></a>
## [0.9.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.2...0.9.1) (2016-08-01)

<a name="0.9.1"></a>
## [0.9.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.9.1...0.8.23) (2016-07-29)

<a name="0.8.23"></a>
## [0.8.23](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.23...0.8.22) (2016-07-22)

<a name="0.8.22"></a>
## [0.8.22](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.22...0.8.21) (2016-07-20)

<a name="0.8.21"></a>
## [0.8.21](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.21...0.8.20) (2016-07-20)

<a name="0.8.20"></a>
## [0.8.20](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.20...0.8.19) (2016-07-20)

<a name="0.8.19"></a>
## [0.8.19](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.19...0.8.18) (2016-07-20)

<a name="0.8.18"></a>
## [0.8.18](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.18...0.8.17) (2016-07-20)

<a name="0.8.17"></a>
## [0.8.17](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.17...0.8.16) (2016-07-20)

<a name="0.8.16"></a>
## [0.8.16](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.16...0.8.15) (2016-07-20)

<a name="0.8.15"></a>
## [0.8.15](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.15...0.8.14) (2016-07-20)

<a name="0.8.14"></a>
## [0.8.14](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.14...0.8.13) (2016-07-20)

<a name="0.8.13"></a>
## [0.8.13](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.13...0.8.12) (2016-07-20)

<a name="0.8.12"></a>
## [0.8.12](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.12...0.8.11) (2016-07-19)

<a name="0.8.11"></a>
## [0.8.11](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.11...0.8.10) (2016-07-19)

<a name="0.8.10"></a>
## [0.8.10](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.10...0.8.9) (2016-07-19)

<a name="0.8.9"></a>
## [0.8.9](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.9...0.8.8) (2016-07-19)

<a name="0.8.8"></a>
## [0.8.8](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.8...0.8.7) (2016-07-19)

<a name="0.8.7"></a>
## [0.8.7](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.7...0.8.6) (2016-07-19)

<a name="0.8.6"></a>
## [0.8.6](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.6...0.8.5) (2016-07-19)

<a name="0.8.5"></a>
## [0.8.5](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.5...0.8.4) (2016-07-19)

<a name="0.8.4"></a>
## [0.8.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.4...0.8.3) (2016-07-19)

<a name="0.8.3"></a>
## [0.8.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.3...0.8.2) (2016-07-18)

<a name="0.8.2"></a>
## [0.8.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.2...0.8.1) (2016-07-18)

<a name="0.8.1"></a>
## [0.8.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.8.1...0.7.4) (2016-07-17)

<a name="0.7.4"></a>
## [0.7.4](https://github.com/lightstep/lightstep-tracer-java/compare/0.7.4...0.7.3) (2016-07-14)

<a name="0.7.3"></a>
## [0.7.3](https://github.com/lightstep/lightstep-tracer-java/compare/0.7.3...0.7.2) (2016-07-14)

<a name="0.7.2"></a>
## [0.7.2](https://github.com/lightstep/lightstep-tracer-java/compare/0.7.2...0.7.1) (2016-07-13)

<a name="0.7.1"></a>
## [0.7.1](https://github.com/lightstep/lightstep-tracer-java/compare/0.7.1...0.1.29) (2016-07-13)

<a name="0.1.29"></a>
## [0.1.29](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.29...0.1.28) (2016-07-13)

<a name="0.1.28"></a>
## [0.1.28](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.28...0.1.27) (2016-07-08)

<a name="0.1.27"></a>
## [0.1.27](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.27...0.1.26) (2016-07-08)

<a name="0.1.26"></a>
## [0.1.26](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.26...0.1.25) (2016-07-07)

<a name="0.1.25"></a>
## [0.1.25](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.25...0.1.24) (2016-06-07)

<a name="0.1.24"></a>
## [0.1.24](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.24...0.1.23) (2016-06-07)

<a name="0.1.23"></a>
## [0.1.23](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.23...0.1.22) (2016-06-07)

<a name="0.1.22"></a>
## [0.1.22](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.22...0.1.20) (2016-06-07)

<a name="0.1.20"></a>
## [0.1.20](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.20...0.1.18) (2016-05-27)

<a name="0.1.18"></a>
## [0.1.18](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.18...0.1.17) (2016-05-27)

<a name="0.1.17"></a>
## [0.1.17](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.17...0.1.16) (2016-04-28)

<a name="0.1.16"></a>
## [0.1.16](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.16...0.1.15) (2016-04-27)

<a name="0.1.15"></a>
## [0.1.15](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.15...0.1.14) (2016-04-27)

<a name="0.1.14"></a>
## [0.1.14](https://github.com/lightstep/lightstep-tracer-java/compare/0.1.14...0.1.13) (2016-04-27)

<a name="0.1.13"></a>
## 0.1.13 (2016-04-27)
