package com.lxm.framework.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

/**
 * 导出的集合
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelCollection {
    /**
     * 导出列名
     */
    String name();

    /**
     * 排序
     */
    int orderNum() default 0;

    /**
     * 创建时创建的类型 默认值是 arrayList
     */
    Class<?> type() default ArrayList.class;

    /**
     * 是否为导入字段，导入时有效，优先级高于集合元素所有excel注解的importField字段值
     */
    boolean importField() default false;
}
