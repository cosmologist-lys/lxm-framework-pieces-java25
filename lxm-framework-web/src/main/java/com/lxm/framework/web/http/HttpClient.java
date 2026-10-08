package com.lxm.framework.web.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.Method;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.lxm.framework.common.AppException;
import com.lxm.framework.common.cache.caffeine.CaffeineCache;
import com.lxm.framework.common.utils.CryptoUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Lys
 * @Date 2023/1/11
 * @Describe
 **/
@Slf4j
public class HttpClient {

    private final static ObjectMapper objectMapper;
    protected final static int TIMEOUT_MILLIS = 5000;
    // 是否使用缓存
    private final static AtomicBoolean useCache;

    static {
        useCache = new AtomicBoolean(false);
        objectMapper = com.lxm.framework.web.jackon.FrameworkJackson.mapper();
    }

    public static <T> T get(
            @NonNull String url, Map<String, Object> requestParams, @NonNull Class<T> returnClass) {
        return execute(
                url, HttpMethod.GET, requestParams, null, null, null, null, returnClass, null);
    }

    public static <T> T get(
            @NonNull String url,
            Map<String, Object> requestParams,
            @NonNull TypeReference<T> typeReference) {
        return execute(
                url, HttpMethod.GET, requestParams, null, null, null, null, null, typeReference);
    }

    public static <T> T post(
            @NonNull String url,
            @NonNull Object requestBody,
            Map<String, Object> requestParams,
            @NonNull Class<T> returnClass) {
        return execute(
                url,
                HttpMethod.POST,
                requestParams,
                requestBody,
                null,
                null,
                null,
                returnClass,
                null);
    }

    public static <T> T post(
            @NonNull String url,
            @NonNull Object requestBody,
            Map<String, Object> requestParams,
            @NonNull TypeReference<T> typeReference) {
        return execute(
                url,
                HttpMethod.POST,
                requestParams,
                requestBody,
                null,
                null,
                null,
                null,
                typeReference);
    }

    public static <T> T put(
            @NonNull String url, Map<String, Object> requestParams, @NonNull Class<T> returnClass) {
        return execute(
                url, HttpMethod.PUT, requestParams, null, null, null, null, returnClass, null);
    }

    public static <T> T put(
            @NonNull String url,
            Map<String, Object> requestParams,
            @NonNull TypeReference<T> typeReference) {
        return execute(
                url, HttpMethod.PUT, requestParams, null, null, null, null, null, typeReference);
    }

    public static <T> T delete(
            @NonNull String url, Map<String, Object> requestParams, @NonNull Class<T> returnClass) {
        return execute(
                url, HttpMethod.DELETE, requestParams, null, null, null, null, returnClass, null);
    }

    public static <T> T delete(
            @NonNull String url,
            Map<String, Object> requestParams,
            @NonNull TypeReference<T> typeReference) {
        return execute(
                url, HttpMethod.DELETE, requestParams, null, null, null, null, null, typeReference);
    }

    public static void useCache(boolean use) {
        useCache.set(use);
    }

    /**
     * @param url           url
     * @param httpMethod    method
     * @param returnClass   return-class
     * @param requestParams request params
     * @param requestBody   request body
     * @param headers       headers
     * @param cookies       cookies
     * @param <T>           generic
     * @return target generic
     */
    public static <T> T execute(
            @NonNull String url,
            @NonNull HttpMethod httpMethod,
            Map<String, Object> requestParams,
            Object requestBody,
            MediaType mediaType,
            Map<String, String> headers,
            Map<String, String> cookies,
            Class<T> returnClass,
            TypeReference<T> typeReference) {
        String finalUrl = link(url, requestParams);
        var req = HttpRequest.of(finalUrl).method(toMethod(httpMethod)).timeout(TIMEOUT_MILLIS);
        setHeaders(req, headers);
        setCookies(req, cookies);
        setBody(req, requestBody);
        setContentType(req, mediaType);
        if (cookies == null || cookies.isEmpty()) req.disableCookie();
        String cacheKey = null;
        if (useCache.get()
                && httpMethod == HttpMethod.GET
                && requestBody == null
                && (headers == null || headers.isEmpty())
                && (cookies == null || cookies.isEmpty())) {
            cacheKey = "lfp:http:" + finalUrl;
            String cached = CaffeineCache.read(cacheKey, String.class);
            if (cached != null) return mapping(cached, returnClass, typeReference, null);
        }
        try (var response = req.execute()) {
            if (!response.isOk())
                throw new AppException(
                        response.getStatus(),
                        "HTTP request failed with status " + response.getStatus());
            String body = response.body();
            T result = mapping(body, returnClass, typeReference, null);
            if (cacheKey != null) CaffeineCache.write(cacheKey, body);
            return result;
        }
    }

