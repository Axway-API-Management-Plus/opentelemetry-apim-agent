package com.axway.apim.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class Configuration {

    private Configuration() {
        throw new IllegalStateException("Configuration class");
    }

    private static OpenTelemetry openTelemetry;

    public static synchronized OpenTelemetry getInstance() {
        if (openTelemetry != null)
            return openTelemetry;
        openTelemetry = initOpenTelemetry();
        return openTelemetry;
    }

    private static OpenTelemetry initOpenTelemetry() {
        //  Resource resource = Resource.getDefault().toBuilder().put("service.name", "dice-server").put("service.version", "0.1.0").build();

//        SdkTracerProvider sdkTracerProvider =
//            SdkTracerProvider.builder()
//                .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
//        //        .setResource(resource)
//                .build();
//
//        OpenTelemetrySdk sdk =
//            OpenTelemetrySdk.builder()
//                .setTracerProvider(sdkTracerProvider)
//                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
//                .build();

        OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize()
            .getOpenTelemetrySdk();

        //Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::close));
        return sdk;
    }
}
