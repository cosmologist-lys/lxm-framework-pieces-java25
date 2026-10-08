package com.lxm.framework.enigma.mvc;

import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.protocol.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import java.io.*;
import java.lang.reflect.Type;

@ControllerAdvice
public final class EnigmaBodyAdvice extends RequestBodyAdviceAdapter {
    private final EnigmaProtocol protocol;
    private final EnigmaProperties properties;

    public EnigmaBodyAdvice(EnigmaProtocol protocol, EnigmaProperties properties) {
        this.protocol = protocol;
        this.properties = properties;
    }

    private ProtectedContext context() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servlet)) return null;
        return (ProtectedContext) servlet.getRequest().getAttribute(EnigmaInterceptor.CONTEXT);
    }

    public boolean supports(
            MethodParameter parameter,
            Type type,
            Class<? extends HttpMessageConverter<?>> converter) {
        var context = context();
        return context != null && "ENCRYPT".equals(context.request.mode());
    }

    @Override
    public HttpInputMessage beforeBodyRead(
            HttpInputMessage message,
            MethodParameter parameter,
            Type type,
            Class<? extends HttpMessageConverter<?>> converter)
            throws IOException {
        var context = context();
        if (context == null
                || !org.springframework.http.converter.json.JacksonJsonHttpMessageConverter.class
                        .isAssignableFrom(converter)) throw EnigmaException.protocol();
        byte[] encrypted = message.getBody().readNBytes(properties.getMaxEnvelopeBytes() + 1);
        if (encrypted.length > properties.getMaxEnvelopeBytes()) throw EnigmaException.protocol();
        byte[] plaintext = protocol.decrypt(context, encrypted);
        var headers = new HttpHeaders();
        headers.putAll(message.getHeaders());
        headers.setContentLength(plaintext.length);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpInputMessage() {
            public InputStream getBody() {
                return new ByteArrayInputStream(plaintext);
            }

            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }

    @Override
    public Object handleEmptyBody(
            Object body,
            HttpInputMessage message,
            MethodParameter parameter,
            Type type,
            Class<? extends HttpMessageConverter<?>> converter) {
        throw EnigmaException.protocol();
    }
}
