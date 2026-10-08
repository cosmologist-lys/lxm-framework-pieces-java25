package com.lxm.framework.auth.aop;

import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.auth.excpetions.LxmVagueException;
import com.lxm.framework.auth.excpetions.LxmPermissionException;
import com.lxm.framework.auth.excpetions.LxmRoleException;
import com.lxm.framework.auth.model.AuthManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @Author: Lys
 * @Date 2023/6/14
 * @Describe
 **/
public class AopAnnotationStrategy {

    public final static AopAnnotationStrategy single = new AopAnnotationStrategy();

    private AopAnnotationStrategy() {
    }


    /**
     * 检查annotation
     */
    public interface TagCheckMethodAnnotationFunction extends Consumer<Method> {
    }

    public TagCheckMethodAnnotationFunction checkAnnotation = method -> {
        // 先找class上的标记
        single.checkElementAnnotation.accept(method.getDeclaringClass());
        // 再找method上的标记
        single.checkElementAnnotation.accept(method);
    };


    /**
     * 检查annotation的元素
     */
    private interface TagCheckElementAnnotationFunction extends Consumer<AnnotatedElement> {
    }

    public TagCheckElementAnnotationFunction checkElementAnnotation = element -> {
        final LxmCheckOr checkOrAnnotation = (LxmCheckOr) single.getAnnotation.apply(element, LxmCheckOr.class);
        if (null != checkOrAnnotation) {
            single.checkOrAnnotation.accept(checkOrAnnotation);
        }
        final LxmCheckRole checkRoleAnnotation = (LxmCheckRole) single.getAnnotation.apply(element, LxmCheckRole.class);
        if (null != checkRoleAnnotation) {
            AuthManager.getPrincipleFilter().checkByAnnotation(checkRoleAnnotation);
        }
        final LxmCheckPermission checkPermissionAnnotation = (LxmCheckPermission) single.getAnnotation.apply(element, LxmCheckPermission.class);
        if (null != checkPermissionAnnotation) {
            AuthManager.getPrincipleFilter().checkByAnnotation(checkPermissionAnnotation);
        }
    };


    /**
     * 从元素上获取注解
     */
    private interface TagGetAnnotationFunction extends BiFunction<AnnotatedElement, Class<? extends Annotation>, Annotation> {
    }

    public TagGetAnnotationFunction getAnnotation = AnnotatedElement::getAnnotation;

    /**
     * 判断一个 Method 或其所属 Class 是否包含指定注解
     */
    private interface TagHasTargetAnnotationFunction extends BiFunction<Method, Class<? extends Annotation>, Boolean> {
    }

    public TagHasTargetAnnotationFunction hasTargetAnnotation = (method, clazz) -> single.getAnnotation.apply(method, clazz) != null
            || single.getAnnotation.apply(method.getDeclaringClass(), clazz) != null;

    /**
     * 校验包含@LxmCheckOr的注解
     */
    private interface TagCheckOrAnnotationFunction extends Consumer<LxmCheckOr> {
    }

    public TagCheckOrAnnotationFunction checkOrAnnotation = annotation -> {
        boolean rolePass = true;
        boolean permissionPass = true;
        try {
            AuthManager.getPrincipleFilter().checkByAnnotation(annotation.role());
        } catch (LxmRoleException re) {
            rolePass = false;
        }

        try {
            AuthManager.getPrincipleFilter().checkByAnnotation(annotation.permission());
        } catch (LxmPermissionException pe) {
            permissionPass = false;
        }
        if (rolePass || permissionPass) {
            return;
        }
        throw new LxmVagueException(AuthPrompts.UNQUALIFIED_ROLE_OR_PERMISSION);
    };
}
