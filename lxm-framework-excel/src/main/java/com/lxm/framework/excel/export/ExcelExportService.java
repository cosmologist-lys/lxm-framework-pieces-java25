package com.lxm.framework.excel.export;

import com.lxm.framework.excel.common.enmus.ExcelType;
import com.lxm.framework.excel.entity.ExportParams;
import com.lxm.framework.excel.exception.ExcelExportException;
import com.lxm.framework.excel.exception.enums.ExcelExportEnum;
import com.lxm.framework.excel.export.entity.ExcelExportEntity;
import com.lxm.framework.excel.export.entity.MergeEntity;
import com.lxm.framework.excel.util.PoiPublicUtil;
import com.lxm.framework.excel.util.PoiReflectorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Excel导出服务
 *
 * @author twenty2
 */
@Slf4j
public class ExcelExportService {

    private final Map<Integer, Double> statistics = new ConcurrentHashMap<>(1);

    /**
     * 创建Workbook
     *
     * @param params   导出参数
     * @param dataSize 数据长度
     * @return Workbook
     */
    public Workbook createWorkbook(ExportParams params, int dataSize) {
        Workbook workbook;
        if (ExcelType.HSSF == params.getType()) {
            workbook = new HSSFWorkbook();
        } else if (dataSize < 100000) {
            workbook = new XSSFWorkbook();
        } else {
            workbook = new SXSSFWorkbook();
        }
        return workbook;
    }

    /**
     * 创建表格
     *
     * @param workbook  工作薄
     * @param sheetName sheet名称
     */
    public synchronized Sheet createSheet(Workbook workbook, String sheetName) {
        Sheet sheet;
        try {
            sheet = workbook.createSheet(sheetName);
        } catch (Exception e) {
            // 重复遍历,出现了重名现象,创建非指定的名称Sheet
            sheet = workbook.createSheet(sheetName + "_" + System.currentTimeMillis());
        }
        return sheet;
    }

    /**
     * 对导出对象排序
     *
     * @param exportEntities 导出对象
     */
    public void sortAllParams(List<ExcelExportEntity> exportEntities) {
        // 自然排序,group 内部排序,集合内部排序
        Map<String, List<ExcelExportEntity>> groupMap = new HashMap<>(4);
        for (int i = exportEntities.size() - 1; i > -1; i--) {
            // 集合内部排序
            if (exportEntities.get(i).getList() != null
                    && exportEntities.get(i).getList().size() > 0) {
                exportEntities
                        .get(i)
                        .getList()
                        .sort(Comparator.comparingInt(ExcelExportEntity::getOrderNum));
            } else {
                if (StringUtils.isNotBlank(exportEntities.get(i).getGroupName())) {
                    if (!groupMap.containsKey(exportEntities.get(i).getGroupName())) {
                        groupMap.put(exportEntities.get(i).getGroupName(), new ArrayList<>());
                    }
                    groupMap.get(exportEntities.get(i).getGroupName()).add(exportEntities.get(i));
                    exportEntities.remove(i);
                }
            }
        }
        exportEntities.sort(Comparator.comparingInt(ExcelExportEntity::getOrderNum));
        if (groupMap.size() > 0) {
            // group 内部排序
            for (Map.Entry<String, List<ExcelExportEntity>> entry : groupMap.entrySet()) {
                entry.getValue().sort(Comparator.comparingInt(ExcelExportEntity::getOrderNum));
                // 插入到exportEntities当中
                boolean isInsert = false;
                for (int i = 0; i < exportEntities.size(); i++) {
                    // 跳过groupName 的元素,防止破会内部结构
                    if (exportEntities.get(i).getOrderNum()
                            > entry.getValue().get(0).getOrderNum()) {
                        exportEntities.addAll(i, entry.getValue());
                        isInsert = true;
                        break;
                    }
                }
                // 如果都比他小就插入到最后
                if (!isInsert) {
                    exportEntities.addAll(entry.getValue());
                }
            }
        }
    }

