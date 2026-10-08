package com.lxm.framework.excel.parse.result;

import lombok.Data;

/**
 * Excel标记类
 * @author twenty2
 */
@Data
public class ExcelImportErrorResult {

    /**
     * 错误数据描述
     */
    private String errorMsg;

    /**
     * 错误行号
     */
    private int rowNum;

    /**
     * 错误数据
     */
    private Object object;

}
