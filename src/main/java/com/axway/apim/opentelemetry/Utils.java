package com.axway.apim.opentelemetry;

import com.vordel.circuit.Message;
import com.vordel.mime.HeaderSet;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public final class Utils {

    public static final String HTTP_HEADERS = "http.headers";
    public static final String DEFAULT = "default";
    public static final String AXWAY_CORRELATION_ID = "AxwayCorrelationId";
    public static final TextMapGetter<HeaderSet> getter = new
        TextMapGetter<HeaderSet>() {
            @Override
            public Iterable<String> keys(HeaderSet carrier) {
                return convertIterableFromIterator(carrier.getHeaderNames());
            }

            @Nullable
            @Override
            public String get(@Nullable HeaderSet carrier, String key) {
                if (carrier != null) {
                    return carrier.getHeader(key);
                }
                return null;
            }
        };

    // setup context on outgoing headers
    public static final TextMapSetter<HeaderSet> setter =
            (carrier, key, value) -> {
                // Insert the context as Header
                if (carrier != null) {
                    carrier.setHeader(key, value);
                }
            };


    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static Iterable<String> convertIterableFromIterator(Iterator<String> iterator) {
        // Overriding an abstract method iterator()
        return () -> iterator;
    }

    public static String getRequestURL(Message message) {
        return message.getOrDefault("http.request.uri", message.get("http.request.path")).toString();
    }

    public static String getHttpMethod(Message message) {
        return message.getOrDefault("http.request.verb", "GET").toString();
    }

    public static void addHttpDetails(Span span, String url, String requestUri, Message message) {
             /*
         One of the following is required:
         - http.scheme, http.host, http.target
         - http.scheme, http.server_name, net.host.port, http.target
         - http.scheme, net.host.name, net.host.port, http.target
         - http.url
        */
        if (url != null) {
            span.setAttribute("http.url", url);
        } else {
            String port = (String) message.get("http.destination.port");
            String host = (String) message.get("http.destination.host");
            String protocol = (String) message.get("http.destination.protocol");
            span.setAttribute("http.scheme", "protocol");
            if (port != null && !port.isEmpty())
                span.setAttribute("http.host", host + ":" + port);
            else
                span.setAttribute("http.host", host);
            span.setAttribute("http.target", requestUri);
        }
    }


    public static void addHttpHeaders(Span span, String type, HeaderSet headers) {
        StringBuilder headerPrefix = new StringBuilder();
        headerPrefix.append(type);
        headerPrefix.append(".http.header.");
        String prefix = headerPrefix.toString();
        if (headers != null) {
            for (Map.Entry<String, HeaderSet.HeaderEntry> entry : headers.entrySet()) {
                String value = getHeaderValues(entry);
                span.setAttribute(prefix + entry.getKey(), value);
            }
        }
    }

    public static String getHeaderValues(Map.Entry<String, HeaderSet.HeaderEntry> entry) {
        return entry.getValue().stream().
            map(Object::toString).
            collect(Collectors.joining(","));
    }

}
