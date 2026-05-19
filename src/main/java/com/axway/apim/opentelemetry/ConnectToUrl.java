package com.axway.apim.opentelemetry;

import com.vordel.circuit.Message;
import com.vordel.config.Circuit;
import com.vordel.mime.HeaderSet;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.semconv.HttpAttributes;
import org.aspectj.lang.ProceedingJoinPoint;

public class ConnectToUrl {

    public Object httpClient(ProceedingJoinPoint pjp, Message message, Circuit circuit, HeaderSet requestHeaders, String httpVerb) throws Throwable {
        Tracer tracer = Telemetry.getTracer("io.opentelemetry.axway.apim.http.HttpClient");
        TextMapPropagator textMapPropagator = Telemetry.getTextMapPropagator();
        if (tracer == null || textMapPropagator == null) {
            return pjp.proceed();
        }

        Span span = null;
        Scope scope = null;
        try {
            String requestUrl = Utils.getRequestURL(message);
            span = tracer.spanBuilder(requestUrl).setSpanKind(SpanKind.CLIENT).startSpan();
            scope = span.makeCurrent();
            span.setAttribute(HttpAttributes.HTTP_REQUEST_METHOD, httpVerb);
            span.setAttribute("component", "http");
            span.setAttribute("Routing policy", circuit.getName());
            String url = (String) message.get("destinationURL");
            Utils.addHttpDetails(span, url, requestUrl, message);
            Utils.addHttpHeaders(span, "request", (HeaderSet) message.get(Utils.HTTP_HEADERS));
            textMapPropagator.inject(Context.current(), requestHeaders, Utils.setter);
        } catch (Throwable e) {
            closeScope(scope);
            endSpan(span, message);
            Telemetry.disable("HTTP client span setup", e);
            return pjp.proceed();
        }

        Object pjpReturnObject;
        Throwable pjpError = null;
        try {
            pjpReturnObject = pjp.proceed();
        } catch (Throwable e) {
            pjpError = e;
            recordException(span, message, e);
            throw e;
        } finally {
            closeScope(scope);
            if (pjpError != null) {
                endSpan(span, message);
            }
        }
        recordResponse(span, message);
        endSpan(span, message);
        return pjpReturnObject;
    }

    private void recordResponse(Span span, Message message) {
        try {
            int httpStatus = (int) message.getOrDefault("http.response.status", 0);
            String httpStatusMessage = (String) message.getOrDefault("http.response.info", "");
            if (httpStatus > 400 && httpStatus < 500) {
                span.setStatus(StatusCode.ERROR, httpStatusMessage);
            } else if (httpStatus > 500) {
                span.setStatus(StatusCode.ERROR, httpStatusMessage);
                span.setAttribute("error.type", httpStatusMessage);
            }
        } catch (Throwable e) {
            Telemetry.recordFailure("HTTP client response recording", e);
        }
    }

    private void recordException(Span span, Message message, Throwable original) {
        try {
            int httpStatus = (int) message.getOrDefault("http.response.status", 0);
            String httpStatusMessage = (String) message.getOrDefault("http.response.info", "");
            span.setStatus(StatusCode.ERROR, httpStatus + "-" + httpStatusMessage);
            span.recordException(original);
        } catch (Throwable e) {
            Telemetry.recordFailure("HTTP client exception recording", e);
        }
    }

    private void endSpan(Span span, Message message) {
        if (span == null) {
            return;
        }
        try {
            Utils.addHttpHeaders(span, "response", (HeaderSet) message.get(Utils.HTTP_HEADERS));
            span.end();
        } catch (Throwable e) {
            Telemetry.recordFailure("HTTP client span close", e);
        }
    }

    private void closeScope(Scope scope) {
        if (scope != null) {
            try {
                scope.close();
            } catch (Throwable e) {
                Telemetry.recordFailure("HTTP client scope close", e);
            }
        }
    }
}
