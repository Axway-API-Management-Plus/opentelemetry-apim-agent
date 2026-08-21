package com.axway.apim.aspects;

import com.axway.apim.opentelemetry.ConnectToUrl;
import com.axway.apim.opentelemetry.HttpServer;
import com.axway.apim.opentelemetry.Utils;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;
import com.vordel.config.Circuit;
import com.vordel.coreapireg.runtime.PathResolverResult;
import com.vordel.coreapireg.runtime.broker.ApiShunt;
import com.vordel.coreapireg.runtime.broker.InvokableMethod;
import com.vordel.mime.Body;
import com.vordel.mime.HeaderSet;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AxwayAspect {

    private static final boolean ISAPIMANAGER = Boolean.parseBoolean(System.getProperty("apimanager", "true"));


    private final HttpServer httpServer = new HttpServer();
    private final ConnectToUrl connectToUrl = new ConnectToUrl();

    /**
     * Captures policies exposed via Listener and API manager UI traffics, it does not capture servlet traffic like api manger REST API
     *
     * @param m                 m
     * @param lastChanceHandler currentApiCallStatus
     * @param context           context
     */

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
    @Around("invokeGateway(m, lastChanceHandler, context)")
    public Object invokePointcutGateway(ProceedingJoinPoint pjp, Message m, MessageProcessor lastChanceHandler, Object context) throws Throwable {

        if (!ISAPIMANAGER) {
            String requestPath = (String) m.get("http.request.path");
            String[] uriSplit = requestPath.split("/");
            String apiName = uriSplit.length == 0 ? "/" : uriSplit[1];
            String httpVerb = Utils.getHttpMethod(m);
            return httpServer.aroundHttpServer(pjp, m, apiName, httpVerb);
        } else {
            return pjp.proceed();
        }
    }

    /**
     * Captures outgoing traffic from API manager to backend services
     *
     * @param c       c
     * @param m       m
     * @param headers headers
     * @param verb    verb
     * @param body    body
     */
    @Pointcut("execution(* com.vordel.circuit.net.ConnectionProcessor.invoke(..)) && args (c, m, headers, verb, body)")
    public void invokeConnectToUrl(Circuit c, Message m, HeaderSet headers, String verb, Body body) {

    }

    /**
     * Captures outgoing traffic from API manager to backend services
     *
     * @param pjp     pjp
     * @param c       c
     * @param m       m
     * @param headers headers
     * @param verb    verb
     * @param body    body
     * @return pjp object
     * @throws Throwable
     */
    @Around("invokeConnectToUrl(c, m, headers, verb, body)")
    public Object invokeConnectToUrlAroundAdvice(ProceedingJoinPoint pjp, Circuit c, Message m, HeaderSet headers, String verb, Body body) throws Throwable {
        return connectToUrl.httpClient(pjp, m, c, headers, verb);
    }

    /**
     * Captures api manager traffic
     *
     * @param m                    m
     * @param runMethod            runMethod
     * @param resolvedMethod       resolvedMethod
     * @param currentApiCallStatus currentApiCallStatus
     */

    @Pointcut("execution(* com.vordel.coreapireg.runtime.APIBroker.invokeMethod(..)) && args (m,  runMethod, resolvedMethod,  currentApiCallStatus)")
    public void invokeMethodPointcut(Message m, InvokableMethod runMethod,
                                     PathResolverResult resolvedMethod,
                                     ApiShunt currentApiCallStatus) {
    }

    /**
     * Captures api manager traffic
     *
     * @param pjp                  pjp
     * @param m                    message
     * @param runMethod            runMethod
     * @param resolvedMethod       resolvedMethod
     * @param currentApiCallStatus currentApiCallStatus
     * @return pjp object
     * @throws Throwable
     */
    @Around("invokeMethodPointcut( m,  runMethod, resolvedMethod, currentApiCallStatus)")
    public Object invokeMethodAroundAdvice(ProceedingJoinPoint pjp, Message m,
                                           InvokableMethod runMethod,
                                           PathResolverResult resolvedMethod, ApiShunt currentApiCallStatus) throws Throwable {
        String[] uriSplit = Utils.getRequestURL(m).split("/");
        String apiName;
        apiName = Utils.getOrDefault(m, "api.name", uriSplit[1]);
        String httpMethod = Utils.getHttpMethod(m);
        return httpServer.aroundHttpServer(pjp, m, apiName, httpMethod);
    }

}
