package com.lxm.framework.auth.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: Lys
 * @Date 2023/6/14
 * @Describe
 *
 *      permission or role里，任意通过1个即可
 *      即：permission 通过 || role 通过
 *      具体通过几个，跟其mode配置相关
 *
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LxmCheckOr {

    LxmCheckPermission permission();

    LxmCheckRole role();
}
