package com.lxm.framework.auth.model.imf;

import com.lxm.framework.auth.model.LxmResponse;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public class LxmSpringResponse implements LxmResponse {

    private final HttpServletResponse response;

    public LxmSpringResponse(HttpServletResponse response) {
        this.response = response;
    }

    @Override

    public HttpServletResponse getSource() {
        return this.response;
    }

    @Override
    public void deleteCookie(@NonNull String name) {
        addCookie(name, null, null, null, 0);
    }

    @Override
    public void addCookie(@NonNull String name, String value, String path, String domain, int timeout) {
        final Cookie cookie = new Cookie(name.trim(), value);
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        if (StringUtils.isNotBlank(domain)) {
            cookie.setDomain(domain);
        }
        cookie.setPath(path);
        if (timeout < 0) {
            timeout = 0;
        }
        cookie.setMaxAge(timeout);
        this.response.addCookie(cookie);
    }
}
