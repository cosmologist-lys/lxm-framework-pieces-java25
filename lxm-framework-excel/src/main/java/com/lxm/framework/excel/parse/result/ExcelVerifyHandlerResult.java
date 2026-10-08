package com.lxm.framework.excel.parse.result;

import lombok.Data;

/**
 * Excel导入处理返回结果
 *
 * @author twenty2
 */
@Data
public class ExcelVerifyHandlerResult {

    /**
     * 是否正确
     */
    private boolean success;

    /**
     * 错误行号
     */
    private int rowNum;

    /**
     * 错误信息
     */
    private String msg;

    public ExcelVerifyHandlerResult() {}

    public ExcelVerifyHandlerResult(boolean success) {
        this.success = success;
    }

    public ExcelVerifyHandlerResult(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public static ExcelVerifyHandlerResult fail(String msg) {
        return new ExcelVerifyHandlerResult(false, msg);
    }

    public static ExcelVerifyHandlerResult success() {
        return new ExcelVerifyHandlerResult(true);
    }
}
