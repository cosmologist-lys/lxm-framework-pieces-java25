package com.lxm.framework.auth.model.imf;

import com.lxm.framework.auth.model.LxmRequest;
import lombok.NonNull;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public class LxmSpringRequest implements LxmRequest {

    private final HttpServletRequest request;

    public LxmSpringRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public HttpServletRequest getSource() {
        return this.request;
    }

    @Override
    public String getParameter(@NonNull String name) {
        return this.request.getParameter(name.trim());
    }

    @Override
    public String getHeader(@NonNull String name) {
        return this.request.getHeader(name.trim());
    }

    @Override
    public String getCookieValue(@NonNull String name) {
        final Cookie[] cookies = this.request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name.trim())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public String getRequestURI() {
        return this.request.getRequestURI();
    }

    @Override
    public void set(@NonNull String key, @NonNull Object value) {
        this.request.setAttribute(key.trim(), value);
    }

    @Override
    public Object get(@NonNull String key) {
        return this.request.getAttribute(key.trim());
    }

    @Override
    public void delete(@NonNull String key) {
        this.request.removeAttribute(key.trim());
    }
}