    /**
     * 请求之后的映射
     *
     * @param requestBodyString 请求结果的string
     * @param returnClass       返回的class
     * @param typeReference     返回的type-reference
     * @param md5               如果有缓存，就有md5的key
     * @param <T>               类型
     * @return T
     */
    private static <T> T mapping(
            String requestBodyString,
            Class<T> returnClass,
            TypeReference<T> typeReference,
            String md5) {
        try {
            T result;
            if (null != typeReference) {
                final TypeReference<String> StringTypeReference = new TypeReference<>() {};
                // 如果要返回的类型是string，就不用转换了，下面同理
                if (StringTypeReference.getType().equals(typeReference.getType())) {
                    return (T) requestBodyString;
                }
                result = objectMapper.readValue(requestBodyString, typeReference);
            } else {
                if (String.class.equals(returnClass)) {
                    return (T) requestBodyString;
                }
                result = objectMapper.readValue(requestBodyString, returnClass);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("HTTP response cannot be decoded", e);
        }
    }

    /**
     * 若存在header信息
     *
     * @param request 请求
     * @param headers headers
     */
    private static void setHeaders(@NonNull HttpRequest request, Map<String, String> headers) {
        if (Objects.nonNull(headers) && !headers.isEmpty()) {
            headers.forEach(request::header);
        }
    }

    /**
     * 若存在cookie信息
     *
     * @param request 请求
     * @param cookies cookie信息
     */
    private static void setCookies(@NonNull HttpRequest request, Map<String, String> cookies) {
        if (Objects.nonNull(cookies) && !cookies.isEmpty()) {
            var cookieList = new ArrayList<HttpCookie>();
            cookies.forEach((k, v) -> cookieList.add(new HttpCookie(k, v)));
            request.cookie(cookieList);
        }
    }

    /**
     * 若存在body信息
     *
     * @param request 请求
     * @param body    body
     */
    private static void setBody(@NonNull HttpRequest request, Object body) {
        if (body != null) {
            if (request.getMethod() == Method.GET || request.getMethod() == Method.HEAD)
                throw new IllegalArgumentException("Body is not supported for this method");
            request.body(objectMapper.writeValueAsString(body));
        }
    }

    /**
     * 若存在media-type
     *
     * @param request   请求
     * @param mediaType media-type
     */
    private static void setContentType(@NonNull HttpRequest request, MediaType mediaType) {
        if (Objects.isNull(mediaType)) {
            mediaType = MediaType.APPLICATION_JSON;
        }
        request.contentType(mediaType.toString());
    }

    /**
     * 若使用cache
     *
     * @param request     req
     * @param requestBody req-body
     * @return string
     */
    private static Method toMethod(HttpMethod httpMethod) {
        switch (httpMethod.name()) {
            case "GET" -> {
                return Method.GET;
            }
            case "POST" -> {
                return Method.POST;
            }
            case "PUT" -> {
                return Method.PUT;
            }
            case "DELETE" -> {
                return Method.DELETE;
            }
        }
        throw new AppException(-1, "http-client 请求类型只支持GET POST PUT DELETE");
    }

    private static String link(@NonNull String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) return url;
        int fragment = url.indexOf('#');
        String suffix = fragment < 0 ? "" : url.substring(fragment);
        String base = fragment < 0 ? url : url.substring(0, fragment);
        var result = new StringBuilder(base);
        params.forEach(
                (key, value) -> {
                    if (key != null && value != null) {
                        if (result.indexOf("?") < 0) result.append('?');
                        else if (result.charAt(result.length() - 1) != '?'
                                && result.charAt(result.length() - 1) != '&') result.append('&');
                        result.append(
                                java.net.URLEncoder.encode(
                                        key, java.nio.charset.StandardCharsets.UTF_8));
                        result.append('=')
                                .append(
                                        java.net.URLEncoder.encode(
                                                String.valueOf(value),
                                                java.nio.charset.StandardCharsets.UTF_8));
                    }
                });
        return result.append(suffix).toString();
    }
}
