# OpenTelemetry Axway APIM Integration

Axway APIM Integration with OpenTelemetry using OpenTelemetry Java SDK

## API Management Version Compatibility

This artefact tested with following version:

- V7.7 May 2024 


## Compile/Build

In `build.gradle` file, update dependencies location:

- Set the variable `apim_folder` to you API-Gateway installation folder (e.g. `opt/Axway/APIM/apigateway`)


```
gradlew clean jar
```

## Setup

Copy following jar files


- Copy opentelemetry-apim-agent--x.x.x.jar file to apigateway/ext/lib
- Copy Aspectj weaver - [ aspectjweaver-1.9.22.1.jar ](https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/1.9.22.1/aspectjweaver-1.9.22.1.jar) to  apigateway/ext/lib
- Copy opentelmetry sdk jar files to apigateway/ext/lib 

  - [okhttp-4.12.0.jar](https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.12.0/okhttp-4.12.0.jar)
  - [okio-jvm-3.6.0.jar](https://repo1.maven.org/maven2/com/squareup/okio/okio-jvm/3.6.0/okio-jvm-3.6.0.jar)
  - [kotlin-stdlib-1.9.25.jar](https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.9.25/kotlin-stdlib-1.9.25.jar)

  - [opentelemetry-semconv-1.27.0-alpha.jar](https://repo1.maven.org/maven2/io/opentelemetry/semconv/opentelemetry-semconv/1.27.0-alpha/opentelemetry-semconv-1.27.0-alpha.jar)
  
  - [opentelemetry-api-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-api/1.42.1/opentelemetry-api-1.42.1.jar)
  - [opentelemetry-api-incubator-1.42.1-alpha.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-api-incubator/1.42.1-alpha/opentelemetry-api-incubator-1.42.1-alpha.jar)
  
  - [opentelemetry-context-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-context/1.42.1/opentelemetry-context-1.42.1.jar)

  - [opentelemetry-exporter-common-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-common/1.42.1/opentelemetry-exporter-common-1.42.1.jar)
  - [opentelemetry-exporter-logging-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-logging/1.42.1/opentelemetry-exporter-logging-1.42.1.jar)
  - [opentelemetry-exporter-otlp-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-otlp/1.42.1/opentelemetry-exporter-otlp-1.42.1.jar)
  - [opentelemetry-exporter-otlp-common-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-otlp-common/1.42.1/opentelemetry-exporter-otlp-common-1.42.1.jar)
  - [opentelemetry-exporter-sender-okhttp-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-sender-okhttp/1.42.1/opentelemetry-exporter-sender-okhttp-1.42.1.jar)

  - [opentelemetry-sdk-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk/1.42.1/opentelemetry-sdk-1.42.1.jar)
  - [opentelemetry-sdk-common-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-common/1.42.1/opentelemetry-sdk-common-1.42.1.jar)
  - [opentelemetry-sdk-extension-autoconfigure-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-extension-autoconfigure/1.42.1/opentelemetry-sdk-extension-autoconfigure-1.42.1.jar)
  - [opentelemetry-sdk-extension-autoconfigure-spi-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-extension-autoconfigure-spi/1.42.1/opentelemetry-sdk-extension-autoconfigure-spi-1.42.1.jar)
 
  
  - [opentelemetry-extension-incubator-1.30.1-alpha.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-extension-incubator/1.30.1-alpha/opentelemetry-extension-incubator-1.30.1-alpha.jar)
  - [opentelemetry-sdk-logs-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-logs/1.42.1/opentelemetry-sdk-logs-1.42.1.jar)
  - [opentelemetry-sdk-metrics-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-metrics/1.42.1/opentelemetry-sdk-metrics-1.42.1.jar)
  - [opentelemetry-sdk-trace-1.42.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-trace/1.42.1/opentelemetry-sdk-trace-1.42.1.jar)
- Environment variables for API Gateway to send metrics to Jaeger / any other OTLP supported vendors

```
export OTEL_EXPORTER_OTLP_ENDPOINT=http://10.129.61.129:4317
export OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=grpc
export OTEL_TRACES_EXPORTER=otlp
export OTEL_SERVICE_NAME=apim-gw
export OTEL_METRICS_EXPORTER=none
```
- Create a file named jvm.xml under APIGATEWAY_INSTALL_DIR/apigateway/conf/
    ```xml
    <ConfigurationFragment>
        <VMArg name="-javaagent:/home/axway/Axway-7.7.0/apigateway/ext/lib/aspectjweaver-1.9.22.1.jar"/>
    </ConfigurationFragment>
    ```
- Restart API Gateway instances

# Testing with Jaeger

- Start Jaeger server
```
docker run --name jaeger   -e COLLECTOR_OTLP_ENABLED=true   -p 16686:16686   -p 4317:4317   -p 4318:4318   jaegertracing/all-in-one:1.49
```


## Requests captured in OpenTelemetry
- Policy exposed as Endpoint.
- API manager Traffic
- API Repository defined in Policystudio
- API Manager UI traffic

## Requests not captured in OpenTelemetry
- API Manager REST API call.
- Servlet defined in Policystudio.


## Contributing

Please read [Contributing.md](https://github.com/Axway-API-Management-Plus/Common/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Team

![alt text][Axwaylogo] Axway Team

[Axwaylogo]: https://github.com/Axway-API-Management/Common/blob/master/img/AxwayLogoSmall.png  "Axway logo"

## License
[Apache License 2.0](LICENSE)
