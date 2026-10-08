package com.lxm.framework.auth.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: Lys
 * @Date 2023/6/13
 * @Describe
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface LxmCheckPermission {

    /**
     * 需要校验的角色标识 [ 数组 ]
     *
     * @return /
     */
    String [] value() default {};

    /**
     * 验证模式：AND | OR，默认AND
     *
     * @return /
     */
    LxmMode mode() default LxmMode.AND;
}
