package com.lxm.framework.auth.model;

import lombok.NonNull;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public interface LxmResponse {

    /**
     * 获取底层源对象
     *
     * @return see note
     */
    HttpServletResponse getSource();

    /**
     * 删除指定Cookie
     *
     * @param name Cookie名称
     */
    void deleteCookie(@NonNull String name);

    /**
     * 写入指定Cookie
     *  @param name    Cookie名称
     * @param value   Cookie值
     * @param path    Cookie路径
     * @param domain  Cookie的作用域
     * @param timeout 过期时间 （秒）
     */
    void addCookie(@NonNull String name, String value, String path, String domain, int timeout);
}
