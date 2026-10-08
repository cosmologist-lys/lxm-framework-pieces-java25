package com.lxm.framework.excel.export.styler;

import com.lxm.framework.excel.export.entity.ExcelExportEntity;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * Excel导出样式接口
 *
 * @author twenty2
 */
public interface IExcelExportStyler {

    void createStyles(Workbook workbook, List<ExcelExportEntity> exportEntities);

    /**
     * 列表头样式
     */
    CellStyle getHeaderStyle(short headerColor);

    /**
     * 标题样式
     */
    CellStyle getTitleStyle(short color);

    /**
     * 获取数据样式
     *
     * @param entity 导出对象
     */
    CellStyle getDataStyle(ExcelExportEntity entity);

}
