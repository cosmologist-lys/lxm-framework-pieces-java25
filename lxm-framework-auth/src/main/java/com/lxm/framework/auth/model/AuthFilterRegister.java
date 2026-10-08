package com.lxm.framework.auth.model;

import com.lxm.framework.auth.filter.ServletErrorStrategy;
import com.lxm.framework.auth.filter.ServletFilterStrategy;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/6/13
 * @Describe 拦截器，执行url过滤和拦截
 **/
public interface AuthFilterRegister {

    /**
     * 需要拦截的uri 例如：/api/v1/**
     *
     * @return
     */
    List<String> includes();

    /**
     * 需要放行，不进行权限校验的uri 例如：/auth/sign-in
     *
     * @return
     */
    List<String> excludes();

    /**
     * 当前访问的uri是需要拦截的uri时，需要执行的内容
     */
    ServletFilterStrategy whenBlocked();

    /**
     * 被 whenBlocked 执行时或许会抛错，然后被此方法捕获
     * 对于捕获的错误，可以根据具体情况返回json-string
     * 最后输出流会写出此内容返回请求方
     *
     * @return
     */
    ServletErrorStrategy whenError();
}
