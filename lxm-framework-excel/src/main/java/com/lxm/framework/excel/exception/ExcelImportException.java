package com.lxm.framework.excel.exception;

import com.lxm.framework.common.AppException;
import com.lxm.framework.excel.exception.enums.ExcelImportEnum;

/**
 * 导入异常
 *
 * @author twenty2
 */
public class ExcelImportException extends AppException {

    private static final long serialVersionUID = 1L;

    private ExcelImportEnum type;

    public ExcelImportException() {
        super(100001, "");
    }

    public ExcelImportException(ExcelImportEnum type) {
        super(100001, type.getMsg());
        this.type = type;
    }

    public ExcelImportException(ExcelImportEnum type, Throwable cause) {
        super(100001, type.getMsg(), cause);
    }

    public ExcelImportException(String message) {
        super(100001, message);
    }

    public ExcelImportException(String message, ExcelImportEnum type) {
        super(100001, message);
        this.type = type;
    }

    public ExcelImportException(String message, Throwable cause) {
        super(500, message, cause);
    }

    public ExcelImportEnum getType() {
        return type;
    }

    public void setType(ExcelImportEnum type) {
        this.type = type;
    }
}
