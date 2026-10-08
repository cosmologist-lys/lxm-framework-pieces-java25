package com.lxm.framework.auth.filter;

/**
 * @Author: Lys
 * @Date 2023/5/30
 * @Describe
 **/
@FunctionalInterface
public interface ServletErrorStrategy {

    /** 这里可以返回一个json string */
    String run(Throwable r);
}
