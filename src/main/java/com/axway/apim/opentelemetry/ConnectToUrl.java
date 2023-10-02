package com.axway.apim.opentelemetry;

import com.vordel.circuit.Message;
import com.vordel.config.Circuit;
import com.vordel.mime.HeaderSet;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.aspectj.lang.ProceedingJoinPoint;

public class ConnectToUrl {

    private static final Tracer TRACER = Configuration.getInstance().getTracer("io.opentelemetry.axway.apim.http.HttpClient");
    private static final TextMapPropagator TEXT_MAP_PROPAGATOR = Configuration.getInstance().getPropagators().getTextMapPropagator();

    public Object httpClient(ProceedingJoinPoint pjp, Message message, Circuit circuit, HeaderSet requestHeaders, String httpVerb) throws Throwable {

        HeaderSet headerSet = (HeaderSet) message.get(Utils.HTTP_HEADERS);
        Context context = TEXT_MAP_PROPAGATOR.extract(Context.current(), headerSet, Utils.getter);
        String requestUrl = Utils.getRequestURL(message);
        Span span = TRACER.spanBuilder(requestUrl).setParent(context).setSpanKind(SpanKind.CLIENT).startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute(SemanticAttributes.HTTP_METHOD, httpVerb);
            span.setAttribute("component", "http");
            String url = (String) message.get("destinationURL");
            Utils.addHttpDetails(span, url, requestUrl, message);
            if (pjp != null) {
                try {
                    return pjp.proceed();
                } catch (Throwable e) {
                    throw e;
                }
            }
        } finally {
            span.end();
        }
        return null;
    }
}
