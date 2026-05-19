# OpenTelemetry Axway APIM Integration

Axway APIM Integration with OpenTelemetry using OpenTelemetry Java SDK

## API Management Version Compatibility

This artifact tested with the following version:

- V7.7 May 2024 


## Compile/Build

Set `apim_folder` to the API Gateway `system` folder so Gradle can compile against the APIM runtime jars:


```
./gradlew clean jar -Papim_folder=/opt/Axway/apigateway/system
```

Alternatively, set `APIM_FOLDER`:

```
APIM_FOLDER=/opt/Axway/apigateway/system ./gradlew clean jar
```

To assemble the agent jar and required runtime dependency jars into one install directory, run:

```
./gradlew clean assembleInstall -Papim_folder=/opt/Axway/apigateway/system
```

The install directory is created at `build/install/opentelemetry-apim-agent`.

The assembled directory is the source of truth for the runtime jar set. To review the exact files Gradle resolved for this build, run:

```
ls -1 build/install/opentelemetry-apim-agent/*.jar
```

## Setup

Copy the assembled jar files:

- Copy all jar files from `build/install/opentelemetry-apim-agent` to `apigateway/ext/lib`.
- Remove any previously copied OpenTelemetry/APIM agent jars from `apigateway/ext/lib` before copying the newly assembled jars. Do not leave older OpenTelemetry versions beside the assembled set.

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
