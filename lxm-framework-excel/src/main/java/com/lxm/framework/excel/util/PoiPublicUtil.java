package com.lxm.framework.excel.util;

import com.lxm.framework.common.utils.ListUtils;
import com.lxm.framework.excel.annotation.Excel;
import com.lxm.framework.excel.annotation.ExcelCollection;
import com.lxm.framework.excel.annotation.ExcelEntity;
import com.lxm.framework.excel.entity.ExportParams;
import com.lxm.framework.excel.exception.ExcelExportException;
import com.lxm.framework.excel.exception.enums.ExcelExportEnum;
import com.lxm.framework.excel.export.entity.ExcelExportEntity;
import com.lxm.framework.web.util.EntityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * EASYPOI 的公共基础类
 *
 * @author twenty2
 */
@Slf4j
public final class PoiPublicUtil {

    /**
     * 判断是不是集合的实现类
     *
     * @param clazz 判断类
     * @return boolean
     */
    public static boolean isCollection(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    /**
     * 是不是java基础类
     *
     * @param clz 判断类
     * @return boolean
     */
    public static boolean isJavaClass(Class<?> clz) {
        return clz.isPrimitive() || clz.getPackage() == null || "java.lang".equals(clz.getPackage().getName())
                || "java.math".equals(clz.getPackage().getName()) || "java.sql".equals(clz.getPackage().getName())
                || "java.util".equals(clz.getPackage().getName()) || "java.time".equals(clz.getPackage().getName());
    }

    /**
     * double to String 防止科学计数法
     *
     * @param value double值
     * @return doubleStr
     */
    public static String doubleToString(Double value) {
        String temp = value.toString();
        if (temp.contains("E")) {
            BigDecimal bigDecimal = new BigDecimal(temp);
            temp = bigDecimal.toPlainString();
        }
        return temp;
    }

    /**
     * 获取get方法
     *
     * @param pojoClass 对象类
     * @param field     字段
     * @return 方法
     */
    public static Method getGetMethod(Class<?> pojoClass, Field field) {
        return PoiReflectorUtil.fromCache(pojoClass).getGetMethod(field.getName());
    }

    /**
     * 获取get方法集合
     *
     * @param pojoClass 对象类
     * @param field     字段
     * @return 方法集合
     */
    public static List<Method> getGetMethods(List<Method> getMethods, Class<?> pojoClass, Field field) {
        if (getMethods == null) {
            getMethods = new ArrayList<>();
        }
        getMethods.add(PoiReflectorUtil.fromCache(pojoClass).getGetMethod(field.getName()));
        return getMethods;
    }

    /**
     * 返回单元格数据类型
     *
     * @param clz 数据类
     * @return 单元格数据类型
     */
    public static CellType getCellTypeByClz(Class<?> clz) {
        if (clz == Integer.class || clz == Long.class || clz == Double.class || clz == Float.class || clz == BigDecimal.class ||
                clz == int.class || clz == long.class || clz == double.class || clz == float.class) {
            return CellType.NUMERIC;
        } else if (clz == Boolean.class || clz == boolean.class) {
            return CellType.BOOLEAN;
        }
        return CellType.STRING;
    }

    /**
     * 获取导出报表的字段总长度
     *
     * @param excelExportEntities 导出实体
     * @return 报表的字段总长度
     */
    public static int getFieldLength(List<ExcelExportEntity> excelExportEntities) {
        // 从0开始计算单元格的
        int length = -1;
        for (ExcelExportEntity entity : excelExportEntities) {
            if (entity.getList() != null && entity.getList().size() > 0) {
                length += getFieldLength(entity.getList()) + 1;
            } else {
                length++;
            }
        }
        return length;
    }

    /**
     * 判断表头是只有一行还是多行
     *
     * @param exportEntities 导出实体
     * @param isDeep         是否遍历下级
     * @return 返回行数
     */
    public static int getRowNums(List<ExcelExportEntity> exportEntities, boolean isDeep) {
        for (ExcelExportEntity exportEntity : exportEntities) {
            if (exportEntity.getList() != null && exportEntity.getList().size() > 0) {
                return isDeep ? 1 + getRowNums(exportEntity.getList(), true) : 2;
            }
            if (StringUtils.isNotEmpty(exportEntity.getGroupName())) {
                return 2;
            }
        }
        return 1;
    }

    /**
     * 导出格式化日期
     *
     * @param value     值
     * @param formatStr 格式
     * @return 格式化后的日期
     */
    public static String dateFormatValue(Object value, String formatStr) {
        if (value == null) {
            return null;
        }
        String dateStr = null;
        if (value instanceof Date || value instanceof java.sql.Time || value instanceof java.sql.Timestamp) {
            SimpleDateFormat format = new SimpleDateFormat(formatStr);
            dateStr = format.format(value);
        } else if (value instanceof TemporalAccessor) {
            dateStr = DateTimeFormatter.ofPattern(formatStr).format((TemporalAccessor) value);
        }
        return dateStr;
    }

    /**
     * 替换值
     *
     * @param replace 替换数组
     * @param value   值
     * @param action  替换操作，1：导出 2：导入
     * @return 替换后的值
     */
    public static String replaceValue(String[] replace, String value, int action) {
        if (replace == null) {
            return value;
        }
        String[] temp;
        for (String str : replace) {
            temp = str.split("_");
            if (action == 1 && temp[1].equals(value)) {
                value = temp[0];
                break;
            } else if (action == 2 && temp[0].equals(value)) {
                value = temp[1];
                break;
            }
        }
        return value;
    }

    /**
     * 添加合并单元格
     *
     * @param sheet    表格
     * @param firstRow 第一行
     * @param lastRow  最后一行
     * @param firstCol 第一列
     * @param lastCol  最后一列
     */
    public static void addMergedRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        if (firstRow == lastRow && firstCol == lastCol) return;
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    /**
     * 根据单元格值自适应设置列宽
     *
     * @param sheet 单元格
     * @param index 列索引
     * @param val   值
     */
    public static void setColumnWidthAdaptive(Sheet sheet, int index, String val) {
        int cellLength = Math.min(val.getBytes().length, 24);
        if (cellLength * 256 + 16 > sheet.getColumnWidth(index)) {
            sheet.setColumnWidth(index, cellLength * 256 + 16);
        }
    }

    /**
     * excel删除行
     *
     * @param sheet  sheet
     * @param rowNum 起始行
     */
    public static void deleteRow(Sheet sheet, int rowNum) {
        int lastRowNum = sheet.getLastRowNum();
        if (rowNum < lastRowNum) {
            sheet.shiftRows(rowNum + 1, lastRowNum, -1, true, false);
        } else if (rowNum == lastRowNum) {
            Row removingRow = sheet.getRow(rowNum);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }

    /**
     * 分隔数组 根据每段数量分段
     *
     * @param data  被分隔的数组
     * @param count 每段数量
     * @return List
     */
    public static <T> List<List<T>> subListByCount(List<T> data, int count) {
        return ListUtils.subListByCount(data,count);
    }

    /**
     * 转换导出实体
     *
     * @param params    导出参数
     * @param pojoClass 导出类
     * @return List<ExcelExportEntity>
     */
    public static List<ExcelExportEntity> createExcelExportEntities(ExportParams params, Class<?> pojoClass) {
        if (params == null || pojoClass == null) {
            throw new ExcelExportException(ExcelExportEnum.PARAMETER_ERROR);
        }
        try {
            List<ExcelExportEntity> exportEntities = new ArrayList<>();
            if (params.isAddIndex()) {
                //添加Index列,保证是第一排
                ExcelExportEntity indexExcelEntity = new ExcelExportEntity();
                indexExcelEntity.setOrderNum(Integer.MIN_VALUE);
                indexExcelEntity.setName(params.getIndexName());
                indexExcelEntity.setType(CellType.NUMERIC);
                indexExcelEntity.setWidth(10);
                exportEntities.add(indexExcelEntity);
            }
            getAllExportExcelField(params.getInclusions(), params.getExclusions(), exportEntities, pojoClass, null, null);
            return exportEntities;
        } catch (Exception e) {
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, e);
        }
    }

    /**
     * 获取需要导出的全部字段
     */
    public static void getAllExportExcelField(String[] inclusions, String[] exclusions, List<ExcelExportEntity> exportEntities, Class<?> pojoClass, List<Method> getMethods, ExcelEntity excelEntity) {
        // 得到所有字段
        Field[] fields = EntityUtils.getAllFields(pojoClass);
        // 遍历整个filed
        for (Field field : fields) {
            // 先判断是不是collection,在判断是不是java自带对象,之后就是我们自己的对象了
            if (isNotExcelExportField(inclusions, exclusions, field)) {
                continue;
            }
            // 首先判断Excel 可能一下特殊数据用户回自定义处理
            if (field.getAnnotation(Excel.class) != null) {
                exportEntities.add(createExcelExportEntity(field, pojoClass, getMethods, excelEntity));
            } else if (field.isAnnotationPresent(ExcelCollection.class) && PoiPublicUtil.isCollection(field.getType())) {
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                Class<?> clz = (Class<?>) pt.getActualTypeArguments()[0];
                ExcelExportEntity excelExportEntity = new ExcelExportEntity();
                List<ExcelExportEntity> exportEntityitems = new ArrayList<>();
                if (!PoiPublicUtil.isJavaClass(clz)) {
                    getAllExportExcelField(inclusions, exclusions, exportEntityitems, clz, null, null);
                } else {
                    excelExportEntity.setType(PoiPublicUtil.getCellTypeByClz(clz));
                }
                ExcelCollection excel = field.getAnnotation(ExcelCollection.class);
                excelExportEntity.setName(StringUtils.isEmpty(excel.name()) ? "column" + excel.orderNum() : excel.name());
                excelExportEntity.setOrderNum(excel.orderNum());
                excelExportEntity.setGetMethods(PoiPublicUtil.getGetMethods(getMethods, pojoClass, field));
                excelExportEntity.setList(exportEntityitems);
                exportEntities.add(excelExportEntity);
            } else if (field.isAnnotationPresent(ExcelEntity.class)) {
                ExcelEntity excel = field.getAnnotation(ExcelEntity.class);
                if (StringUtils.isNotBlank(excel.name())) {
                    getAllExportExcelField(inclusions, exclusions, exportEntities, field.getType(), PoiPublicUtil.getGetMethods(getMethods, pojoClass, field), excel);
                }
            }
        }
    }

    /**
     * 判断是否为excel导出字段
     *
     * @param inclusions 包含字段属性
     * @param exclusions 排除字段属性
     * @param field      字段
     * @return boolean
     */
    public static boolean isNotExcelExportField(String[] inclusions, String[] exclusions, Field field) {
        boolean boo = true;
        if (field.getAnnotation(ExcelCollection.class) != null && (inclusions == null || ArrayUtils.contains(inclusions, field.getName())
                && (exclusions == null || !ArrayUtils.contains(exclusions, field.getName())))) {
            boo = false;
        } else if (field.getAnnotation(Excel.class) != null && (inclusions == null || ArrayUtils.contains(inclusions, field.getName()))
                && (exclusions == null || !ArrayUtils.contains(exclusions, field.getName()))) {
            boo = false;
        } else if (field.getAnnotation(ExcelEntity.class) != null && (inclusions == null || ArrayUtils.contains(inclusions, field.getName()))
                && (exclusions == null || !ArrayUtils.contains(exclusions, field.getName()))) {
            boo = false;
        }
        return boo;
    }

    /**
     * 创建导出实体对象
     */
    public static ExcelExportEntity createExcelExportEntity(Field field, Class<?> pojoClass, List<Method> getMethods, ExcelEntity excelEntity) {
        ExcelExportEntity excelExportEntity = new ExcelExportEntity();
        Excel excel = field.getAnnotation(Excel.class);
        if (excel.type() == CellType._NONE) {
            excelExportEntity.setType(PoiPublicUtil.getCellTypeByClz(field.getType()));
        } else {
            excelExportEntity.setType(excel.type());
        }
        excelExportEntity.setStyleType(excel.styleType());
        excelExportEntity.setName(StringUtils.isEmpty(excel.name()) ? "column" + excel.orderNum() : excel.name());
        excelExportEntity.setWidth(excel.width());
        excelExportEntity.setMergeVertical(excel.mergeVertical());
        excelExportEntity.setMergeRely(excel.mergeRely());
        excelExportEntity.setReplace(excel.replace());
        excelExportEntity.setOrderNum(excel.orderNum());
        excelExportEntity.setWrap(excel.isWrap());
        excelExportEntity.setSuffix(excel.suffix());
        excelExportEntity.setFormat(excel.format());
        excelExportEntity.setStatistics(excel.statistics());
        excelExportEntity.setHyperlink(excel.hyperlink());
        Method getMethod = PoiPublicUtil.getGetMethod(pojoClass, field);
        List<Method> methods;
        if (getMethods != null) {
            methods = new ArrayList<>(getMethods);
        } else {
            methods = new ArrayList<>();
        }
        methods.add(getMethod);
        excelExportEntity.setGetMethods(methods);
        excelExportEntity.setNumFormat(excel.numFormat());
        excelExportEntity.setColumnHidden(excel.columnHidden());
        excelExportEntity.setDict(excel.dict());
        excelExportEntity.setEnumExportField(excel.enumExportField());
        if (excelEntity != null) {
            excelExportEntity.setGroupName(excelEntity.name());
        } else {
            excelExportEntity.setGroupName(excel.groupName());
        }
        return excelExportEntity;
    }

    public static String getCellStringValue(Cell cell) {
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case STRING:
                    return cell.getStringCellValue();
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? "true" : "false";
                case ERROR:
                    return "error";
                default:
                    return "";
            }
        }
        return null;
    }
}
