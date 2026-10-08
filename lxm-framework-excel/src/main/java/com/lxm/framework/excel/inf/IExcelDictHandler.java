package com.lxm.framework.excel.inf;

/**
 * @author twenty2
 */
public interface IExcelDictHandler {

    /**
     * 从值翻译到名称
     *
     * @param dict  字典Key
     * @param name  属性名称
     * @param value 属性值
     * @return 返回值
     */
    String exportHandler(String dict, String name, Object value);

    /**
     * 从名称翻译到值
     *
     * @param dict  字典Key
     * @param name  属性名称
     * @param value 属性值
     * @return 返回值
     */
    String importHandler(String dict, String name, Object value);
}
