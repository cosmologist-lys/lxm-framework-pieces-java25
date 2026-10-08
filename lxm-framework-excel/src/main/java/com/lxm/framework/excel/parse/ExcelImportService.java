package com.lxm.framework.excel.parse;

import com.lxm.framework.excel.annotation.Excel;
import com.lxm.framework.excel.annotation.ExcelCollection;
import com.lxm.framework.excel.annotation.ExcelEntity;
import com.lxm.framework.excel.entity.ImportParams;
import com.lxm.framework.excel.exception.ExcelImportException;
import com.lxm.framework.excel.inf.IExcelDataModel;
import com.lxm.framework.excel.parse.entity.ExcelCollectionImportEntity;
import com.lxm.framework.excel.parse.entity.ExcelImportEntity;
import com.lxm.framework.excel.parse.result.ExcelImportErrorResult;
import com.lxm.framework.excel.parse.result.ExcelImportResult;
import com.lxm.framework.excel.parse.result.ExcelVerifyHandlerResult;
import com.lxm.framework.excel.util.PoiPublicUtil;
import com.lxm.framework.excel.util.PoiReflectorUtil;
import com.lxm.framework.web.util.EntityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Excel 导入服务
 *
 * @author twenty2
 */
@Slf4j
public class ExcelImportService<T> {

    /**
     * 成功的行
     */
    private List<Row> successRow;
    /**
     * 失败的行
     */
    private List<Row> failRow;
    /**
     * 验证错误集合
     */
    private List<ExcelImportErrorResult> failList = new ArrayList<>();
    /**
     * 数据处理器失败数据
     */
    private List<ExcelVerifyHandlerResult> failHandlerList = new ArrayList<>();
    /**
     * 错误单元格的样式
     */
    private CellStyle errorCellStyle;

