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

    private static final Tracer TRACER = Configuration.getInstance().getTracer("io.opentelemetry.axway.apim.http.HttpClient");
    private static final TextMapPropagator TEXT_MAP_PROPAGATOR = Configuration.getInstance().getPropagators().getTextMapPropagator();

    public Object httpClient(ProceedingJoinPoint pjp, Message message, Circuit circuit, HeaderSet requestHeaders, String httpVerb) throws Throwable {
        Object pjpReturnObject;
        String requestUrl = Utils.getRequestURL(message);
        Span span = TRACER.spanBuilder(requestUrl).setSpanKind(SpanKind.CLIENT).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            span.setAttribute(HttpAttributes.HTTP_REQUEST_METHOD, httpVerb);
            span.setAttribute("component", "http");
            span.setAttribute("Routing policy", circuit.getName());
            String url = (String) message.get("destinationURL");
            Utils.addHttpDetails(span, url, requestUrl, message);
            Utils.addHttpHeaders(span, "request", (HeaderSet) message.get(Utils.HTTP_HEADERS));
            TEXT_MAP_PROPAGATOR.inject(Context.current(), requestHeaders, Utils.setter);
            pjpReturnObject = pjp.proceed();
            int httpStatus = (int) message.getOrDefault("http.response.status", 0);
            String httpStatusMessage = (String) message.getOrDefault("http.response.info", "");
            if (httpStatus > 400 && httpStatus < 500) {
                span.setStatus(StatusCode.ERROR, httpStatusMessage);
            } else if (httpStatus > 500) {
                span.setStatus(StatusCode.ERROR, httpStatusMessage);
                span.setAttribute("error.type", httpStatusMessage);
            }
        } catch (Throwable e) {
            String httpStatusMessage = (String) message.getOrDefault("http.response.info", "");
            span.setStatus(StatusCode.ERROR, httpStatusMessage);
            span.recordException(e);
            throw e;
        } finally {
            Utils.addHttpHeaders(span, "response", (HeaderSet) message.get(Utils.HTTP_HEADERS));
            span.end();
        }
        return pjpReturnObject;
    }
}
