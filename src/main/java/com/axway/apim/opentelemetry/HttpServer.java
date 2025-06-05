package com.axway.apim.opentelemetry;

import com.vordel.circuit.Message;
import com.vordel.dwe.CorrelationID;
import com.vordel.mime.HeaderSet;
import com.vordel.trace.Trace;
import io.opentelemetry.api.OpenTelemetry;
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


    private static final OpenTelemetry openTelemetry = Configuration.getInstance();
    private static final Tracer tracer =
        openTelemetry.getTracer("io.opentelemetry.axway.apim.http.HttpServer");

    private static final TextMapPropagator TEXT_MAP_PROPAGATOR =
        openTelemetry.getPropagators().getTextMapPropagator();


    public Object aroundHttpServer(ProceedingJoinPoint pjp, Message message, String apiName, String httpVerb) throws Throwable {

        Object pjpReturnObject;
        HeaderSet headerSet = (HeaderSet) message.get(Utils.HTTP_HEADERS);
        Context context = TEXT_MAP_PROPAGATOR.extract(Context.current(), headerSet, Utils.getter);
        String requestUri = Utils.getRequestURL(message);
        Trace.debug("OpenTelemetry Context " + context);
        Span span = tracer.spanBuilder(httpVerb + " " + requestUri).setParent(context).setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope ignored = span.makeCurrent()) {
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
            pjpReturnObject = pjp.proceed();
            int httpStatus = (int) message.getOrDefault("http.response.status", 0);
            String httpStatusMessage = (String) message.getOrDefault("http.response.info", "");
            if (httpStatus > 500) {
                span.setStatus(StatusCode.ERROR, httpStatusMessage);
                span.setAttribute("error.type", "internal server error");
            }
        } catch (Throwable e) {
            String httpStatusMessage = (String) message.getOrDefault("http.response.info", "");
            span.setStatus(StatusCode.ERROR, httpStatusMessage);
            span.recordException(e);
            throw e;
        } finally {
            // Close the span
            Utils.addHttpHeaders(span, "response", (HeaderSet) message.get(Utils.HTTP_HEADERS));
            span.end();
        }
        return pjpReturnObject;
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
