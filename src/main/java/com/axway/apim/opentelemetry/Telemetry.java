package com.axway.apim.opentelemetry;

import com.vordel.trace.Trace;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;

public final class Telemetry {

    private static volatile OpenTelemetry openTelemetry;
    private static volatile boolean disabled;
    private static volatile boolean failureLogged;

    private Telemetry() {
        throw new IllegalStateException("Telemetry class");
    }

    static OpenTelemetry getOpenTelemetry() {
        if (disabled) {
            return null;
        }
        if (openTelemetry != null) {
            return openTelemetry;
        }
        synchronized (Telemetry.class) {
            if (disabled) {
                return null;
            }
            if (openTelemetry != null) {
                return openTelemetry;
            }
            try {
                openTelemetry = Configuration.getInstance();
                return openTelemetry;
            } catch (Throwable e) {
                disable("OpenTelemetry initialization", e);
                return null;
            }
        }
    }

    static Tracer getTracer(String instrumentationName) {
        OpenTelemetry telemetry = getOpenTelemetry();
        if (telemetry == null) {
            return null;
        }
        try {
            return telemetry.getTracer(instrumentationName);
        } catch (Throwable e) {
            disable("OpenTelemetry tracer creation", e);
            return null;
        }
    }

    static TextMapPropagator getTextMapPropagator() {
        OpenTelemetry telemetry = getOpenTelemetry();
        if (telemetry == null) {
            return null;
        }
        try {
            return telemetry.getPropagators().getTextMapPropagator();
        } catch (Throwable e) {
            disable("OpenTelemetry context propagation setup", e);
            return null;
        }
    }

    public static void disable(String operation, Throwable e) {
        disabled = true;
        if (!failureLogged) {
            synchronized (Telemetry.class) {
                if (!failureLogged) {
                    Trace.info("OpenTelemetry disabled after failure during " + operation + ": " + e);
                    failureLogged = true;
                }
            }
        }
    }

    static void recordFailure(String operation, Throwable e) {
        if (!failureLogged) {
            synchronized (Telemetry.class) {
                if (!failureLogged) {
                    Trace.info("OpenTelemetry failed during " + operation + ": " + e);
                    failureLogged = true;
                }
            }
        }
    }
}
