package com.axway.apim.aspects;

import com.axway.apim.opentelemetry.ConnectToUrl;
import com.axway.apim.opentelemetry.HttpServer;
import com.axway.apim.opentelemetry.Utils;
import com.vordel.circuit.InvocationContext;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;
import com.vordel.config.Circuit;
import com.vordel.coreapireg.runtime.PathResolverResult;
import com.vordel.coreapireg.runtime.broker.ApiShunt;
import com.vordel.coreapireg.runtime.broker.InvokableMethod;
import com.vordel.dwe.http.ServerTransaction;
import com.vordel.mime.Body;
import com.vordel.mime.HeaderSet;
import com.vordel.trace.Trace;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AxwayAspect {

    private HttpServer httpServer = new HttpServer();
    private ConnectToUrl connectToUrl = new ConnectToUrl();


    @Pointcut("execution(* com.vordel.circuit.SyntheticCircuitChainProcessor.invoke(..)) && args (m, lastChanceHandler, context)")
    public void invokeGateway(Message m, MessageProcessor lastChanceHandler, Object context) {

    }

    /**
     * Captures policies exposed via Listener and API manager UI traffics, it does not capture servlet traffic like api manger REST API
     *
     * @param m                 m
     * @param lastChanceHandler currentApiCallStatus
     * @param context           context
     * @return context object
     * @throws Throwable
     */
    @After("invokeGateway(m, lastChanceHandler, context)")
    public void invokePointcutGateway(Message m, MessageProcessor lastChanceHandler, Object context) throws Throwable {
        String[] uriSplit = ((String) m.get("http.request.path")).split("/");
        String alternateApiName = uriSplit.length == 0 ? "/" : uriSplit[1];
        String apiName = (String) m.getOrDefault("service.name", alternateApiName);
        Trace.debug("Dynatrace :: Service Name : " + apiName);
        String apiContextRoot = "/";
        apiContextRoot = (String) m.getOrDefault("api.path", apiContextRoot);
        String httpVerb = Utils.getHttpMethod(m);
        httpServer.aroundHttpServer(null, m, apiName, httpVerb, apiContextRoot);
    }

    @Pointcut("execution(* com.vordel.circuit.net.ConnectionProcessor.invoke(..)) && args (c, m, headers, verb, body)")
    public void invokeConnectToUrl(Circuit c, Message m, HeaderSet headers, String verb, Body body) {

    }

    @Around("invokeConnectToUrl(c, m, headers, verb, body)")
    public Object invokeConnectToUrlAroundAdvice(ProceedingJoinPoint pjp, Circuit c, Message m, HeaderSet headers, String verb, Body body) throws Throwable {
        return connectToUrl.httpClient(pjp, m, c, headers, verb);
    }

    @Pointcut("execution(* com.vordel.coreapireg.runtime.CoreApiBroker.invokeMethod(..)) && args (txn, m, lastChanceHandler, runMethod, resolvedMethod, matchCount, httpMethod, currentApiCallStatus)")
    public void invokeMethodPointcut(ServerTransaction txn, Message m,
                                     MessageProcessor lastChanceHandler, InvokableMethod runMethod,
                                     final PathResolverResult resolvedMethod, final int matchCount,
                                     String httpMethod, ApiShunt currentApiCallStatus) {

    }

    /**
     * Captures api manager traffic
     *
     * @param pjp                  pjp
     * @param txn                  txt
     * @param m                    message
     * @param lastChanceHandler    lastChanceHandler
     * @param runMethod            runMethod
     * @param resolvedMethod       resolvedMethod
     * @param matchCount           matchCount
     * @param httpMethod           httpMethod
     * @param currentApiCallStatus currentApiCallStatus
     * @return pjp object
     * @throws Throwable
     */
    @Around("invokeMethodPointcut(txn, m, lastChanceHandler, runMethod, resolvedMethod, matchCount, httpMethod, currentApiCallStatus)")
    public Object invokeMethodAroundAdvice(ProceedingJoinPoint pjp, ServerTransaction txn, Message m,
                                           MessageProcessor lastChanceHandler, InvokableMethod runMethod,
                                           final PathResolverResult resolvedMethod, final int matchCount,
                                           String httpMethod, ApiShunt currentApiCallStatus) throws Throwable {
        String[] uriSplit = Utils.getRequestURL(m).split("/");
        String apiName;
        String apiContextRoot = "/";
        apiName = (String) m.getOrDefault("api.name", uriSplit[1]);
        apiContextRoot = (String) m.getOrDefault("api.path", apiContextRoot);
        return httpServer.aroundHttpServer(pjp, m, apiName, httpMethod, apiContextRoot);
    }


    @Pointcut("execution(* com.vordel.coreapireg.runtime.CoreApiBroker.invokeFaultHandler(..)) && args (shuntReason, m, ctx)")
    public void apiManagerFaultHandler(ApiShunt shuntReason, Message m, InvocationContext ctx) {

    }

    @Around("apiManagerFaultHandler(shuntReason, m, ctx)")
    public Object handleApiManagerFaultHandler(ProceedingJoinPoint pjp, ApiShunt shuntReason, Message m, InvocationContext ctx) throws Throwable {
        // Only handle API not found case
        if (shuntReason.getStatusCode() == 404) {
            return httpServer.aroundHttpServer(pjp, m, "NotFound", "/", null);
        }
        return pjp.proceed();
    }
}
