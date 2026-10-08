package com.lxm.framework.excel.inf;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;

/**
 * Excel 导入导出 数据处理接口
 *
 * @author twenty2
 */
public interface IExcelDataHandler {

    /**
     * 导出处理方法
     *
     * @param name  当前字段名称
     * @param value 当前值
     * @return Object
     */
    Object exportHandler(String name, Object value);

    /**
     * 导入处理方法 当前对象,当前字段名称,当前值
     *
     * @param name  当前字段名称
     * @param value 当前值
     * @return String
     */
    String importHandler(String name, Object value);

    /**
     * 获取这个字段的 Hyperlink ,07版本需要,03版本不需要
     *
     * @param creationHelper creationHelper
     * @param obj            当前对象
     * @param name           名称
     * @param value          值
     * @return Hyperlink
     */
    Hyperlink getHyperlink(CreationHelper creationHelper, Object obj, String name, Object value);
}
