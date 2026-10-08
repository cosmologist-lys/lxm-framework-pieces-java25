package com.lxm.framework.excel.html;

import com.lxm.framework.excel.common.enmus.ExcelType;
import com.lxm.framework.excel.exception.ExcelExportException;
import com.lxm.framework.excel.html.css.CssParseService;
import com.lxm.framework.excel.html.css.ICssConvertToExcel;
import com.lxm.framework.excel.html.css.impl.*;
import com.lxm.framework.excel.html.entity.ExcelCssConstant;
import com.lxm.framework.excel.html.entity.HtmlCssConstant;
import com.lxm.framework.excel.html.entity.style.CellStyleEntity;
import com.lxm.framework.excel.util.PoiPublicUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.management.RuntimeErrorException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 读取Table数据生成Excel
 *
 * @author twenty2
 */
@Slf4j
public class HtmlToExcelService {

    /**
     * 样式
     */
    private static final List<ICssConvertToExcel> STYLE_APPLIERS = new LinkedList<>();

    static {
        STYLE_APPLIERS.add(new AlignCssConvertImpl());
        STYLE_APPLIERS.add(new BackgroundCssConvertImpl());
        STYLE_APPLIERS.add(new BorderCssConverImpl());
        STYLE_APPLIERS.add(new TextCssConvertImpl());
    }

    /**
     * Cell 高宽
     */
    private static final List<ICssConvertToExcel> SHEET_APPLIERS = new LinkedList<>();

    static {
        SHEET_APPLIERS.add(new WidthCssConverImpl());
        SHEET_APPLIERS.add(new HeightCssConverImpl());
    }

    private Sheet sheet;
    private Map<String, Object> cellsOccupied = new HashMap<>();
    private Map<String, CellStyle> cellStyles = new HashMap<>();
    private CellStyle defaultCellStyle;
    private int maxRow = 0;
    private CssParseService cssParse = new CssParseService();

