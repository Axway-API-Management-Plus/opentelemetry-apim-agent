# OpenTelemetry Axway APIM Integration

Axway APIM Integration with OpenTelemetry using OpenTelemetry Java SDK

## API Management Version Compatibility

This artefact tested with following version:

- V7.7 May 2023 


## Compile/Build

In `build.gradle` file, update dependencies location:

- Set the variable `apim_folder` to you API-Gateway installation folder (e.g. `opt/Axway/APIM/apigateway`)


```
gradlew clean jar
```

## Setup

Copy following jar files

- 

- Copy opentelemetry-apim-agent--x.x.x.jar file to  apigateway/ext/lib and restart API Gateway instances

- Copy opentelmetry sdk jar files

  - [opentelemetry-api-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-api/1.30.1/opentelemetry-api-1.30.1.jar)
  - [opentelemetry-exporter-logging-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-logging/1.30.1/opentelemetry-exporter-logging-1.30.1.jar)
  - [opentelemetry-sdk-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk/1.30.1/opentelemetry-sdk-1.30.1.jar)
  - [opentelemetry-sdk-metrics-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-metrics/1.30.1/opentelemetry-sdk-metrics-1.30.1.jar)
  - [kotlin-stdlib-1.6.10.jar](https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.6.20/kotlin-stdlib-1.6.20.jar)
  - [opentelemetry-api-events-1.30.1-alpha.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-api-events/1.30.1-alpha/opentelemetry-api-events-1.30.1-alpha.jar)
  - [opentelemetry-exporter-otlp-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-otlp/1.30.1/opentelemetry-exporter-otlp-1.30.1.jar)       
  - [opentelemetry-sdk-common-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-common/1.30.1/opentelemetry-sdk-common-1.30.1.jar)
  - [opentelemetry-sdk-trace-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-trace/1.30.1/opentelemetry-sdk-trace-1.30.1.jar)
  - [okhttp-4.11.0.jar](https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.11.0/okhttp-4.11.0.jar)
  - [opentelemetry-exporter-otlp-common-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-otlp/1.30.1/opentelemetry-exporter-otlp-1.30.1.jar)
  - [opentelemetry-sdk-extension-autoconfigure-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-extension-autoconfigure-spi/1.30.1/opentelemetry-sdk-extension-autoconfigure-spi-1.30.1.jar)
  - [opentelemetry-semconv-1.30.1-alpha.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-semconv/1.30.1-alpha/opentelemetry-semconv-1.30.1-alpha.jar)
  - [okio-jvm-3.2.0.jar](https://repo1.maven.org/maven2/com/squareup/okio/okio-jvm/3.2.0/okio-jvm-3.2.0.jar)
  - [opentelemetry-context-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-context/1.30.1/opentelemetry-context-1.30.1.jar)
  - [opentelemetry-exporter-sender-okhttp-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-sender-okhttp/1.30.1/opentelemetry-exporter-sender-okhttp-1.30.1.jar)
  - [opentelemetry-sdk-extension-autoconfigure-spi-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-extension-autoconfigure-spi/1.30.1/opentelemetry-sdk-extension-autoconfigure-spi-1.30.1.jar)
  - [opentelemetry-exporter-common-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-exporter-common/1.30.1/opentelemetry-exporter-common-1.30.1.jar)
  - [opentelemetry-extension-incubator-1.30.1-alpha.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-extension-incubator/1.30.1-alpha/opentelemetry-extension-incubator-1.30.1-alpha.jar)
  - [opentelemetry-sdk-logs-1.30.1.jar](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-sdk-logs/1.30.1/opentelemetry-sdk-logs-1.30.1.jar)
- Copy Dynatrace one agent library - https://repo1.maven.org/maven2/com/dynatrace/oneagent/sdk/java/oneagent-sdk/1.8.0/oneagent-sdk-1.8.0.jar to  apigateway/ext/lib
- Copy Aspectj weaver - https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/1.9.6/aspectjweaver-1.9.6.jar to  apigateway/ext/lib
- Sample configuration for Jaeger

```
export OTEL_EXPORTER_OTLP_ENDPOINT=http://10.129.61.129:4317
export OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=grpc
export OTEL_TRACES_EXPORTER=otlp
export OTEL_SERVICE_NAME=apim-gw
export OTEL_METRICS_EXPORTER=none
```
- Restart API Gateway instances
- Create a file named jvm.xml under APIGATEWAY_INSTALL_DIR/apigateway/conf/
    ```xml
    <ConfigurationFragment>
        <VMArg name="-javaagent:/home/axway/Axway-7.7.0-Aug2021/apigateway/ext/lib/aspectjweaver-1.9.6.jar"/>
    </ConfigurationFragment>
    ```


  ![image](https://user-images.githubusercontent.com/58127265/234663741-32b38f29-371a-4413-9c1a-5b81b6a56af8.png)
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
