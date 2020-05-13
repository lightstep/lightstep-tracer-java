# LightStep Tracer bundle.

The LightStep distributed tracing library for the standard Java runtime environment, as a fat-jar containing the
OkHttp collector layer and a `TracerFactory` implementation, which can be configured through a `tracer.properties`
configuration file:

```properties
ls.accessToken=myaccesstoken
ls.componentName=MyApplication
ls.collectorHost=collector.lightstep.com
ls.collectorProtocol=https
ls.collectorPort=66631
``` 

Parameters can be overriden through System properties, too:

```
java -cp:$MYCLASSPATH:lightstep.jar \
	-Dls.componentName=AnotherService \
	com.mycompany.MyService
```

## Parameters

LightStep Tracer parameters use the prefix `ls.`. The only required parameter is `ls.accessToken`, and no Tracer will be created if this parameter is missing. In case of error, a log showing the error will be shown.

Common parameters are:

|Parameter | Type| Default| Description|
|----------|-----|--------|------------|
|ls.accessToken | String| (required) | access token for the collector |
|ls.componentName | String| name of the java command | the service name |
|ls.serviceVersion | String| `<null>` | sets the `service.version` tag |
|ls.collectorClient | `grpc` or `http` | `http` | how spans are sent to the collector | 
|ls.collectorHost | String| `collector.lightstep.com` | the collector host |
|ls.collectorProtocol | `http` or `https`| `https` | the protocol to use for the `http` collector client |
|ls.collectorPort | Integer larger than 0| 80 or 443 | the collector port |
|ls.tags | Comma separates values, such as "clientid=21228915,offline=false" | | global tags |
|ls.clockSkewCorrection | boolean | true | if clock skew should be corrected
|ls.deadlineMillis | long | 30000 | timeout for sending spans | 
|ls.disableReportingLoop | boolean | false | if reporting should be disabled |
|ls.maxBufferedSpans | int | 1000 | maximum number of spans to buffer |
|ls.maxReportingIntervalMillis | int | 3000 | maximum reporting interval |
|ls.resetClient | boolean | true | if the collection to the collector should be reset each time | 
|ls.verbosity | int | 1 | the logging verbosity | 
|ls.propagator | `b3` | LightStep | the propagator to use for HTTP headers |
|ls.metricsUrl | String | | full url for metrics reporting, such as "https://myhost:myport/metrics" |
|ls.disableMetricsReporting | boolean | false | disables metrics reporting |  
