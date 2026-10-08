package com.lxm.framework.excel.parse.result;

import lombok.Data;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * 导入返回类
 *
 * @author twenty2
 */
@Data
public class ExcelImportResult<T> {

    /**
     * 结果集
     */
    private List<T> list;
    /**
     * 失败数据
     */
    private List<ExcelImportErrorResult> failList;
    /**
     * 数据处理器失败数据
     */
    private List<ExcelVerifyHandlerResult> failHandlerList;

    /**
     * 成功的数据源
     */
    private Workbook successWorkbook;
    /**
     * 失败的数据源
     */
    private Workbook failWorkbook;

}