    /**
     * Excel 导入
     *
     * @param inputstream excel文件流
     * @param pojoClass   导入对象
     * @param params      导入参数
     * @return 导入结果
     * @throws Exception 异常
     */
    public ExcelImportResult<T> importExcelByIs(InputStream inputstream, Class<T> pojoClass, ImportParams params) throws Exception {
        if (inputstream == null || pojoClass == null) {
            throw new ExcelImportException("invalid excel import params");
        }
        if (params == null) {
            params = new ImportParams();
        }
        ExcelImportResult<T> importResult = new ExcelImportResult<>();
        if (params.isMoreInfo()) {
            //将导入数据流缓存下来
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputstream.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                Workbook book;
                try (InputStream userIs = new ByteArrayInputStream(baos.toByteArray())) {
                    book = WorkbookFactory.create(userIs);
                    //创建验证错误单元格的样式
                    errorCellStyle = book.createCellStyle();
                    Font font = book.createFont();
                    font.setColor(Font.COLOR_RED);
                    errorCellStyle.setFont(font);
                    failRow = new ArrayList<>();
                    successRow = new ArrayList<>();
                    importResult.setList(importParse(book, pojoClass, params));
                    importResult.setFailWorkbook(removeSuperfluousRows(book, successRow, params));
                }
                try (InputStream in = new ByteArrayInputStream(baos.toByteArray())) {
                    Workbook successBook = WorkbookFactory.create(in);
                    importResult.setSuccessWorkbook(removeSuperfluousRows(successBook, failRow, params));
                }
            }
        } else {
            try (Workbook workbook=WorkbookFactory.create(inputstream)) {
                importResult.setList(importParse(workbook, pojoClass, params));
            }
        }
        importResult.setFailHandlerList(failHandlerList);
        importResult.setFailList(failList);
        return importResult;
    }

    /**
     * Excel解析
     *
     * @param book      表格
     * @param pojoClass 导入对象
     * @param params    导入参数
     * @return 解析数据结果
     */
    private List<T> importParse(Workbook book, Class<T> pojoClass, ImportParams params) {
        List<T> result = new ArrayList<>();
        for (int i = params.getStartSheetIndex(); i < params.getStartSheetIndex() + params.getSheetNum(); i++) {
            result.addAll(importParse(book.getSheetAt(i), pojoClass, params));
        }
        return result;
    }

    /**
     * Excel解析
     *
     * @param sheet     解析表格
     * @param pojoClass 导入对象
     * @param params    导入参数
     * @return 解析数据结果
     */
    @SuppressWarnings("unchecked")
    private List<T> importParse(Sheet sheet, Class<T> pojoClass, ImportParams params) {
        Iterator<Row> rows = sheet.rowIterator();
        //过滤标题行
        for (int j = 0; j < params.getTitleRows(); j++) {
            rows.next();
        }
        //获取导入模板表头集合
        Map<String, Integer> headerMap = getHeaderMap(sheet, rows.next());
        //创建导入实体集合
        Map<Integer, Object> importEntityMap = parseImportEntity(pojoClass, params, headerMap);
        if (importEntityMap.isEmpty()) {
            throw new ExcelImportException("excel import parameters are not defined");
        }
        //跳过无效行
        for (int i = 0; i < params.getStartRows(); i++) {
            rows.next();
        }
        List<T> collection = new ArrayList<>();
        int readRow = 0;
        StringBuilder errorMsg;
        T object;
        int mergedRegionLast;
        Row row;
        while (rows.hasNext()) {
            //判断是否手动设置了读取行数，若超过就跳出
            if (params.getReadRowLen() > 0 && readRow > params.getReadRowLen()) {
                break;
            }
            row = rows.next();
            // 如果当前行的单元格都是无效的，那就继续下一行
            if (row.getLastCellNum() < 0) {
                continue;
            }
            Cell cell = row.getCell(params.getKeyIndex());
            if (cell == null) {
                continue;
            }
            //判断是否合并单元格
            mergedRegionLast = getMergedRegion(sheet, cell.getRowIndex(), cell.getColumnIndex());
            //主键值为空，则整行数据无效
            if (mergedRegionLast == -1) {
                object = (T) createObject(pojoClass);
                setFieldValue(params, object, importEntityMap, row, false);
            } else {
                object = (T) createObject(pojoClass);
                setFieldValue(params, object, importEntityMap, row, false);
                for (int i = 0; i < mergedRegionLast; i++) {
                    row = rows.next();
                    setFieldValue(params, object, importEntityMap, row, true);
                }
            }
            //验证数据的合法性
            ExcelImportErrorResult importErrorModel = verifyingDataValidity(object, row, params);
            if (importErrorModel == null) {
                collection.add(object);
            }
            readRow++;
        }
        return collection;
    }

    /**
     * 创建一个对象
     *
     * @param clazz 对象
     * @return 创建的对象
     */
    private Object createObject(Class<?> clazz) {
        Object obj;
        try {
            if (clazz.equals(Map.class)) {
                return new LinkedHashMap<String, Object>();
            }
            obj = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = EntityUtils.getAllFields(clazz);
            for (Field field : fields) {
                if (field.isAnnotationPresent(ExcelCollection.class) && PoiPublicUtil.isCollection(field.getType())) {
                    ExcelCollection collection = field.getAnnotation(ExcelCollection.class);
                    PoiReflectorUtil.fromCache(clazz).setValue(obj, field.getName(), collection.type().getDeclaredConstructor().newInstance());
                } else if (field.isAnnotationPresent(Excel.class) || field.isAnnotationPresent(ExcelEntity.class)) {
                    if (!PoiPublicUtil.isJavaClass(field.getType()) && !field.getType().isEnum()) {
                        PoiReflectorUtil.fromCache(clazz).setValue(obj, field.getName(), createObject(field.getType()));
                    }
                }
            }
        } catch (Exception e) {
            log.error("excel parse error, create object faild", e);
            throw new ExcelImportException("excel parse error, create object faild");
        }
        return obj;
    }

    /**
     * 赋值
     *
     * @param params          导入参数
     * @param object          对象
     * @param importEntityMap 导入实体集合
     * @param row             行
     * @param isCollection    是否集合
     */
    @SuppressWarnings("unchecked")
    private void setFieldValue(ImportParams params, Object object, Map<Integer, Object> importEntityMap, Row row, boolean isCollection) {
        for (Map.Entry<Integer, Object> entry : importEntityMap.entrySet()) {
            if (entry.getValue() instanceof ExcelCollectionImportEntity) {
                ExcelCollectionImportEntity collectionImportEntity = (ExcelCollectionImportEntity) entry.getValue();
                Collection collection = (Collection) PoiReflectorUtil.fromCache(object.getClass()).getValue(object, collectionImportEntity.getName());
                Object obj = createObject(collectionImportEntity.getType());
                if (obj instanceof IExcelDataModel) {
                    ((IExcelDataModel) obj).setRowNum(row.getRowNum());
                }
                setFieldValue(params, obj, collectionImportEntity.getImportEntityMap(), row, false);
                collection.add(obj);
            } else {
                if (isCollection) {
                    continue;
                }
                Object o = getCellValue(row.getCell(entry.getKey()));
                ExcelImportEntity importEntity = (ExcelImportEntity) entry.getValue();
                if (params.getDataHandler() != null) {
                    o = params.getDataHandler().importHandler(importEntity.getName(), o);
                }
                if (StringUtils.isNotEmpty(importEntity.getDict()) && params.getDictHandler() != null) {
                    o = params.getDictHandler().importHandler(importEntity.getDict(), importEntity.getName(), o);
                }
                if (StringUtils.isNotEmpty(importEntity.getSuffix()) && o != null && o.toString().endsWith(importEntity.getSuffix())) {
                    o = o.toString().substring(0, o.toString().length() - importEntity.getSuffix().length());
                }
                if (importEntity.getReplace() != null && importEntity.getReplace().length > 0) {
                    o = PoiPublicUtil.replaceValue(importEntity.getReplace(), String.valueOf(o), 2);
                }
                if (object instanceof Map) {
                    ((Map) object).put(importEntity.getName(), o);
                } else {
                    if (o == null) {
                        continue;
                    }
                    try {
                        Object t = object;
                        if (importEntity.getGetMethod() != null) {
                            t = importEntity.getGetMethod().invoke(object);
                        }
                        importEntity.getSetMethod().invoke(t, convertedValue(importEntity, o));
                    } catch (Exception e) {
                        log.error("excel import set field value error", e);
                        throw new ExcelImportException("wrong data type in the " + entry.getKey() + " column of row " + row.getRowNum());
                    }
                }
            }
        }
    }

    /**
     * 获取单元格内的值
     *
     * @param cell 单元格
     * @return Object
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        Object result = null;
        switch (cell.getCellType()) {
            case STRING:
                result = cell.getStringCellValue() != null ? cell.getStringCellValue().trim().replace("\n", "") : null;
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    result = cell.getDateCellValue();
                } else {
                    result = readNumericCell(cell);
                }
                break;
            case BOOLEAN:
                result = Boolean.toString(cell.getBooleanCellValue());
                break;
            case BLANK:
                break;
            case ERROR:
                break;
            case FORMULA:
                try {
                    result = readNumericCell(cell);
                } catch (Exception e1) {
                    try {
                        result = cell.getRichStringCellValue() == null ? "" : cell.getRichStringCellValue().getString();
                    } catch (Exception e2) {
                        throw new RuntimeException("获取公式类型的单元格失败", e2);
                    }
                }
                break;
            default:
                break;
        }
        return result;
    }

    private Object readNumericCell(Cell cell) {
        Object result;
        double value = cell.getNumericCellValue();
        if (((int) value) == value) {
            result = (int) value;
        } else {
            result = value;
        }
        return result;
    }

    /**
     * 获取单元格的值
     *
     * @param cell 单元格
     * @return 单元格值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue() != null ? cell.getStringCellValue().trim().replace("\n", "") : null;
    }

    private Object convertedValue(ExcelImportEntity importEntity, Object value) {
        Class clz = importEntity.getSetMethod().getParameterTypes()[0];
        if (StringUtils.isBlank(value.toString())) {
            if (String.class == clz) {
                return value;
            } else {
                return null;
            }
        }
        Object o = value;
        try {
            if (String.class == clz) {
                return value.toString();
            } else if (Integer.class == clz) {
                try {
                    o = Integer.valueOf(value.toString());
                } catch (Exception e) {
                    o = Double.valueOf(value.toString()).intValue();
                }
            } else if (Double.class == clz) {
                o = Double.valueOf(value.toString());
            } else if (Long.class == clz) {
                try {
                    o = Long.valueOf(value.toString());
                } catch (Exception e) {
                    o = Double.valueOf(value.toString()).longValue();
                }
            } else if (Float.class == clz) {
                o = Float.valueOf(value.toString());
            } else if (BigDecimal.class == clz) {
                o = new BigDecimal(value.toString());
            } else if (Boolean.class == clz) {
                o = Boolean.valueOf(value.toString());
            } else if (Date.class == clz) {
                if (value instanceof String) {
                    String format = importEntity.getFormat();
                    if (StringUtils.isBlank(format)) {
                        format = "yyyy-MM-dd HH:mm:ss";
                    }
                    o = DateUtils.parseDate(value.toString(), format);
                }

            } else if (LocalDateTime.class == clz) {
                if (value instanceof Date) {
                    Date date = (Date) value;
                    Instant instant = date.toInstant();
                    o = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                } else if (value instanceof String) {
                    String format = importEntity.getFormat();
                    if (StringUtils.isBlank(format)) {
                        format = "yyyy-MM-dd HH:mm:ss";
                    }
                    o = LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern(format));
                }
            } else if (LocalDate.class == clz) {
                if (value instanceof Date) {
                    Date date = (Date) value;
                    Instant instant = date.toInstant();
                    o = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
                } else if (value instanceof String) {
                    String format = importEntity.getFormat();
                    if (StringUtils.isBlank(format)) {
                        format = "yyyy-MM-dd";
                    }
                    o = LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern(format));
                }
            } else if (LocalTime.class == clz) {
                if (value instanceof Date) {
                    Date date = (Date) value;
                    Instant instant = date.toInstant();
                    o = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
                } else if (value instanceof String) {
                    String format = importEntity.getFormat();
                    if (StringUtils.isBlank(format)) {
                        format = "HH:mm:ss";
                    }
                    o = LocalTime.parse(value.toString(), DateTimeFormatter.ofPattern(format));
                }
            }
        } catch (Exception e) {
            log.error("excel parser converted value error, importEntity = {}, value = {}", importEntity, value, e);
            o = null;
        }
        return o;
    }

    /**
     * 获取导入实体
     *
     * @param pojoClass 需导入的实体类
     * @param params    导入参数
     * @param headerMap 表头集合
     * @return 导入实体集合
     */
    private Map<Integer, Object> parseImportEntity(Class<?> pojoClass, ImportParams params, Map<String, Integer> headerMap) {
        Map<Integer, Object> importEntityMap = new HashMap<>(4);
        if (pojoClass == Map.class) {
            ExcelImportEntity importEntity;
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                importEntity = new ExcelImportEntity();
                importEntity.setName(entry.getKey());
                importEntityMap.put(entry.getValue(), importEntity);
            }
        } else {
            getAllImportExcelField(headerMap, importEntityMap, pojoClass, null, null);
        }
        return importEntityMap;
    }

    /**
     * 获取表头信息应信息，不支持合并单元格，表头信息不能重复，重复取最后一个
     *
     * @param sheet 单元格
     * @param row   表头行
     * @return key单元格值，val列索引
     */
    private Map<String, Integer> getHeaderMap(Sheet sheet, Row row) {
        if (row == null) {
            throw new ExcelImportException("excel import must have headers");
        }
        //表头集合
        Map<String, Integer> headerMap = new LinkedHashMap<>();
        Iterator<Cell> headerCellIterator = row.cellIterator();
        while (headerCellIterator.hasNext()) {
            Cell cell = headerCellIterator.next();
            if (getMergedRegion(sheet, row.getRowNum(), cell.getColumnIndex()) != -1) {
                throw new ExcelImportException("excel import headers cannot have merged region");
            }
            String value = getCellStringValue(cell);
            if (StringUtils.isNotEmpty(value)) {
                headerMap.put(value, cell.getColumnIndex());
            }
        }
        return headerMap;
    }

    /**
     * 判断指定的单元格是否合并单元格，只支持行合并，不支持
     *
     * @param sheet  单元格
     * @param row    行下标
     * @param column 列下标
     * @return 合并单元格最后行减当前行，-1不是合并单元格
     */
    private int getMergedRegion(Sheet sheet, int row, int column) {
        for (CellRangeAddress range : sheet.getMergedRegions()) {
            if (row >= range.getFirstRow() && row <= range.getLastRow()) {
                if (column == range.getFirstColumn() && column == range.getLastColumn()) {
                    return range.getLastRow() - row;
                } else if (column > range.getFirstColumn() && column < range.getLastColumn()) {
                    throw new ExcelImportException("excel import only row merge is supported, column merge is not supported");
                }
            }
        }
        return -1;
    }

    /**
     * 获取需要导出的全部字段
     *
     * @param headerMap       表头集合
     * @param importEntityMap 导入实体集合，以列名为key、导入实体为val
     * @param pojoClass       导入对象
     * @param getMethod       当excelEntity不为null有效，get方法
     * @param excelEntity     被@ExcelEntity标记的属性
     */
    private void getAllImportExcelField(Map<String, Integer> headerMap, Map<Integer, Object> importEntityMap, Class<?> pojoClass, Method getMethod, ExcelEntity excelEntity) {
        for (Field field : EntityUtils.getAllFields(pojoClass)) {
            if (field.isAnnotationPresent(ExcelCollection.class)) {
                if (!PoiPublicUtil.isCollection(field.getType())) {
                    log.warn("The {} property: {} has 【@ExcelCollection】, but it's not a collection.", pojoClass.getName(), field.getName());
                    continue;
                }
                ExcelCollection excel = field.getAnnotation(ExcelCollection.class);
                if (!excel.importField() || importEntityMap.containsKey(0)) {
                    continue;
                }
                //获取集合元素类型
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                Class<?> clz = (Class<?>) pt.getActualTypeArguments()[0];
                //创建集合导入子集合
                Map<Integer, Object> importItemMap = new HashMap<>(4);
                if (PoiPublicUtil.isJavaClass(clz)) {
                    if (!headerMap.containsKey(excel.name())) {
                        throw new ExcelImportException("Incorrect excel template, there is no " + excel.name() + "cloumn");
                    }
                    ExcelImportEntity importEntity = new ExcelImportEntity();
                    importEntity.setName(excel.name());
                    importItemMap.put(headerMap.get(excel.name()), importEntity);
                } else {
                    getExcelCollectionFieldList(headerMap, importItemMap, clz, EntityUtils.getAllFields(clz), null, excel.name());
                }
                ExcelCollectionImportEntity collectionItemParams = new ExcelCollectionImportEntity();
                collectionItemParams.setName(field.getName());
                collectionItemParams.setType(clz);
                collectionItemParams.setImportEntityMap(importItemMap);
                importEntityMap.put(-1, collectionItemParams);
            } else if (field.isAnnotationPresent(ExcelEntity.class)) {
                if (PoiPublicUtil.isCollection(field.getType()) || PoiPublicUtil.isJavaClass(field.getType())) {
                    throw new ExcelImportException("The" + pojoClass.getName() + " is not pojo，but it has 【@ExcelEntity】.");
                }
                getAllImportExcelField(headerMap, importEntityMap, field.getType(), PoiPublicUtil.getGetMethod(pojoClass, field), field.getAnnotation(ExcelEntity.class));
            } else if (field.isAnnotationPresent(Excel.class)) {
                addEntityToMap(headerMap, importEntityMap, pojoClass, field, getMethod, excelEntity == null ? null : excelEntity.name());
            }
        }
    }

    /**
     * 获取集合里的导入实体
     *
     * @param fields          字段集合
     * @param pojoClass       导入对象
     * @param importEntityMap 导入实体集合
     * @param getMethod       当excelEntity不为null有效，get方法
     * @param groupName       集合分组，例如：学生_名称
     */
    private void getExcelCollectionFieldList(Map<String, Integer> headerMap, Map<Integer, Object> importEntityMap, Class<?> pojoClass, Field[] fields, Method getMethod, String groupName) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelCollection.class)) {
                throw new ExcelImportException("The field : " + field.getName() + " incorrect, there can be no more ExcelCollection in the ExcelCollection");
            } else if (field.isAnnotationPresent(Excel.class)) {
                addEntityToMap(headerMap, importEntityMap, pojoClass, field, getMethod, groupName);
            } else if (field.isAnnotationPresent(ExcelEntity.class)) {
                getExcelCollectionFieldList(headerMap, importEntityMap, field.getType(), EntityUtils.getAllFields(field.getType()), PoiPublicUtil.getGetMethod(pojoClass, field), groupName);
            }
        }
    }

    /**
     * 把这个注解解析放到类型对象中
     *
     * @param field           字段
     * @param pojoClass       导入对象
     * @param importEntityMap 导入实体集合
     * @param getMethod       当excelEntity不为null有效，get方法集合
     * @param groupName       javabean属性时有效，例如：学生_年龄
     */
    private void addEntityToMap(Map<String, Integer> headerMap, Map<Integer, Object> importEntityMap, Class<?> pojoClass, Field field, Method getMethod, String groupName) {
        if (!PoiPublicUtil.isJavaClass(field.getType())) {
            throw new ExcelImportException("The field : " + field.getName() + " incorrect, the @excel annotation must be in Java base when imported");
        }
        Excel excel = field.getAnnotation(Excel.class);
        if (!excel.importField()) {
            return;
        }
        String name = StringUtils.isBlank(groupName) ? excel.name() : groupName + "_" + excel.name();
        if (!headerMap.containsKey(name)) {
            throw new ExcelImportException("Incorrect excel template, there is no " + excel.name() + "cloumn");
        }
        ExcelImportEntity importEntity = new ExcelImportEntity();
        importEntity.setName(name);
        importEntity.setReplace(excel.replace());
        importEntity.setSuffix(excel.suffix());
        importEntity.setSetMethod(PoiReflectorUtil.fromCache(pojoClass).getSetMethod(field.getName()));
        importEntity.setFormat(excel.format());
        importEntity.setDict(excel.dict());
        importEntity.setEnumImportMethod(excel.enumImportMethod());
        importEntity.setGetMethod(getMethod);
        importEntityMap.put(headerMap.get(importEntity.getName()), importEntity);
    }

    /**
     * 校验数据合法性
     *
     * @param object 验证对象
     * @param row    行
     * @param params 导入参数
     * @return 验证错误信息
     */
    private ExcelImportErrorResult verifyingDataValidity(T object, Row row, ImportParams params) {
        ExcelImportErrorResult importErrorModel = null;
        if (params.isNeedVerify() || params.getVerifyHandler() != null) {
            importErrorModel = new ExcelImportErrorResult();
            Cell cell = null;
            boolean verifying = true;
            if (params.isNeedVerify()) {
                String errorMsg = validation(object, params.getVerifyGroup());
                if (StringUtils.isNotEmpty(errorMsg)) {
                    importErrorModel.setRowNum(row.getRowNum());
                    importErrorModel.setErrorMsg(errorMsg);
                    importErrorModel.setObject(object);
                    failList.add(importErrorModel);
                    if (params.isMoreInfo()) {
                        cell = row.createCell(row.getLastCellNum() + 1);
                        cell.setCellValue(errorMsg);
                        cell.setCellStyle(errorCellStyle);
                        failRow.add(row);
                    }
                    verifying = false;
                }
            }
            if (params.getVerifyHandler() != null) {
                ExcelVerifyHandlerResult result = params.getVerifyHandler().verifyHandler(object);
                if (!result.isSuccess()) {
                    result.setRowNum(row.getRowNum());
                    failHandlerList.add(result);
                    if (params.isMoreInfo()) {
                        if (cell == null) {
                            cell = row.createCell(row.getLastCellNum() + 1);
                        }
                        cell.setCellValue(importErrorModel.getErrorMsg());
                        cell.setCellStyle(errorCellStyle);
                        failRow.add(row);
                    }
                    verifying = false;
                }
            }
            if (verifying) {
                if (params.isMoreInfo()) {
                    successRow.add(row);
                }
                return null;
            }
        }
        return importErrorModel;
    }

    /**
     * HIBERNATE 校验
     *
     * @param obj         校验对象
     * @param verfiyGroup 校验组
     * @return 错误信息
     */
    private String validation(Object obj, Class[] verfiyGroup) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> set;
        if (verfiyGroup != null) {
            set = validator.validate(obj, verfiyGroup);
        } else {
            set = validator.validate(obj);
        }
        if (set != null && set.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<Object> constraintViolation : set) {
                builder.append(constraintViolation.getMessage()).append(",");
            }
            return builder.substring(0, builder.length() - 1);
        }
        return null;
    }

    /**
     * 移除表格行
     *
     * @param book    表格
     * @param rowList 被移除行集合
     * @param params  导入参数
     * @return 表格
     */
    private Workbook removeSuperfluousRows(Workbook book, List<Row> rowList, ImportParams params) {
        for (int i = params.getStartSheetIndex() + params.getSheetNum() - 1; i >= 0; i--) {
            Sheet sheet = book.getSheetAt(i);
            Row row;
            for (int j = rowList.size() - 1; j >= 0; j--) {
                row = rowList.get(j);
                if (row.getSheet().getSheetName().equals(sheet.getSheetName())) {
                    PoiPublicUtil.deleteRow(sheet, row.getRowNum());
                }
            }
        }
        return book;
    }

}
