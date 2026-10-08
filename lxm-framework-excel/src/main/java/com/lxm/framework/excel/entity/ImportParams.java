package com.lxm.framework.excel.entity;

import com.lxm.framework.excel.common.ExcelBaseParams;
import com.lxm.framework.excel.inf.IExcelVerifyHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导入参数设置
 *
 * @author twenty2
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ImportParams extends ExcelBaseParams {
    /**
     * 开始读取的sheet位置，默认为0
     */
    private int startSheetIndex = 0;

    /**
     * 上传表格需要读取的sheet 数量，默认为1
     */
    private int sheetNum = 1;

    /**
     * 表格标题行数，默认0
     */
    private int titleRows = 0;

    /**
     * 主键索引列，默认为0(第一列)，若为空则此行为无效数据
     */
    private int keyIndex = 0;

    /**
     * 字段真正值和列标题之间的行间距 默认0
     */
    private int startRows = 0;

    /**
     * 手动控制读取的行数，默认0为全读
     */
    private int readRowLen = 0;

    /**
     * 是否创建失败或成功的workbook，默认为false
     */
    private boolean moreInfo = false;

    /**
     * 是否校验，HIBERNATE 校验，默认为false
     */
    private boolean needVerify = false;

    /**
     * 校验组，needVerify为true时有效
     */
    private Class[] verifyGroup = null;

    /**
     * 自定义校验处理器
     */
    private IExcelVerifyHandler verifyHandler;
}
