package com.lxm.framework.excel.inf;


import com.lxm.framework.excel.parse.result.ExcelVerifyHandlerResult;

/**
 * 导入校验接口
 */
public interface IExcelVerifyHandler<T> {

    /**
     * 导入校验方法
     *
     * @param obj 验证对象
     * @return ExcelVerifyHandlerResult
     */
    ExcelVerifyHandlerResult verifyHandler(T obj);

}
