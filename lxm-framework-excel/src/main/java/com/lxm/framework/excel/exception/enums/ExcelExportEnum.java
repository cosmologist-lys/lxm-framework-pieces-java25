package com.lxm.framework.excel.exception.enums;

/**
 * 导出异常类型枚举
 *
 * @author twenty2
 */
public enum ExcelExportEnum {

    /**
     * 导出参数错误
     */
    PARAMETER_ERROR("Excel导出参数错误"),
    /**
     * Excel导出数据错误
     */
    DATA_ERROR("Excel导出数据错误"),
    /**
     * 导出错误
     */
    EXPORT_ERROR("Excel导出错误"),
    /**
     * 导出Html流错误
     */
    HTML_ERROR("Excel导出Html流错误"),
    /**
     * 模板错误
     */
    TEMPLATE_ERROR("Excel模板错误");

    private String msg;

    ExcelExportEnum(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
