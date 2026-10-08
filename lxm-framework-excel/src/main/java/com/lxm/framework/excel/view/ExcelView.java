package com.lxm.framework.excel.view;

import com.lxm.framework.common.AppException;
import com.lxm.framework.excel.common.enmus.ExcelType;
import com.lxm.framework.excel.entity.ExcelMultiSheets;
import com.lxm.framework.excel.entity.ExportParams;
import com.lxm.framework.excel.export.entity.ExcelExportEntity;
import com.lxm.framework.excel.util.ExcelUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 导出excel
 *
 * @author twenty2
 */
@Slf4j
public class ExcelView extends AbstractView {

    /**
     * 工作薄对象
     */
    private final Workbook wb;

    /**
     * 表格标题
     */
    private final String filename;

    /**
     * 导出文件扩展名
     */
    private String extension = "xlsx";

    @Override
    protected void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setContentType(
                "xls".equals(extension)
                        ? "application/vnd.ms-excel"
                        : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded =
                URLEncoder.encode(filename + "." + extension, StandardCharsets.UTF_8)
                        .replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        try (Workbook workbook = wb) {
            buildExcelDocument(model, workbook, request, response);
        }
    }

    protected void buildExcelDocument(
            Map<String, Object> model,
            Workbook workbook,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        workbook.write(response.getOutputStream());
    }

    /**
     * @param workbook excel
     * @return ExcelView
     */
    public ExcelView(Workbook workbook) {
        if (Objects.isNull(workbook)) {
            log.warn("workbook cannot be null");
            throw new AppException(55602, "excel export class cannot be null");
        }
        this.filename = workbook.getSheetName(0);
        this.wb = workbook;
        if (workbook instanceof HSSFWorkbook) {
            this.extension = "xls";
        }
    }

    /**
     * @param title    标题
     * @param workbook excel
     * @return ExcelView
     */
    public ExcelView(String title, Workbook workbook) {
        if (StringUtils.isBlank(title)) {
            log.warn("excel export title cannot be null");
            throw new AppException(55601, "excel export title cannot be null");
        }
        if (Objects.isNull(workbook)) {
            log.warn("workbook cannot be null");
            throw new AppException(55602, "excel export class cannot be null");
        }
        this.filename = title;
        this.wb = workbook;
        if (workbook instanceof HSSFWorkbook) {
            this.extension = "xls";
        }
    }

    /**
     * @param title     标题
     * @param pojoClass 导出的注解类
     * @param dataList  导出的列表数据
     * @return ExcelView
     */
    public ExcelView(String title, Class<?> pojoClass, List<?> dataList) {
        if (StringUtils.isBlank(title)) {
            log.warn("excel export title cannot be null");
            throw new AppException(55601, "excel export title cannot be null");
        }
        if (Objects.isNull(pojoClass)) {
            log.warn("excel export title cannot be null");
            throw new AppException(55602, "excel export class cannot be null");
        }
        if (Objects.isNull(dataList)) {
            log.warn("excel export data cannot be null");
            throw new AppException(55603, "excel export data cannot be null");
        }
        this.filename = title;
        ExportParams params = new ExportParams();
        params.setTitle(title);
        this.wb = ExcelUtils.export(params, pojoClass, dataList);
    }

    /**
     * 导出含有多个sheet的excel
     * @param filename  文件名
     * @param sheets  各个sheet的组合对象
     */
    public ExcelView(String filename, ExcelMultiSheets sheets) {
        if (StringUtils.isBlank(filename)) {
            log.warn("excel export title cannot be null");
            throw new AppException(55601, "excel export title cannot be null");
        }
        if (sheets.unqualified()) {
            log.warn("excel export multi sheets has only 1 or 0 sheet");
            throw new AppException(55691, "excel export multi sheets has only 1 or 0 sheet");
        }
        this.filename = filename;
        this.wb = ExcelUtils.exportMultiSheets(sheets);
    }

    /**
     * @param params    导出参数
     * @param pojoClass 导出的注解类
     * @param dataList  导出的列表数据
     * @return ExcelView
     */
    public ExcelView(ExportParams params, Class<?> pojoClass, List<?> dataList) {
        if (Objects.isNull(params)) {
            log.warn("excel export params cannot be null");
            throw new AppException(55604, "excel export params cannot be null");
        }
        if (Objects.isNull(pojoClass)) {
            log.warn("excel export title cannot be null");
            throw new AppException(55602, "excel export class cannot be null");
        }
        if (Objects.isNull(dataList)) {
            log.warn("excel export data cannot be null");
            throw new AppException(55603, "excel export data cannot be null");
        }
        this.filename = params.getTitle();
        if (params.getType() == ExcelType.HSSF) {
            this.extension = "xls";
        }
        this.wb = ExcelUtils.export(params, pojoClass, dataList);
    }

    /**
     * @param title          标题
     * @param exportEntities 导出实体
     * @param dataList       导出的列表数据
     * @return ExcelView
     */
    public ExcelView(String title, List<ExcelExportEntity> exportEntities, List<?> dataList) {
        if (StringUtils.isBlank(title)) {
            log.warn("excel export title cannot be null");
            throw new AppException(55601, "excel export title cannot be null");
        }
        if (Objects.isNull(exportEntities)) {
            log.warn("excel export entities cannot be null");
            throw new AppException(55605, "excel export entities cannot be null");
        }
        if (Objects.isNull(dataList)) {
            log.warn("excel export data cannot be null");
            throw new AppException(55603, "excel export data cannot be null");
        }
        this.filename = title;
        var params = new ExportParams();
        params.setTitle(title);
        this.wb = ExcelUtils.export(params, exportEntities, dataList);
    }

    /**
     * @param params         导出参数
     * @param exportEntities 导出实体
     * @param dataList       导出的列表数据
     * @return ExcelView
     */
    public ExcelView(
            ExportParams params, List<ExcelExportEntity> exportEntities, List<?> dataList) {
        if (Objects.isNull(params)) {
            log.warn("excel export params cannot be null");
            throw new AppException(55601, "excel export params cannot be null");
        }
        if (Objects.isNull(exportEntities)) {
            log.warn("excel export entities cannot be null");
            throw new AppException(55605, "excel export entities cannot be null");
        }
        if (Objects.isNull(dataList)) {
            log.warn("excel export data cannot be null");
            throw new AppException(55603, "excel export data cannot be null");
        }
        this.filename = params.getTitle();
        if (params.getType() == ExcelType.HSSF) {
            this.extension = "xls";
        }
        this.wb = ExcelUtils.export(params, exportEntities, dataList);
    }

    /**
     * 获取工作薄
     *
     * @return Workbook
     */
    public Workbook getWorkbook() {
        return this.wb;
    }
}
