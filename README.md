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
- Copy Aspectj weaver - [ aspectjweaver-1.9.6.jar ](https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/1.9.6/aspectjweaver-1.9.6.jar) to  apigateway/ext/lib
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

- Environment variables for API Gateway to send metrics to Jaeger

```
export OTEL_EXPORTER_OTLP_ENDPOINT=http://10.129.61.129:4317
export OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=grpc
export OTEL_TRACES_EXPORTER=otlp
export OTEL_SERVICE_NAME=apim-gw
export OTEL_METRICS_EXPORTER=none
```

- Restart API Gateway

# Testing with New Relic
- Create a licence key
- Environment variables for API Gateway to send metrics to New Relic

```
export OTEL_SERVICE_NAME=api-gw
export OTEL_EXPERIMENTAL_EXPORTER_OTLP_RETRY_ENABLED=true
export OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION=BASE2_EXPONENTIAL_BUCKET_HISTOGRAM
export OTEL_EXPERIMENTAL_RESOURCE_DISABLED_KEYS=process.command_args
export OTEL_EXPORTER_OTLP_ENDPOINT=https://otlp.nr-data.net
export OTEL_EXPORTER_OTLP_HEADERS=api-key=<<license key>>
export OTEL_ATTRIBUTE_VALUE_LENGTH_LIMIT=4095
export OTEL_EXPORTER_OTLP_COMPRESSION=gzip
export OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
export OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE=delta
```

- Restart API Gateway

# Testing with Datadog
- Copy API key 
    - Goto https://us3.datadoghq.com/organization-settings/api-keys and copy the key

- Update datadog/docker-compose.yaml with key and site parameter 
```yaml
        environment:
            - DD_API_KEY=<apikey>
            - DD_SITE=us3.datadoghq.com
```
- Start OTEL collector
```bash
$docker-compose -f datadog/docker-compose.yaml start
```
- Setup environment variable for api gateway
```
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
export OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=grpc
export OTEL_TRACES_EXPORTER=otlp
export OTEL_SERVICE_NAME=apim-gw
export OTEL_METRICS_EXPORTER=none
```

- Restart API Gateway

# Testing with Dynatrace

## Set up your Dynatrace account and environment variables
  - Create a Dynatrace account. If you don’t have one, you can use a [trial account](https://www.dynatrace.com/signup/).

 -  create an access token that includes scopes for the following:

    Ingest OpenTelemetry traces (openTelemetryTrace.ingest)
    Ingest metrics (metrics.ingest) - Currently not used
    Ingest logs (logs.ingest) - currently not used
For details, see [Dynatrace API – Tokens and authentication](https://docs.dynatrace.com/docs/dynatrace-api/basics/dynatrace-api-authentication) in the Dynatrace documentation.

- Update dynatrace/docker-compose.yaml with api key and dynatrace endpoint
```yaml
        environment:
            - DT_API_TOKEN=<Access Token>
            - DT_ENDPOINT=https://{your-env-id}.live.dynatrace.com/api/v2/otlp
```

- Start OTEL collector
```bash
$docker-compose -f dynatrace/docker-compose.yaml start
```
- Setup environment variable for api gateway
```
export OTEL_EXPORTER_OTLP_ENDPOINT=http://loclhost:4317
export OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=grpc
export OTEL_TRACES_EXPORTER=otlp
export OTEL_SERVICE_NAME=apim-gw
export OTEL_METRICS_EXPORTER=none
```

- Restart API Gateway



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