    /**
     * 插入数据
     *
     * @param sheet          表格
     * @param params         导出参数
     * @param exportEntities 导出实体
     * @param dataSet        数据列表
     */
    public void insertDataToSheet(
            Sheet sheet,
            ExportParams params,
            List<ExcelExportEntity> exportEntities,
            Collection<?> dataSet) {
        try {
            // 创建标题和表头
            int rowIndex = createHeaderAndTitle(sheet, params, exportEntities);
            // 数据开始行数
            int dataStartIndex = rowIndex;
            // 设置单元格宽度和是否隐藏
            setCellWithAndHidden(sheet, exportEntities);
            // 当前序号
            AtomicInteger currentIndex = new AtomicInteger();
            for (Object t : dataSet) {
                rowIndex +=
                        createData(sheet, rowIndex, t, params, exportEntities, currentIndex, 0)[0];
            }
            if (params.getFreezeCol() != 0) {
                sheet.createFreezePane(params.getFreezeCol(), 0, params.getFreezeCol(), 0);
            }
            // 值相同合并单元格
            mergeCells(sheet, exportEntities, dataStartIndex);
        } catch (Exception e) {
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, e.getCause());
        }
    }

    /**
     * 创建标题和表头
     *
     * @param sheet          表格
     * @param params         导出参数
     * @param exportEntities 导出实体
     * @return 行数
     */
    private int createHeaderAndTitle(
            Sheet sheet, ExportParams params, List<ExcelExportEntity> exportEntities) {
        int rowIndex = 0;
        if (params.isCreateTitle()) {
            rowIndex += createTitle(sheet, params, exportEntities);
        }
        if (params.isCreateHeader()) {
            rowIndex += createHeader(sheet, rowIndex, params, exportEntities, 0);
        }
        if (params.isFixedTitle()) {
            sheet.createFreezePane(0, rowIndex, 0, rowIndex);
        }
        return rowIndex;
    }

    /**
     * 创建标题
     *
     * @param sheet          表格
     * @param params         导出参数
     * @param exportEntities 导出实体
     * @return 行数
     */
    private int createTitle(
            Sheet sheet, ExportParams params, List<ExcelExportEntity> exportEntities) {
        Row row = sheet.createRow(0);
        row.setHeight(params.getTitleHeight());
        // 获取标题样式
        CellStyle titleStyle = params.getStyle().getTitleStyle(params.getTitleColor());
        // 创建标题单元格
        createCell(params.getType(), row, 0, params.getTitle(), titleStyle, null);
        int fieldWidth = PoiPublicUtil.getFieldLength(exportEntities);
        for (int i = 1; i <= fieldWidth; i++) {
            createCell(params.getType(), row, i, "", titleStyle, null);
        }
        // 合并标题单元格
        PoiPublicUtil.addMergedRegion(sheet, 0, 0, 0, fieldWidth);
        // 是否第二标题
        if (params.getSecondTitle() != null) {
            row = sheet.createRow(1);
            row.setHeight(params.getSecondTitleHeight());
            CellStyle secondTitleStyle = sheet.getWorkbook().createCellStyle();
            secondTitleStyle.cloneStyleFrom(titleStyle);
            secondTitleStyle.setAlignment(params.getSecondTitleStyle());
            Font font = sheet.getWorkbook().createFont();
            font.setFontName("黑体");
            font.setFontHeightInPoints(params.getSecondTitleFontSize());
            font.setColor(params.getSecondTitleFontColor());
            secondTitleStyle.setFont(font);
            secondTitleStyle.setWrapText(true);
            createCell(params.getType(), row, 0, params.getSecondTitle(), secondTitleStyle, null);
            for (int i = 1; i <= fieldWidth; i++) {
                createCell(params.getType(), row, i, "", titleStyle, null);
            }
            PoiPublicUtil.addMergedRegion(sheet, 1, 1, 0, fieldWidth);
            return 2;
        }
        return 1;
    }

    /**
     * 创建表头
     *
     * @param sheet          表格
     * @param index          行数
     * @param params         导出参数
     * @param exportEntities 导出实体列表
     * @param cellIndex      单元格索引
     * @return 行数
     */
    private int createHeader(
            Sheet sheet,
            int index,
            ExportParams params,
            List<ExcelExportEntity> exportEntities,
            int cellIndex) {
        Row row = sheet.getRow(index) == null ? sheet.createRow(index) : sheet.getRow(index);
        // 判断是否存在多级表头
        int rows = PoiPublicUtil.getRowNums(exportEntities, true);
        row.setHeight(params.getHeaderHeight());
        Row listRow = null;
        if (rows > 1) {
            listRow = sheet.createRow(index + 1);
            listRow.setHeight(params.getHeaderHeight());
        }
        int groupCellLength = 0;
        CellStyle headerStyle = params.getStyle().getHeaderStyle(params.getHeaderColor());
        for (int i = 0, exportFieldTitleSize = exportEntities.size();
                i < exportFieldTitleSize;
                i++) {
            ExcelExportEntity entity = exportEntities.get(i);
            if (StringUtils.isNotBlank(entity.getGroupName())) {
                createCell(
                        params.getType(), row, cellIndex, entity.getGroupName(), headerStyle, null);
                createCell(
                        params.getType(),
                        listRow == null ? listRow = sheet.createRow(index + 1) : listRow,
                        cellIndex,
                        entity.getName(),
                        headerStyle,
                        null);
                if (entity.getGroupName().equals(exportEntities.get(i - 1).getGroupName())) {
                    groupCellLength++;
                    if (groupCellLength > 1) {
                        int mergedRegion =
                                getMergedRegion(sheet, index, cellIndex - groupCellLength + 1);
                        if (mergedRegion != -1) {
                            sheet.removeMergedRegion(mergedRegion);
                        }
                        sheet.addMergedRegion(
                                new CellRangeAddress(
                                        index, index, cellIndex - groupCellLength + 1, cellIndex));
                    }
                } else {
                    groupCellLength = 1;
                }
            } else {
                groupCellLength = 0;
                createCell(params.getType(), row, cellIndex, entity.getName(), headerStyle, null);
            }
            if (entity.getList() != null && entity.getList().size() > 0) {
                createHeader(
                        sheet, rows == 1 ? index : index + 1, params, entity.getList(), cellIndex);
                PoiPublicUtil.addMergedRegion(
                        sheet, index, index, cellIndex, cellIndex + entity.getList().size() - 1);
                cellIndex = cellIndex + entity.getList().size() - 1;
            } else if (rows > 1 && groupCellLength == 0) {
                createCell(params.getType(), listRow, cellIndex, "", headerStyle, null);
                PoiPublicUtil.addMergedRegion(sheet, index, index + rows - 1, cellIndex, cellIndex);
            }
            cellIndex++;
        }
        return rows;
    }

    /**
     * 设置单元格宽度和是否隐藏
     *
     * @param sheet          表格
     * @param exportEntities 导出实体
     */
    private void setCellWithAndHidden(Sheet sheet, List<ExcelExportEntity> exportEntities) {
        int index = 0;
        for (ExcelExportEntity exportEntity : exportEntities) {
            if (exportEntity.getList() != null) {
                for (ExcelExportEntity item : exportEntity.getList()) {
                    if (exportEntity.getWidth() == 0) {
                        PoiPublicUtil.setColumnWidthAdaptive(sheet, index, exportEntity.getName());
                    } else {
                        sheet.setColumnWidth(index, 256 * exportEntity.getWidth());
                    }
                    sheet.setColumnHidden(index, item.isColumnHidden());
                    index++;
                }
            } else {
                if (exportEntity.getWidth() > 0) {
                    sheet.setColumnWidth(index, 256 * exportEntity.getWidth());
                } else {
                    PoiPublicUtil.setColumnWidthAdaptive(sheet, index, exportEntity.getName());
                }
                sheet.setColumnHidden(index, exportEntity.isColumnHidden());
                index++;
            }
        }
    }

    /**
     * 创建数据域
     *
     * @param sheet          表格
     * @param rowIndex       行数
     * @param t              数据对象
     * @param params         导出参数
     * @param exportEntities 导出对象
     * @param currentIndex   当前序号
     * @param cellNum        列索引
     * @return 序号
     */
    private int[] createData(
            Sheet sheet,
            int rowIndex,
            Object t,
            ExportParams params,
            List<ExcelExportEntity> exportEntities,
            AtomicInteger currentIndex,
            int cellNum) {
        try {
            Row row =
                    sheet.getRow(rowIndex) == null
                            ? sheet.createRow(rowIndex)
                            : sheet.getRow(rowIndex);
            row.setHeight(params.getHeight());
            // 需要合并单元格的起始列数
            int margeCellNum = cellNum;
            int maxHeight = 1;
            // 创建序号单元格
            int indexKey = createIndex(row, params, exportEntities.get(0), currentIndex);
            cellNum += indexKey;
            ExcelExportEntity entity;
            for (int k = indexKey, paramSize = exportEntities.size(); k < paramSize; k++) {
                entity = exportEntities.get(k);
                if (entity.getList() != null) {
                    Collection<?> list = getListCellValue(entity, t);
                    if (list != null && list.size() > 0) {
                        int tempCellNum = 0;
                        for (Object obj : list) {
                            int[] temp;
                            if (entity.getList().size() == 0) {
                                ExcelExportEntity entiyItem = new ExcelExportEntity();
                                entiyItem.setName("");
                                entiyItem.setType(entity.getType());
                                temp =
                                        createData(
                                                sheet,
                                                rowIndex + maxHeight - 1,
                                                obj,
                                                params,
                                                List.of(entiyItem),
                                                currentIndex,
                                                cellNum);
                            } else {
                                temp =
                                        createData(
                                                sheet,
                                                rowIndex + maxHeight - 1,
                                                obj,
                                                params,
                                                entity.getList(),
                                                currentIndex,
                                                cellNum);
                            }
                            tempCellNum = temp[1];
                            maxHeight += temp[0];
                        }
                        cellNum = tempCellNum;
                        maxHeight--;
                    }
                } else {
                    CellStyle cellStyle = params.getStyle().getDataStyle(entity);
                    Object val = getCellValue(params, entity, t);
                    createCell(params.getType(), row, cellNum++, val, cellStyle, entity);
                    if (entity.getWidth() == 0) {
                        PoiPublicUtil.setColumnWidthAdaptive(
                                sheet, cellNum - 1, String.valueOf(val));
                    }
                    if (entity.isHyperlink() && params.getDataHandler() != null) {
                        row.getCell(cellNum - 1)
                                .setHyperlink(
                                        params.getDataHandler()
                                                .getHyperlink(
                                                        row.getSheet()
                                                                .getWorkbook()
                                                                .getCreationHelper(),
                                                        t,
                                                        entity.getName(),
                                                        val));
                    }
                    // 添加合计数据
                    addStatisticsData(cellNum - 1, val, entity);
                }
            }
            // 合并list、group单元格
            for (ExcelExportEntity exportEntity : exportEntities) {
                entity = exportEntity;
                if (entity.getList() != null) {
                    margeCellNum += (entity.getList().size() == 0 ? 1 : entity.getList().size());
                } else if (maxHeight > 1) {
                    for (int i = rowIndex + 1; i < rowIndex + maxHeight; i++) {
                        sheet.getRow(i).createCell(margeCellNum);
                        sheet.getRow(i)
                                .getCell(margeCellNum)
                                .setCellStyle(params.getStyle().getDataStyle(entity));
                    }
                    PoiPublicUtil.addMergedRegion(
                            sheet, rowIndex, rowIndex + maxHeight - 1, margeCellNum, margeCellNum);
                    margeCellNum++;
                }
            }
            return new int[] {maxHeight, cellNum};
        } catch (Exception e) {
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, e);
        }
    }

    /**
     * 获取集合的值
     *
     * @param entity 导出实体
     * @param obj    数据对象
     * @return 集合结果
     * @throws Exception 异常
     */
    private Collection<?> getListCellValue(ExcelExportEntity entity, Object obj) throws Exception {
        Object value;
        if (obj instanceof Map) {
            value = ((Map<?, ?>) obj).get(entity.getKey());
        } else {
            value = getFieldBySomeMethod(entity.getGetMethods(), obj);
        }
        return (Collection<?>) value;
    }

    /**
     * 获取cell的值
     *
     * @param params 导出参数
     * @param entity 导出实体
     * @param obj    数据对象
     * @return 单元格值
     * @throws Exception 异常
     */
    private Object getCellValue(ExportParams params, ExcelExportEntity entity, Object obj)
            throws Exception {
        Object value;
        if (obj instanceof Map) {
            value = ((Map<?, ?>) obj).get(entity.getKey());
        } else if (PoiPublicUtil.isJavaClass(obj.getClass())) {
            value = obj;
        } else {
            value = getFieldBySomeMethod(entity.getGetMethods(), obj);
        }
        if (StringUtils.isNotEmpty(entity.getFormat())) {
            value = PoiPublicUtil.dateFormatValue(value, entity.getFormat());
        }
        if (entity.getReplace() != null && entity.getReplace().length > 0) {
            value = PoiPublicUtil.replaceValue(entity.getReplace(), String.valueOf(value), 1);
        }
        if (StringUtils.isNotEmpty(entity.getDict()) && params.getDictHandler() != null) {
            value =
                    params.getDictHandler()
                            .exportHandler(entity.getDict(), entity.getName(), value);
        }
        if (params.getDataHandler() != null) {
            value = params.getDataHandler().exportHandler(entity.getName(), value);
        }
        if (StringUtils.isNotEmpty(entity.getSuffix()) && value != null) {
            value = value + entity.getSuffix();
        }
        if (value != null && StringUtils.isNotEmpty(entity.getEnumExportField())) {
            value =
                    PoiReflectorUtil.fromCache(value.getClass())
                            .getValue(value, entity.getEnumExportField());
        }
        return value;
    }

    /**
     * 多个反射获取值
     *
     * @param list 方法列表
     * @param t    数据对象
     * @return 返回值
     * @throws Exception 异常
     */
    private Object getFieldBySomeMethod(List<Method> list, Object t) throws Exception {
        if (t == null) {
            return "";
        }
        for (Method m : list) {
            t = m.invoke(t);
        }
        return t;
    }

    /**
     * 创建序号单元格
     *
     * @param row          行
     * @param params       导出参数
     * @param exportEntity 导出实体
     * @param currentIndex 当前序号
     * @return 起始列索引
     */
    private int createIndex(
            Row row,
            ExportParams params,
            ExcelExportEntity exportEntity,
            AtomicInteger currentIndex) {
        if (params.isAddIndex()
                && params.getIndexName() != null
                && params.getIndexName().equals(exportEntity.getName())) {
            createCell(
                    params.getType(),
                    row,
                    0,
                    currentIndex.incrementAndGet() + "",
                    params.getStyle().getDataStyle(null),
                    null);
            return 1;
        }
        return 0;
    }

    /**
     * 创建文本类型的Cell
     *
     * @param type      表格类型
     * @param row       行
     * @param index     列索引
     * @param val       值
     * @param cellStyle 单元格样式
     * @param entity    导出实体
     */
    private void createCell(
            ExcelType type,
            Row row,
            int index,
            Object val,
            CellStyle cellStyle,
            ExcelExportEntity entity) {
        Cell cell = row.createCell(index);
        try {
            if (entity == null) {
                entity = new ExcelExportEntity();
                entity.setType(CellType.STRING);
            }
            switch (entity.getType()) {
                case STRING:
                    String text = val != null ? val.toString() : "";
                    RichTextString rtext;
                    if (type == ExcelType.HSSF) {
                        rtext = new HSSFRichTextString(text);
                    } else {
                        rtext = new XSSFRichTextString(text);
                    }
                    cell.setCellValue(rtext);
                    break;
                case NUMERIC:
                    if (val == null) {
                        cell.setCellValue("");
                        cell.setBlank();
                    } else if (val instanceof String) {
                        cell.setCellValue((String) val);
                    } else {
                        if (val instanceof Integer) {
                            cell.setCellValue((Integer) val);
                        } else if (val instanceof Long) {
                            cell.setCellValue((Long) val);
                        } else if (val instanceof Double) {
                            cell.setCellValue((Double) val);
                        } else if (val instanceof Float) {
                            cell.setCellValue((Float) val);
                        } else if (val instanceof BigDecimal) {
                            cell.setCellValue(((BigDecimal) val).doubleValue());
                        }
                    }
                    break;
                case BOOLEAN:
                    if (val == null) {
                        cell.setCellValue("");
                    } else if (val instanceof Boolean) {
                        cell.setCellValue((Boolean) val);
                    } else if (val instanceof String) {
                        cell.setCellValue(Boolean.parseBoolean((String) val));
                    } else {
                        cell.setCellValue("");
                    }
                    break;
                case FORMULA:
                    String formula = val != null ? val.toString() : "";
                    cell.setCellFormula(formula);
                    break;
                default:
                    cell.setBlank();
            }
        } catch (Exception ex) {
            log.warn("Set cell value [{}, {}] error: e = {}", row.getRowNum(), index, ex);
            cell.setCellValue(val != null ? val.toString() : "");
        }
        if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
        }
    }

    /**
     * 合并单元格，纵向合并相同值
     *
     * @param sheet          表格
     * @param exportEntities 导出实体集合
     * @param startRow       开始行
     */
    private void mergeCells(Sheet sheet, List<ExcelExportEntity> exportEntities, int startRow) {
        Map<Integer, int[]> mergeMap = new HashMap<>(4);
        // 设置参数顺序,为之后合并单元格做准备
        int n = 0;
        for (ExcelExportEntity entity : exportEntities) {
            if (entity.isMergeVertical()) {
                mergeMap.put(n, entity.getMergeRely());
            }
            if (entity.getList() != null) {
                for (ExcelExportEntity inner : entity.getList()) {
                    if (inner.isMergeVertical()) {
                        mergeMap.put(n, inner.getMergeRely());
                    }
                    n++;
                }
            } else {
                n++;
            }
        }
        if (mergeMap.size() == 0) {
            return;
        }
        // 结束行
        int endRow = sheet.getLastRowNum();
        Map<Integer, MergeEntity> mergeDataMap = new HashMap<>(4);
        Row row;
        String text;
        for (int i = startRow; i <= endRow; i++) {
            row = sheet.getRow(i);
            for (Integer index : mergeMap.keySet()) {
                if (row == null || row.getCell(index) == null) {
                    if (mergeDataMap.get(index) == null) {
                        continue;
                    }
                    if (mergeDataMap.get(index).getEndRow() == 0) {
                        mergeDataMap.get(index).setEndRow(i - 1);
                    }
                } else {
                    text = PoiPublicUtil.getCellStringValue(row.getCell(index));
                    handlerMergeCells(
                            index,
                            i,
                            text,
                            mergeDataMap,
                            sheet,
                            row.getCell(index),
                            mergeMap.get(index));
                }
            }
        }
        if (mergeDataMap.size() > 0) {
            for (Integer index : mergeDataMap.keySet()) {
                if (mergeDataMap.get(index).getEndRow() > mergeDataMap.get(index).getStartRow()) {
                    PoiPublicUtil.addMergedRegion(
                            sheet,
                            mergeDataMap.get(index).getStartRow(),
                            mergeDataMap.get(index).getEndRow(),
                            index,
                            index);
                }
            }
        }
    }

    /**
     * 处理合并单元格
     *
     * @param colIndex     序号
     * @param rowNum       行号
     * @param text         内容
     * @param mergeDataMap 合并map
     * @param sheet        表格
     * @param cell         单元格
     * @param delys        待合并单元格
     */
    private void handlerMergeCells(
            int colIndex,
            int rowNum,
            String text,
            Map<Integer, MergeEntity> mergeDataMap,
            Sheet sheet,
            Cell cell,
            int[] delys) {
        if (text != null) {
            if (!mergeDataMap.containsKey(colIndex)) {
                mergeDataMap.put(colIndex, createMergeEntity(text, rowNum, cell, delys));
            }
            if (checkIsEqualByCellContents(mergeDataMap.get(colIndex), text, cell, delys, rowNum)) {
                int endRow = rowNum;
                int regionIndex = getMergedRegion(sheet, rowNum, colIndex);
                if (regionIndex != -1) {
                    endRow = sheet.getMergedRegion(regionIndex).getLastRow();
                    sheet.removeMergedRegion(regionIndex);
                }
                mergeDataMap.get(colIndex).setEndRow(endRow);
            } else {
                if (mergeDataMap.get(colIndex).getEndRow() < rowNum
                        && mergeDataMap.get(colIndex).getEndRow()
                                > mergeDataMap.get(colIndex).getStartRow()) {
                    PoiPublicUtil.addMergedRegion(
                            sheet,
                            mergeDataMap.get(colIndex).getStartRow(),
                            mergeDataMap.get(colIndex).getEndRow(),
                            colIndex,
                            colIndex);
                }
                mergeDataMap.remove(colIndex);
                handlerMergeCells(colIndex, rowNum, text, mergeDataMap, sheet, cell, delys);
            }
        }
    }

    /**
     * 获取合并单元格索引
     *
     * @param sheet  表格
     * @param row    行数
     * @param column 列数
     * @return 单元格索引
     */
    private int getMergedRegion(Sheet sheet, int row, int column) {
        for (int i = 0; i < sheet.getMergedRegions().size(); i++) {
            if (row >= sheet.getMergedRegions().get(i).getFirstRow()
                    && row <= sheet.getMergedRegions().get(i).getLastRow()) {
                if (column >= sheet.getMergedRegions().get(i).getFirstColumn()
                        && column <= sheet.getMergedRegions().get(i).getLastColumn()) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 根据内容检查是否合并
     *
     * @param mergeEntity 合并实体
     * @param text        内容
     * @param cell        单元格
     * @param delys       待合并单元格
     * @param rowNum      行号
     * @return true or false
     */
    private boolean checkIsEqualByCellContents(
            MergeEntity mergeEntity, String text, Cell cell, int[] delys, int rowNum) {
        // 没有依赖关系
        if (delys == null || delys.length == 0) {
            return mergeEntity.getText().equals(text);
        }
        // 存在依赖关系
        if (mergeEntity.getText().equals(text)) {
            for (int i = 0; i < delys.length; i++) {
                if (mergeEntity.getRelyList().get(i) == null
                        || !mergeEntity
                                .getRelyList()
                                .get(i)
                                .equals(getCellNotNullText(cell, delys[i], rowNum))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 创建合并单元格实体
     *
     * @param text   值
     * @param rowNum 行号
     * @param cell   单元格
     * @param delys  待合并的行号
     * @return 合并单元格实体
     */
    private MergeEntity createMergeEntity(String text, int rowNum, Cell cell, int[] delys) {
        MergeEntity mergeEntity = new MergeEntity(text, rowNum, rowNum);
        // 存在依赖关系
        if (delys != null && delys.length != 0) {
            List<String> list = new ArrayList<>(delys.length);
            mergeEntity.setRelyList(list);
            for (int dely : delys) {
                list.add(getCellNotNullText(cell, dely, rowNum));
            }
        }
        return mergeEntity;
    }

    /**
     * 获取一个单元格的值,确保这个单元格必须有值,不然向上查询
     *
     * @param cell   单元格
     * @param index  序号
     * @param rowNum 行号
     * @return 单元格值
     */
    private String getCellNotNullText(Cell cell, int index, int rowNum) {
        if (cell == null || cell.getRow() == null) {
            return null;
        }
        Cell c = cell.getRow().getCell(index);
        if (c != null) {
            String res = PoiPublicUtil.getCellStringValue(c);
            if (StringUtils.isNotEmpty(res)) {
                return res;
            }
        }
        return getCellNotNullText(
                cell.getRow().getSheet().getRow(--rowNum).getCell(index), index, rowNum);
    }

    /**
     * 创建统计行
     *
     * @param sheet  表格
     * @param params 导入参数
     */
    public void createStatisticsRow(Sheet sheet, ExportParams params) {
        if (sheet != null && statistics.size() > 0) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            CellStyle cellStyle = params.getStyle().getDataStyle(null);
            createCell(params.getType(), row, 0, "合计", cellStyle, null);
            for (Map.Entry<Integer, Double> entry : statistics.entrySet()) {
                ExcelExportEntity entity = new ExcelExportEntity();
                entity.setType(CellType.NUMERIC);
                createCell(
                        params.getType(), row, entry.getKey(), entry.getValue(), cellStyle, entity);
            }
            statistics.clear();
        }
    }

    /**
     * 添加需统计数据
     *
     * @param colIndex 列索引
     * @param val      值
     * @param entity   导出实体
     */
    private synchronized void addStatisticsData(
            int colIndex, Object val, ExcelExportEntity entity) {
        if (entity != null && entity.isStatistics() && StringUtils.isNumeric(String.valueOf(val))) {
            Double temp = 0D;
            if (!statistics.containsKey(colIndex)) {
                statistics.put(colIndex, temp);
            }
            try {
                temp = Double.valueOf(String.valueOf(val));
            } catch (NumberFormatException e) {
                log.warn(
                        "excel exprot add statistics data value = {}, transfer type error", val, e);
                return;
            }
            statistics.put(colIndex, statistics.get(colIndex) + temp);
        }
    }
}
