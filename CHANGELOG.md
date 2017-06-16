<a name="Pending Release"></a>
## [Pending Release](https://github.com/lightstep/lightstep-tracer-java/compare/master...0.12.4)

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
