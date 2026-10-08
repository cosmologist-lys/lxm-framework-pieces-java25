package com.lxm.framework.excel.exception;

import com.lxm.framework.common.AppException;
import com.lxm.framework.excel.exception.enums.ExcelExportEnum;

/**
 * 导出异常
 *
 * @author twenty2
 */
public class ExcelExportException extends AppException {

    private static final long serialVersionUID = 1L;

    private ExcelExportEnum type;

    public ExcelExportException() {
        super(100002, "");
    }

    public ExcelExportException(ExcelExportEnum type) {
        super(100002, type.getMsg());
        this.type = type;
    }

    public ExcelExportException(ExcelExportEnum type, Throwable cause) {
        super(100002, type.getMsg(), cause);
    }

    public ExcelExportException(String message) {
        super(100002, message);
    }

    public ExcelExportException(String message, ExcelExportEnum type) {
        super(100002, message);
        this.type = type;
    }

    public ExcelExportEnum getType() {
        return type;
    }

    public void setType(ExcelExportEnum type) {
        this.type = type;
    }
}
