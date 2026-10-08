package com.lxm.framework.excel.export.styler;

import com.lxm.framework.excel.common.enmus.ExcelStyleType;
import com.lxm.framework.excel.export.entity.ExcelExportEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象接口提供两个公共方法
 *
 * @author twenty2
 */
public class DefaultExcelExportStyler implements IExcelExportStyler {

    /**
     * 样式列表
     */
    protected Map<String, CellStyle> styles;

    @Override
    public void createStyles(Workbook workbook, List<ExcelExportEntity> exportEntities) {
        this.styles = new HashMap<>(4);
        // 标题样式
        CellStyle style = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short) 22);
        titleFont.setBold(true);
        titleFont.setFontName("黑体");
        style.setFont(titleFont);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("titleStyle", style);

        // 表头样式
        style = workbook.createCellStyle();
        style.cloneStyleFrom(styles.get("titleStyle"));
        Font headFont = workbook.createFont();
        headFont.setFontName("黑体");
        headFont.setFontHeightInPoints((short) 10);
        headFont.setBold(true);
        style.setFont(headFont);
        styles.put("headStyle", style);

        // 数据样式
        createDataStyle(workbook, exportEntities);

        // 其他数据样式
        style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font dataFont = workbook.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        style.setFont(dataFont);
        styles.put("other", style);
    }

    @Override
    public CellStyle getTitleStyle(short color) {
        CellStyle cellStyle = styles.get("titleStyle");
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    @Override
    public CellStyle getHeaderStyle(short color) {
        CellStyle cellStyle = styles.get("headStyle");
        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    @Override
    public CellStyle getDataStyle(ExcelExportEntity entity) {
        if (entity != null) {
            return styles.get("dataStyle_" + entity.getName() + "_" + entity.getOrderNum());
        }
        return styles.get("other");
    }

    private void createDataStyle(Workbook workbook, List<ExcelExportEntity> exportEntities) {
        CellStyle style;
        for (ExcelExportEntity entity : exportEntities) {
            style = workbook.createCellStyle();
            if (entity.getType() == CellType.NUMERIC
                    && StringUtils.isNotEmpty(entity.getNumFormat())) {
                DataFormat format = workbook.createDataFormat();
                style.setDataFormat(format.getFormat(entity.getNumFormat()));
            } else if (entity.getStyleType() == ExcelStyleType.MONEY) {
                DataFormat format = workbook.createDataFormat();
                style.setDataFormat(format.getFormat("#,##0.00"));
            } else {
                style.setDataFormat((short) 0);
            }
            if (!entity.isWrap()) {
                style.setWrapText(false);
            } else {
                style.setWrapText(true);
            }
            if (entity.getStyleType() == ExcelStyleType.DATA_RIGHT) {
                style.setAlignment(HorizontalAlignment.RIGHT);
            } else if (entity.getStyleType() == ExcelStyleType.DATA_LEFT) {
                style.setAlignment(HorizontalAlignment.LEFT);
            } else if (entity.getStyleType() == ExcelStyleType.MONEY) {
                style.setAlignment(HorizontalAlignment.RIGHT);
            } else {
                style.setAlignment(HorizontalAlignment.CENTER);
            }
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            Font dataFont = workbook.createFont();
            dataFont.setFontName("Arial");
            dataFont.setFontHeightInPoints((short) 10);
            style.setFont(dataFont);
            styles.put("dataStyle_" + entity.getName() + "_" + entity.getOrderNum(), style);
            if (entity.getList() != null && entity.getList().size() > 0) {
                createDataStyle(workbook, entity.getList());
            }
        }
    }
}
