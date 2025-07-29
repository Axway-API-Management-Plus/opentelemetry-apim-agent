package com.axway.apim.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.runtimemetrics.java8.*;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

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

        OpenTelemetry sdk = AutoConfiguredOpenTelemetrySdk.initialize()
            .getOpenTelemetrySdk();
        Classes.registerObservers(sdk);
        Cpu.registerObservers(sdk);
        MemoryPools.registerObservers(sdk);
        Threads.registerObservers(sdk);
        GarbageCollector.registerObservers(sdk, true);
        return sdk;
    }
}
