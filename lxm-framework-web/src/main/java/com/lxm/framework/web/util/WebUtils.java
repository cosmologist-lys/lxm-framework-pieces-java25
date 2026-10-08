package com.lxm.framework.web.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Twenty2
 */
public class WebUtils {

    public static final String DEFAULT_LANG_PARAMETER = "lang";

    /**
     * 获取请求地址
     *
     * @param request request
     * @return ip
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        var ip = request.getHeader("x-real-ip");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }
        ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }
        ip = request.getRemoteAddr();
        ip = (null == ip) ? "" : ip;
        return ip;
    }

    /**
     * 获取请求地址
     *
     * @return ip
     */
    public static String getRemoteAddr() {
        HttpServletRequest request =
                ((ServletRequestAttributes)
                                Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                        .getRequest();
        return getRemoteAddr(request);
    }

    /**
     * 获取请求语言环境
     *
     * @param request request
     * @param name    参数名
     * @return lang
     */
    public static String getLang(HttpServletRequest request, String name) {
        String lang = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie c : cookies) {
                if (c.getName().equals(name)) {
                    lang = c.getValue();
                    break;
                }
            }
        }
        if (StringUtils.isBlank(lang)) {
            lang = request.getParameter(name);
        }
        if (StringUtils.isNotBlank(lang)) {
            if (lang.startsWith("en")) {
                lang = "en_US";
            } else if (lang.startsWith("zh")) {
                lang = "zh_CN";
            }
        }
        return lang;
    }

    /**
     * 获取请求语言环境
     *
     * @param request request
     * @param name    参数名
     * @return locale
     */
    public static Locale getLocale(HttpServletRequest request, String name) {
        String lang = getLang(request, name);
        if (StringUtils.isBlank(lang)) {
            return null;
        }
        return org.springframework.util.StringUtils.parseLocale(lang);
    }
}
