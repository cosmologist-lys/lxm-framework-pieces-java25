package com.lxm.framework.auth.model;

import lombok.NonNull;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public interface LxmRequest {

    static final Class<HttpServletRequest> httpServletRequestClass = HttpServletRequest.class;

    /**
     * 获取底层源对象
     * @return see note
     */
    HttpServletRequest getSource();

    /**
     * 在 [请求体] 里获取一个值
     * @param name 键
     * @return 值
     */
    String getParameter(@NonNull String name);

    /**
     * 在 [请求头] 里获取一个值
     * @param name 键
     * @return 值
     */
    String getHeader(@NonNull String name);

    /**
     * 在 [Cookie作用域] 里获取一个值
     * @param name 键
     * @return 值
     */
    String getCookieValue(@NonNull String name);

    /**
     * 返回当前请求的URL
     * @return see note
     */
    String getRequestURI();

    /**
     * 在 [Request作用域] 里写入一个值
     * @param key 键
     * @param value 值
     */
    void set(@NonNull String key, @NonNull Object value);

    /**
     * 在 [Request作用域] 里获取一个值
     * @param key 键
     * @return 值
     */
    Object get(@NonNull String key);

    /**
     * 在 [Request作用域] 里删除一个值
     * @param key 键
     */
    void delete(@NonNull String key);
}
