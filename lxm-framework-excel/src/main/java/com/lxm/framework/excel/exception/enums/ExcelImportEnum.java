package com.lxm.framework.excel.exception.enums;

/**
 * 导出异常类型枚举
 * @author twenty2
 */
public enum ExcelImportEnum {
    /**
     * 导入参数错误
     */
    PARAMETER_ERROR("参数错误"),
    /**
     * 非法导入模板
     */
    IS_NOT_A_VALID_TEMPLATE("不是合法的Excel模板"),
    /**
     * 导入值获取失败
     */
    GET_VALUE_ERROR("Excel值获取失败"),
    /**
     * 导入值校验失败
     */
    VERIFY_ERROR("值校验失败");

    private String msg;

    ExcelImportEnum(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
