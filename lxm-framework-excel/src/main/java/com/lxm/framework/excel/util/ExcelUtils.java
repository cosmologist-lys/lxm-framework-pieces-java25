package com.lxm.framework.excel.util;

import com.lxm.framework.excel.common.enmus.ExcelType;
import com.lxm.framework.excel.entity.ExcelMultiSheets;
import com.lxm.framework.excel.entity.ExcelSingleSheet;
import com.lxm.framework.excel.entity.ExportParams;
import com.lxm.framework.excel.entity.ImportParams;
import com.lxm.framework.excel.exception.ExcelExportException;
import com.lxm.framework.excel.exception.enums.ExcelExportEnum;
import com.lxm.framework.excel.export.ExcelExportService;
import com.lxm.framework.excel.export.entity.ExcelExportEntity;
import com.lxm.framework.excel.export.styler.DefaultExcelExportStyler;
import com.lxm.framework.excel.export.styler.IExcelExportStyler;
import com.lxm.framework.excel.html.HtmlToExcelService;
import com.lxm.framework.excel.parse.ExcelImportService;
import com.lxm.framework.excel.parse.result.ExcelImportResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author twenty2
 */
public class ExcelUtils {

    /**
     * 导入excel，并读取数据
     *
     * @param file          文件
     * @param sheetNum      哪个sheet
     * @param colNum        每一行有多少列
     * @param escapeHeaders 是否忽略列名
     * @return LinkedList<LinkedList < String>> 内层为1行，外层为所有行
     * @throws IOException
     */
    public static LinkedList<LinkedList<String>> read(
            MultipartFile file, int sheetNum, int colNum, boolean escapeHeaders)
            throws IOException {
        try (var input = file.getInputStream();
                var workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(input)) {
            return read(workbook.getSheetAt(sheetNum), colNum, escapeHeaders);
        }
    }

    private static LinkedList<LinkedList<String>> read(
            Sheet sheet, int colNum, boolean escapeHeaders) throws IOException {
        var arrays = new LinkedList<LinkedList<String>>();
        int rowLength = sheet.getLastRowNum();
        for (int i = 0; i <= rowLength; i++) {
            if (escapeHeaders && i == 0) {
                continue;
            }
            var col = new LinkedList<String>();
            Row oneRow = sheet.getRow(i);
            int columns =
                    colNum == 0
                            ? (oneRow == null ? 0 : Math.max(0, oneRow.getLastCellNum()))
                            : colNum;
            var formatter = new org.apache.poi.ss.usermodel.DataFormatter(java.util.Locale.ROOT);
            for (int i1 = 0; i1 < columns; i1++) {
                col.add(formatter.formatCellValue(oneRow == null ? null : oneRow.getCell(i1)));
            }
            arrays.add(col);
        }
        return arrays;
    }

    /**
     * 导出excel多sheet
     *
     * @param excelMultiSheets 导出参数
     * @return Workbook
     */
    public static Workbook exportMultiSheets(ExcelMultiSheets excelMultiSheets) {
        if (excelMultiSheets == null) {
            throw new ExcelExportException(ExcelExportEnum.PARAMETER_ERROR);
        }
        ExcelExportService exportService = new ExcelExportService();
        Workbook workbook = new XSSFWorkbook();
        try {
            for (ExcelSingleSheet excelSingleSheet : excelMultiSheets.getSheetList()) {
                ExportParams params = excelSingleSheet.getParams();
                List<ExcelExportEntity> entities =
                        PoiPublicUtil.createExcelExportEntities(
                                params, excelSingleSheet.getPojoClass());
                // 对导出对象排序
                exportService.sortAllParams(entities);
                excelSingleSheet.setExcelExportEntities(entities);
                // 创建默认样式
                IExcelExportStyler exportStyler = new DefaultExcelExportStyler();
                exportStyler.createStyles(workbook, entities);
                params.setStyle(exportStyler);
                Sheet sheet = exportService.createSheet(workbook, params.getSheetName());
                // 插入数据到表格
                exportService.insertDataToSheet(
                        sheet, params, entities, excelSingleSheet.getDataList());
            }
        } catch (RuntimeException e) {
            try {
                workbook.close();
            } catch (IOException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, e);
        }
        return workbook;
    }

