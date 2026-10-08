package com.lxm.framework.mybatisplus.interceptor;

import org.apache.ibatis.plugin.Invocation;

/**
 * @Author: Lys
 * @Date 2022/2/25
 * @Describe
 **/
public abstract class AbstractInterceptor {

    protected final String INTERCEPT_FLAG = "$";

    abstract Object invoke(Invocation invocation) throws Exception;
}
