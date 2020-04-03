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

|Parameter | Type| Required|
|----------|-----|
|ls.accessToken | String|
|ls.componentName | String|
|ls.serviceVersion | String|
|ls.collectorHost | String|
|ls.collectorProtocol | http or https|
|ls.collectorPort | Integer larger than 0|
|ls.tags | Comma separates values, such as "clientid=21228915,offline=false"|
|ls.metricUrl | String, full url, such as "https://myhost:myport/metrics"|
|ls.disableMetricsReporting | Boolean, defaults to false|