    /**
     * 导出excel
     *
     * @param params    导出参数
     * @param pojoClass 导出类
     * @param dataList  数据集合
     * @return Workbook
     */
    public static Workbook export(ExportParams params, Class<?> pojoClass, List<?> dataList) {
        if (params == null || pojoClass == null) {
            throw new ExcelExportException(ExcelExportEnum.PARAMETER_ERROR);
        }
        return export(params, PoiPublicUtil.createExcelExportEntities(params, pojoClass), dataList);
    }

    /**
     * 导出excel
     *
     * @param params         导出参数
     * @param exportEntities 导出实体集合
     * @param dataList       数据集合
     * @return Workbook
     */
    public static Workbook export(
            ExportParams params, List<ExcelExportEntity> exportEntities, List<?> dataList) {
        if (params == null || exportEntities == null) {
            throw new ExcelExportException(ExcelExportEnum.PARAMETER_ERROR);
        }
        if (dataList == null) {
            throw new ExcelExportException(ExcelExportEnum.DATA_ERROR);
        }
        ExcelExportService exportService = new ExcelExportService();
        Workbook workbook = exportService.createWorkbook(params, dataList.size());
        // 对导出对象排序
        exportService.sortAllParams(exportEntities);
        // 创建默认样式
        if (params.getStyle() == null) {
            IExcelExportStyler exportStyler = new DefaultExcelExportStyler();
            exportStyler.createStyles(workbook, exportEntities);
            params.setStyle(exportStyler);
        }
        String sheetName = params.getSheetName();
        Sheet sheet = null;
        if (ExcelType.HSSF == params.getType() && dataList.size() > 60000) {
            List<? extends List<?>> lists = PoiPublicUtil.subListByCount(dataList, 60000);
            for (int i = 0; i < lists.size(); i++) {
                sheet = exportService.createSheet(workbook, sheetName + "_" + (i + 1));
                // 插入数据到表格
                exportService.insertDataToSheet(sheet, params, exportEntities, lists.get(i));
            }
        } else {
            sheet = exportService.createSheet(workbook, sheetName);
            // 插入数据到表格
            exportService.insertDataToSheet(sheet, params, exportEntities, dataList);
        }
        // 最后一个sheet创建合计信息
        exportService.createStatisticsRow(sheet, params);
        return workbook;
    }

    /**
     * 解析excel
     *
     * @param in        excel文件流
     * @param pojoClass 导入类
     * @param params    导入参数
     * @return ExcelImportResult
     */
    public static <T> ExcelImportResult<T> parse(
            InputStream in, Class<T> pojoClass, ImportParams params) throws Exception {
        return new ExcelImportService<T>().importExcelByIs(in, pojoClass, params);
    }

    /**
     * Html转Excel
     *
     * @param html html字符串
     * @return Workbook
     */
    public static Workbook htmlToExcel(String html) {
        return htmlToExcel(html, ExcelType.XSSF);
    }

    /**
     * Html转Excel
     *
     * @param html html字符串
     * @param type excel类型
     * @return Workbook
     */
    public static Workbook htmlToExcel(String html, ExcelType type) {
        return new HtmlToExcelService().createSheet(html, type);
    }

    /**
     * Html 读取Excel
     *
     * @param is html文件流
     * @return Workbook
     */
    public static Workbook htmlToExcel(InputStream is) {
        return htmlToExcel(is, ExcelType.XSSF);
    }

    /**
     * Html 读取Excel
     *
     * @param is   html文件流
     * @param type excel类型
     * @return Workbook
     */
    public static Workbook htmlToExcel(InputStream is, ExcelType type) {
        try {
            return htmlToExcel(
                    new String(IOUtils.toByteArray(is), java.nio.charset.StandardCharsets.UTF_8),
                    type);
        } catch (IOException e) {
            throw new ExcelExportException(ExcelExportEnum.HTML_ERROR, e);
        }
    }
}
