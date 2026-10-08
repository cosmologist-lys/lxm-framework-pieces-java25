package com.lxm.framework.enigma.mvc;

import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.protocol.*;
import com.lxm.framework.enigma.spi.*;
import com.lxm.framework.web.jsonresult.JsonResult;
import jakarta.servlet.http.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public final class EnigmaInterceptor implements HandlerInterceptor {
    public static final String POLICY = EnigmaInterceptor.class.getName() + ".policy";
    public static final String CONTEXT = EnigmaInterceptor.class.getName() + ".context";
    private final EnigmaProperties properties;
    private final EnigmaIdentityResolver identities;
    private final EnigmaSessionService sessions;
    private final EnigmaProtocol protocol;

    public EnigmaInterceptor(
            EnigmaProperties properties,
            EnigmaIdentityResolver identities,
            EnigmaSessionService sessions,
            EnigmaProtocol protocol) {
        this.properties = properties;
        this.identities = identities;
        this.sessions = sessions;
        this.protocol = protocol;
    }

    public static EnigmaProtected policy(HandlerMethod handler) {
        var method =
                AnnotatedElementUtils.findMergedAnnotation(
                        handler.getMethod(), EnigmaProtected.class);
        return method != null
                ? method
                : AnnotatedElementUtils.findMergedAnnotation(
                        handler.getBeanType(), EnigmaProtected.class);
    }

    public static void validate(HandlerMethod handler, RequestMappingInfo mapping) {
        var policy = policy(handler);
        if (policy == null) return;
        var returnType = ResolvableType.forMethodReturnType(handler.getMethod());
        if (returnType.resolve() == ResponseEntity.class) returnType = returnType.getGeneric(0);
        if (returnType.resolve() != JsonResult.class)
            throw new IllegalStateException(
                    "Enigma endpoints must return JsonResult or ResponseEntity<JsonResult>: "
                            + handler.getShortLogMessage());
        int bodies = 0;
        for (var parameter : handler.getMethodParameters())
            if (parameter.hasParameterAnnotation(RequestBody.class)) bodies++;
        if ((policy.value() == EnigmaProtected.Mode.ENCRYPT && bodies != 1)
                || (policy.value() == EnigmaProtected.Mode.SIGN && bodies != 0))
            throw new IllegalStateException(
                    "Enigma endpoint body contract does not match its server policy: "
                            + handler.getShortLogMessage());
        for (var type : mapping.getProducesCondition().getProducibleMediaTypes())
            if (!MediaType.APPLICATION_JSON.isCompatibleWith(type))
                throw new IllegalStateException(
                        "Protected endpoint requires JSON output: " + handler.getShortLogMessage());
        if (policy.value() == EnigmaProtected.Mode.ENCRYPT)
            for (var type : mapping.getConsumesCondition().getConsumableMediaTypes())
                if (!MediaType.APPLICATION_JSON.isCompatibleWith(type))
                    throw new IllegalStateException(
                            "Protected endpoint requires JSON input: "
                                    + handler.getShortLogMessage());
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object candidate)
            throws Exception {
        if (!(candidate instanceof HandlerMethod handler)) return true;
        var policy = policy(handler);
        if (policy == null) return true;
        request.setAttribute(POLICY, policy);
        response.setHeader("Cache-Control", "no-store");
        EnigmaTransport.validate(request, properties);
        if ("HEAD".equals(request.getMethod())) throw EnigmaException.protocol();
        var identity = identities.resolve(request);
        if (identity == null) throw EnigmaException.identity();
        var authentication =
                RequestContext.from(
                        request, policy.value().name(), properties.getPublicPathPrefix());
        var context =
                new ProtectedContext(
                        authentication, sessions.lookup(authentication, identity), identity);
        request.setAttribute(CONTEXT, context);
        if (policy.value() == EnigmaProtected.Mode.SIGN) {
            if (request.getInputStream().read() != -1) throw EnigmaException.protocol();
            protocol.verifySign(context, RequestContext.header(request, "X-Enigma-Sign"));
        } else if (request.getHeader("X-Enigma-Sign") != null) throw EnigmaException.protocol();
        return true;
    }
}
