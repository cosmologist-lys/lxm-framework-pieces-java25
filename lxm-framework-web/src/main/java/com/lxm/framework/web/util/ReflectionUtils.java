package com.lxm.framework.web.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;


@Component
public class ReflectionUtils {

    private static ApplicationContext context;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        ReflectionUtils.context = context;
    }

    /**
     * 反射方法
     *
     * @param beanMethod 例如 reflectionUtils.invokeMethod，类名在前(spring type id)方法名在后点号(.)分隔
     * @param params     方法参数，统一使用map传递
     * @return Object 返回值
     */
    public static Object invokeMethod(String beanMethod, Map<String, Object> params) {
        String[] a = StringUtils.split(beanMethod, ".");
        String beanName = a[0];
        String methodName = a[1];
        Object bean = context.getBean(beanName);
        Method method;
        Object o;

        if (null == params) {
            method = org.springframework.util.ReflectionUtils.findMethod(bean.getClass(), methodName);
            if (method == null) {
                return null;
            }
            o = org.springframework.util.ReflectionUtils.invokeMethod(method, bean);
        } else {
            method = org.springframework.util.ReflectionUtils.findMethod(bean.getClass(), methodName, Map.class);
            if (method == null) {
                return null;
            }
            o = org.springframework.util.ReflectionUtils.invokeMethod(method, bean, params);
        }
        return o;
    }
}
