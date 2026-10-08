package com.lxm.framework.auth.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @Author: Lys
 * @Date 2023/6/13
 * @Describe
 **/
@Aspect

@Order(Integer.MIN_VALUE)
public class LxmAnnotationAspect {

    public LxmAnnotationAspect() {
    }

    private static final String POINT_CUT_TAG =
            "@within(com.lxm.framework.auth.aop.LxmCheckPermission) || @annotation(com.lxm.framework.auth.aop.LxmCheckPermission) || "
                    + "@within(com.lxm.framework.auth.aop.LxmCheckRole) || @annotation(com.lxm.framework.auth.aop.LxmCheckRole) || "
                    + "@within(com.lxm.framework.auth.aop.LxmCheckOr) || @annotation(com.lxm.framework.auth.aop.LxmCheckOr)";

    @Pointcut(POINT_CUT_TAG)
    public void pointcut() {
    }

    /**
     * 环绕切入
     *
     * @param joinPoint 切面对象
     * @return 底层方法执行后的返回值
     * @throws Throwable 底层方法抛出的异常
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取对应的 Method 处理函数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 注解鉴权
        AopAnnotationStrategy.single.checkAnnotation.accept(method);
        // 执行原有逻辑
        return joinPoint.proceed();
    }
}
