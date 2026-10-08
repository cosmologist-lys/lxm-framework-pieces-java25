package com.lxm.framework.excel.entity;

import com.lxm.framework.excel.export.entity.ExcelExportEntity;
import lombok.Data;

import java.util.List;

/**
 * 每一个sheet
 *
 * @author Lys.
 * @date 2020/10/28 下午3:51
 */
@Data
public class ExcelSingleSheet {

    /**
     * 每个sheet的抬头
     */
    private String sheetTitle;

    /**
     * 导出类型
     */
    private Class<?> pojoClass;

    /**
     * 导出的数据列表
     */
    private List<?> dataList;

    /**
     * 参数
     */
    private ExportParams params;

    /**
     * 排序等属性
     */
    private List<ExcelExportEntity> excelExportEntities;

    public ExcelSingleSheet(String sheetTitle, Class<?> pojoClass, List<?> dataList) {
        this.sheetTitle = sheetTitle;
        this.pojoClass = pojoClass;
        this.dataList = dataList;
        this.params = new ExportParams();
        this.params.setTitle(sheetTitle);
        // sheet名称和title相同
        this.params.setSheetName(sheetTitle);
    }
}
