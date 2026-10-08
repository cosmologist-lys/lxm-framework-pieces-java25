package com.lxm.framework.excel.common;

import com.lxm.framework.excel.inf.IExcelDataHandler;
import com.lxm.framework.excel.inf.IExcelDictHandler;
import lombok.Data;

/**
 * 基础参数
 *
 * @author twenty2
 */
@Data
public class ExcelBaseParams {

    /**
     * 数据处理接口,以此为主,replace,format都在这后面
     */
    private IExcelDataHandler dataHandler;
    /**
     * 字段处理类
     */
    private IExcelDictHandler dictHandler;
}
