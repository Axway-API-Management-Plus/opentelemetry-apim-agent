package com.axway.apim.opentelemetry;

import com.vordel.circuit.Message;
import com.vordel.dwe.CorrelationID;
import com.vordel.mime.HeaderSet;
import com.vordel.trace.Trace;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.aspectj.lang.ProceedingJoinPoint;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {


    public Object aroundHttpServer(ProceedingJoinPoint pjp, Message message, String apiName, String httpVerb) throws Throwable {

        Tracer tracer = Telemetry.getTracer("io.opentelemetry.axway.apim.http.HttpServer");
        TextMapPropagator textMapPropagator = Telemetry.getTextMapPropagator();
        if (tracer == null || textMapPropagator == null) {
            return pjp.proceed();
        }

        Span span = null;
        Scope scope = null;
        try {
            HeaderSet headerSet = (HeaderSet) message.get(Utils.HTTP_HEADERS);
            Context context = textMapPropagator.extract(Context.current(), headerSet, Utils.getter);
            String requestUri = Utils.getRequestURL(message);
            Trace.debug("OpenTelemetry Context " + context);
            span = tracer.spanBuilder(httpVerb + " " + apiName).setParent(context).setSpanKind(SpanKind.SERVER).startSpan();
            scope = span.makeCurrent();
            span.setAttribute("api.name", apiName);
            span.setAttribute("component", "http");
            span.setAttribute("http.method", httpVerb);
            URL requestUrl = (URL) message.get("http.request.url");
            if (requestUrl != null) {
                Utils.addHttpDetails(span, requestUrl.toString(), requestUri, message);
            } else {
                Utils.addHttpDetails(span, null, requestUri, message);
            }
            Utils.addHttpHeaders(span, "request", headerSet);
            String appName = (String) message.getOrDefault("authentication.application.name", Utils.DEFAULT);
            String orgName = (String) message.getOrDefault("authentication.organization.name", Utils.DEFAULT);
            String appId = (String) message.getOrDefault("authentication.subject.id", Utils.DEFAULT);
            addRequestAttributes(span, appName, orgName, appId, message.getIDBase());
        } catch (Throwable e) {
            closeScope(scope);
            endSpan(span, message);
            Telemetry.disable("HTTP server span setup", e);
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
            if (httpStatus > 500) {
                span.setStatus(StatusCode.ERROR, httpStatusMessage);
                span.setAttribute("error.type", "internal server error");
            }
        } catch (Throwable e) {
            Telemetry.recordFailure("HTTP server response recording", e);
        }
    }

    private void recordException(Span span, Message message, Throwable original) {
        try {
            int httpStatus = (int) message.getOrDefault("http.response.status", 0);
            String httpStatusMessage = (String) message.getOrDefault("http.response.info", "");
            span.setStatus(StatusCode.ERROR, httpStatus + "-" + httpStatusMessage);
            span.recordException(original);
        } catch (Throwable e) {
            Telemetry.recordFailure("HTTP server exception recording", e);
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
            Telemetry.recordFailure("HTTP server span close", e);
        }
    }

    private void closeScope(Scope scope) {
        if (scope != null) {
            try {
                scope.close();
            } catch (Throwable e) {
                Telemetry.recordFailure("HTTP server scope close", e);
            }
        }
    }

    public void addRequestAttributes(Span span, String appName, String orgName, String appId, CorrelationID correlationId) {
        Map<String, String> map = new HashMap<>();
        if (appName != null && !appName.equals(Utils.DEFAULT)) {
            map.put("AxwayAppName", appName);
        }
        if (orgName != null && !orgName.equals(Utils.DEFAULT)) {
            map.put("AxwayOrgName", orgName);
        }
        if (appId != null && !appId.equals(Utils.DEFAULT)) {
            map.put("AxwayAppId", appId);
        }
        if (correlationId != null) {
            map.put(Utils.AXWAY_CORRELATION_ID, "Id-" + correlationId);
        }
        Trace.info("OpenTelemetry :: Application Id :" + appId + " - Application Name : " + appName);
        addRequestAttributes(span, map);
    }

    public void addRequestAttributes(Span span, Map<String, String> attributes) {
        attributes.forEach(span::setAttribute);
    }

}
