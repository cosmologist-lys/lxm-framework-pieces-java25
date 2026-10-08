package com.lxm.framework.excel.parse.entity;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * excel 导入工具类,对cell类型做映射
 *
 * @author JueYue
 * @version 1.0 2013年8月24日
 */
@Data
public class ExcelImportEntity {
    /**
     * 列名
     */
    protected String name;

    /**
     * 导出日期格式
     */
    private String format;

    /**
     * 替换
     */
    private String[] replace;

    /**
     * 字典名称
     */
    private String dict;

    /**
     * 后缀
     */
    private String suffix;

    /**
     * 枚举导入静态方法
     */
    private String enumImportMethod;

    /**
     * set方法
     */
    private Method setMethod;

    /**
     * get方法
     */
    private Method getMethod;
}
