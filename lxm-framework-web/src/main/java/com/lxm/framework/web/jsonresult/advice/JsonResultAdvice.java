package com.lxm.framework.web.jsonresult.advice;

import com.lxm.framework.common.web.Result;
import com.lxm.framework.web.jsonresult.JsonResult;
import com.lxm.framework.web.jsonresult.annotation.JsonResultFilter;
import com.lxm.framework.web.jsonresult.annotation.JsonResultFilters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * @author twenty2
 */
@Slf4j
@RestControllerAdvice
public class JsonResultAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends org.springframework.http.converter.HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends org.springframework.http.converter.HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (o == null) {
            return null;
        }
        if (o instanceof JsonResult<?> jsonResult) {
            if (methodParameter.hasMethodAnnotation(JsonResultFilter.class) || methodParameter.hasMethodAnnotation(JsonResultFilters.class)) {
                Annotation[] methodAnnotations = methodParameter.getMethodAnnotations();
                Arrays.asList(methodAnnotations).forEach(item -> {
                    if (item instanceof JsonResultFilter) {
                        var jsonResultFilter = (JsonResultFilter) item;
                        jsonResult.filter(jsonResultFilter);
                    } else if (item instanceof JsonResultFilters) {
                        var jsonResultFilters = (JsonResultFilters) item;
                        Arrays.asList(jsonResultFilters.value()).forEach(jsonResult::filter);
                    }
                });
            }
            return jsonResult;
        } else if (o instanceof Result<?> result) {
            return result;
        }
        return o;
    }
}
