package com.lxm.framework.auth.utils;

import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.auth.excpetions.LxmAuthException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public class SpringMVCUtil {

    /**
     * 获取当前会话的 request
     * @return request
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(servletRequestAttributes == null) {
            throw new LxmAuthException(AuthPrompts.FAILED_FETCH_CONTEXT_REQUEST);
        }
        return servletRequestAttributes.getRequest();
    }

    /**
     * 获取当前会话的 response
     * @return response
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(servletRequestAttributes == null) {
            throw new LxmAuthException(AuthPrompts.FAILED_FETCH_CONTEXT_RESPONSE);
        }
        return servletRequestAttributes.getResponse();
    }

}
