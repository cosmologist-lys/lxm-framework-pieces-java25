package com.lxm.framework.enigma.mvc;

import com.lxm.framework.enigma.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;

/** 代理终止 TLS 时，由容器的可信代理配置提供 isSecure；不读取客户端自报的转发头。 */
final class EnigmaTransport {
    private EnigmaTransport() { }
    static void validate(HttpServletRequest request, EnigmaProperties properties) {
        boolean local=("127.0.0.1".equals(request.getRemoteAddr()) || "::1".equals(request.getRemoteAddr()) || "0:0:0:0:0:0:0:1".equals(request.getRemoteAddr())) &&
            ("localhost".equalsIgnoreCase(request.getServerName()) || "127.0.0.1".equals(request.getServerName()) || "::1".equals(request.getServerName()) || "[::1]".equals(request.getServerName()));
        if (!request.isSecure() && !(properties.isAllowInsecureLocalhost() && local)) throw EnigmaException.identity();
        String origin=request.getHeader("Origin");
        if (origin!=null) {
            if (Collections.list(request.getHeaders("Origin")).size()!=1) throw EnigmaException.identity();
            int port=request.getServerPort();
            String own=request.getScheme()+"://"+request.getServerName()+((port==443 && request.isSecure()) || (port==80 && !request.isSecure())?"":":"+port);
            if (!origin.equals(own) && !properties.getAllowedOrigins().contains(origin)) throw EnigmaException.identity();
        }
    }
}