    public Workbook createSheet(String html, ExcelType type) {
        if (StringUtils.isBlank(html)) {
            throw new ExcelExportException("invalid html");
        }
        Workbook workbook;
        if (ExcelType.HSSF.equals(type)) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }
        Elements els = Jsoup.parseBodyFragment(html).select("table");
        Map<String, Sheet> sheets = new HashMap<>(1);
        Map<String, Integer> maxrowMap = new HashMap<>(1);
        for (Element table : els) {
            String sheetName = table.attr(ExcelCssConstant.SHEET_NAME);
            if (StringUtils.isBlank(sheetName)) {
                log.error("table必须存在name属性!");
                throw new RuntimeErrorException(null, "table必须存在name属性");
            }
            if (sheets.containsKey(sheetName)) {
                maxRow = maxrowMap.get(sheetName);
                sheet = sheets.get(sheetName);
            } else {
                maxRow = 0;
                cellStyles.clear();
                cellsOccupied.clear();
                sheet = workbook.createSheet(sheetName);
            }
            // 数据样式
            defaultCellStyle = workbook.createCellStyle();
            defaultCellStyle.setAlignment(HorizontalAlignment.CENTER);
            defaultCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font dataFont = workbook.createFont();
            dataFont.setFontName("Arial");
            dataFont.setFontHeightInPoints((short) 10);
            defaultCellStyle.setFont(dataFont);
            // 生成一个默认样式
            processTable(table);
            maxrowMap.put(sheetName, maxRow);
            sheets.put(sheetName, sheet);
        }
        return workbook;
    }

    private void processTable(Element table) {
        int rowIndex = 0;
        if (maxRow > 0) {
            // blank row
            maxRow += 2;
            rowIndex = maxRow;
        }
        log.debug("Interate Table Rows.");
        String freezeCol;
        int freezeColIndex = -1;
        for (Element row : table.select("tr")) {
            log.debug("Parse Table Row [{}]. Row Index [{}].", row, rowIndex);
            String freezeRow = row.attr(ExcelCssConstant.FREEZE_ROW);
            if ("true".equals(freezeRow)) {
                sheet.createFreezePane(0, rowIndex + 1, 0, rowIndex + 1);
            }
            int colIndex = 0;
            log.debug("Interate Cols.");
            for (Element td : row.select("td, th")) {
                freezeCol = td.attr(ExcelCssConstant.FREEZE_COL);
                if ("true".equals(freezeCol)) {
                    if (colIndex > freezeColIndex) {
                        freezeColIndex = colIndex;
                    }
                }
                while (cellsOccupied.get(rowIndex + "_" + colIndex) != null) {
                    log.debug("Cell [{}][{}] Has Been Occupied, Skip.", rowIndex, colIndex);
                    ++colIndex;
                }
                log.debug("Parse Col [{}], Col Index [{}].", td, colIndex);
                int rowSpan = 0;
                String strRowSpan = td.attr("rowspan");
                if (StringUtils.isNotBlank(strRowSpan) && StringUtils.isNumeric(strRowSpan)) {
                    log.debug("Found Row Span [{}].", strRowSpan);
                    rowSpan = Integer.parseInt(strRowSpan);
                }
                int colSpan = 0;
                String strColSpan = td.attr("colspan");
                if (StringUtils.isNotBlank(strColSpan) && StringUtils.isNumeric(strColSpan)) {
                    log.debug("Found Col Span [{}].", strColSpan);
                    colSpan = Integer.parseInt(strColSpan);
                }
                // col span & row span
                if (colSpan > 1 && rowSpan > 1) {
                    spanRowAndCol(td, rowIndex, colIndex, rowSpan, colSpan);
                    colIndex += colSpan;
                }
                // col span only
                else if (colSpan > 1) {
                    spanCol(td, rowIndex, colIndex, colSpan);
                    colIndex += colSpan;
                }
                // row span only
                else if (rowSpan > 1) {
                    spanRow(td, rowIndex, colIndex, rowSpan);
                    ++colIndex;
                }
                // no span
                else {
                    createCell(td, getOrCreateRow(rowIndex), colIndex).setCellValue(td.text());
                    ++colIndex;
                }
            }
            ++rowIndex;
        }
        if (freezeColIndex != -1) {
            sheet.createFreezePane(freezeColIndex + 1, 0, freezeColIndex + 1, 0);
        }
    }

    private void spanRow(Element td, int rowIndex, int colIndex, int rowSpan) {
        log.debug("Span Row , From Row [{}], Span [{}].", rowIndex, rowSpan);
        mergeRegion(rowIndex, rowIndex + rowSpan - 1, colIndex, colIndex);
        for (int i = 0; i < rowSpan; ++i) {
            Row row = getOrCreateRow(rowIndex + i);
            createCell(td, row, colIndex);
            cellsOccupied.put((rowIndex + i) + "_" + colIndex, true);
        }
        getOrCreateRow(rowIndex).getCell(colIndex).setCellValue(td.text());
    }

    private void spanCol(Element td, int rowIndex, int colIndex, int colSpan) {
        log.debug("Span Col, From Col [{}], Span [{}].", colIndex, colSpan);
        mergeRegion(rowIndex, rowIndex, colIndex, colIndex + colSpan - 1);
        Row row = getOrCreateRow(rowIndex);
        for (int i = 0; i < colSpan; ++i) {
            createCell(td, row, colIndex + i);
        }
        row.getCell(colIndex).setCellValue(td.text());
    }

    private void spanRowAndCol(Element td, int rowIndex, int colIndex, int rowSpan, int colSpan) {
        log.debug("Span Row And Col, From Row [{}], Span [{}].", rowIndex, rowSpan);
        log.debug("From Col [{}], Span [{}].", colIndex, colSpan);
        mergeRegion(rowIndex, rowIndex + rowSpan - 1, colIndex, colIndex + colSpan - 1);
        for (int i = 0; i < rowSpan; ++i) {
            Row row = getOrCreateRow(rowIndex + i);
            for (int j = 0; j < colSpan; ++j) {
                createCell(td, row, colIndex + j);
                cellsOccupied.put((rowIndex + i) + "_" + (colIndex + j), true);
            }
        }
        getOrCreateRow(rowIndex).getCell(colIndex).setCellValue(td.text());
    }

    private Cell createCell(Element td, Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            log.debug("Create Cell [{}][{}].", row.getRowNum(), colIndex);
            cell = row.createCell(colIndex);
        }
        int cellLength = (td.text()).getBytes().length > 24 ? 24 : (td.text()).getBytes().length;
        if (cellLength * 256 + 16 > sheet.getColumnWidth(colIndex)) {
            sheet.setColumnWidth(colIndex, cellLength * 256 + 16);
        }
        return applyStyle(td, cell);
    }

    private Cell applyStyle(Element td, Cell cell) {
        String style = td.attr(HtmlCssConstant.STYLE);
        CellStyle cellStyle;
        if (StringUtils.isNotBlank(style)) {
            CellStyleEntity styleEntity = cssParse.parseStyle(style.trim());
            cellStyle = cellStyles.get(styleEntity.toString());
            if (cellStyle == null) {
                log.debug("No Cell Style Found In Cache, Parse New Style.");
                cellStyle = cell.getRow().getSheet().getWorkbook().createCellStyle();
                cellStyle.cloneStyleFrom(defaultCellStyle);
                for (ICssConvertToExcel cssConvert : STYLE_APPLIERS) {
                    cssConvert.convertToExcel(cell, cellStyle, styleEntity);
                }
                cellStyles.put(styleEntity.toString(), cellStyle);
            }
            for (ICssConvertToExcel cssConvert : SHEET_APPLIERS) {
                cssConvert.convertToExcel(cell, cellStyle, styleEntity);
            }
            if (cellStyles.size() >= 4000) {
                log.debug(
                        "Custom Cell Style Exceeds 4000, Could Not Create New Style, Use Default Style.");
                cellStyle = defaultCellStyle;
            }
        } else {
            log.debug("Style is null ,Use Default Cell Style.");
            cellStyle = defaultCellStyle;
        }

        cell.setCellStyle(cellStyle);
        return cell;
    }

    private Row getOrCreateRow(int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            log.debug("Create New Row [{}].", rowIndex);
            row = sheet.createRow(rowIndex);
            if (rowIndex > maxRow) {
                maxRow = rowIndex;
            }
        }
        return row;
    }

    private void mergeRegion(int firstRow, int lastRow, int firstCol, int lastCol) {
        log.debug("Merge Region, From Row [{}], To [{}].", firstRow, lastRow);
        log.debug("From Col [{}], To [{}].", firstCol, lastCol);
        PoiPublicUtil.addMergedRegion(sheet, firstRow, lastRow, firstCol, lastCol);
    }
}
