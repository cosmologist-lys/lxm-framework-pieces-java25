package com.lxm.framework.web.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import com.lxm.framework.common.utils.DateTimeUtils;
import com.lxm.framework.common.utils.StringFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.cglib.beans.BeanMap;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Twenty2
 */
@Slf4j
public class EntityUtils {

    private static final XmlMapper xmlMapper;
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = com.lxm.framework.web.jackon.FrameworkJackson.mapper();
        xmlMapper =
                XmlMapper.builder()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .addModule(com.lxm.framework.web.jackon.FrameworkJackson.dates())
                        .build();
    }

    /**
     * 复制属性值全员复制
     *
     * @param src  源
     * @param dist 目标
     */
    public static void copyProperties(Object src, Object dist) {
        copyProperties(src, dist, true);
    }

    /**
     * 复制属性值排除null值
     *
     * @param src  源
     * @param dist 目标
     */
    public static void copyPropertiesExceptNull(Object src, Object dist) {
        copyProperties(src, dist, false);
    }

    /**
     * 复制属性值
     *
     * @param src       源
     * @param dist      目标
     * @param allowNull 是否允许为null
     */
    public static void copyProperties(Object src, Object dist, boolean allowNull) {
        copyProperties(src, dist, allowNull, null, null);
    }

    /**
     * 复制属性值，根据指定属性复制
     *
     * @param src        源
     * @param dist       目标
     * @param properties 指定复制的数组属性
     */
    @Deprecated
    public static void copyProperties(Object src, Object dist, String[] properties) {
        copyProperties(src, dist, true, properties, null);
    }

    /**
     * 复制属性值
     *
     * @param src       源
     * @param dist      目标
     * @param allowNull 是否允许为null
     * @param includes  指定复制的数组属性
     */
    @Deprecated
    public static void copyProperties(
            Object src, Object dist, boolean allowNull, String[] includes) {
        copyProperties(src, dist, allowNull, includes, null);
    }

    /**
     * 复制属性值，根据指定属性复制
     *
     * @param src       源
     * @param dist      目标
     * @param includes  指定复制的数组属性
     * @param allowNull 是否允许为null
     */
    public static void copyIncludes(Object src, Object dist, String[] includes, boolean allowNull) {
        copyProperties(src, dist, allowNull, includes, null);
    }

    /**
     * 复制属性值，指定排除的数组属性
     *
     * @param src       源
     * @param dist      目标
     * @param excludes  指定排除的数组属性
     * @param allowNull 是否允许为null
     */
    public static void copyExcludes(Object src, Object dist, String[] excludes, boolean allowNull) {
        copyProperties(src, dist, allowNull, null, excludes);
    }

    /**
     * 复制属性值
     *
     * @param src       源
     * @param dist      目标
     * @param allowNull 是否允许为null
     * @param includes  指定复制的数组属性
     * @param excludes  指定排除的数组属性
     */
    public static void copyProperties(
            Object src, Object dist, boolean allowNull, String[] includes, String[] excludes) {
        if (Objects.isNull(src) || Objects.isNull(dist)) {
            return;
        }
        Class<?> distClz = dist.getClass();
        Stream.of(getAllFields(distClz))
                .forEach(
                        distField -> {
                            try {
                                if ("serialVersionUID".equals(distField.getName())) {
                                    return;
                                }
                                Class<?> srcClz = src.getClass();
                                Optional<Field> optional =
                                        Stream.of(getAllFields(srcClz))
                                                .filter(
                                                        f ->
                                                                f.getName()
                                                                        .equals(
                                                                                distField
                                                                                        .getName()))
                                                .findFirst();
                                if (optional.isPresent()) {
                                    optional.get().setAccessible(true);
                                    Object v = optional.get().get(src);
                                    if (!allowNull && Objects.isNull(v)) {
                                        return;
                                    }
                                    if (ArrayUtils.getLength(excludes) > 0
                                            && ArrayUtils.contains(excludes, distField.getName())) {
                                        return;
                                    }
                                    if (ArrayUtils.getLength(includes) > 0
                                            && !ArrayUtils.contains(
                                                    includes, distField.getName())) {
                                        return;
                                    }
                                    distField.setAccessible(true);
                                    if ("java.base"
                                            .equals(distField.getType().getModule().getName())) {
                                        distField.set(dist, optional.get().get(src));
                                    } else {
                                        copyProperties(
                                                optional.get().get(src),
                                                distField.get(dist),
                                                allowNull,
                                                null,
                                                null);
                                    }
                                }
                            } catch (IllegalAccessException e) {
                                log.error(
                                        "copy properties field error, srcClz : {}, distClz : {}, field : {}",
                                        src,
                                        dist,
                                        distField,
                                        e);
                            }
                        });
    }

    /**
     * 将对象装换为map
     *
     * @param t 实体对象
     * @return map
     */
    public static <T> Map<String, Object> convert(T t) {
        Map<String, Object> map = new HashMap<>(16);
        if (t != null) {
            BeanMap beanMap = BeanMap.create(t);
            for (Object key : beanMap.keySet()) {
                map.put(key.toString(), beanMap.get(key));
            }
        }
        return map;
    }

    /**
     * 将对象装换为map
     *
     * @param t 实体对象
     * @return map
     */
    public static <T> Map<String, Object> convertExcludeNullValue(T t) {
        Map<String, Object> map = new HashMap<>(16);
        if (t != null) {
            BeanMap beanMap = BeanMap.create(t);
            for (Object key : beanMap.keySet()) {
                Object value = beanMap.get(key);
                if (value != null) {
                    map.put(StringFormatUtils.underline(key.toString()), value);
                }
            }
        }
        return map;
    }

    /**
     * 将map装换为javabean对象
     *
     * @param map 值
     * @param clz 需转换的类
     * @return bean
     */
    public static <T> T convert(Map<String, Object> map, Class<T> clz) {
        T t = null;
        try {
            t = clz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("clazz : {}, newInstance error", clz, e);
        }
        BeanMap beanMap = BeanMap.create(t);
        beanMap.putAll(map);
        return t;
    }

    /**
     * 将List<T>转换为List<Map<String, Object>>
     *
     * @param list 列表
     * @return List<Map>
     */
    public static <T> List<Map<String, Object>> convert(List<T> list) {
        if (Objects.isNull(list)) {
            return null;
        }
        List<Map<String, Object>> listMap = new ArrayList<>(16);
        list.forEach(t -> listMap.add(convert(t)));
        return listMap;
    }

    /**
     * 将List<Map<String,Object>>转换为List<T>
     *
     * @param listMap 列表map
     * @param clazz   需转换的类
     * @return list<bean>
     */
    public static <T> List<T> convert(List<Map<String, Object>> listMap, Class<T> clazz) {
        if (Objects.isNull(listMap)) {
            return null;
        }
        List<T> list = new ArrayList<>(16);
        listMap.forEach(map -> list.add(convert(map, clazz)));
        return list;
    }

    /**
     * 获取所有field
     *
     * @param clz 类
     * @return Field[]
     */
    public static Field[] getAllFields(Class<?> clz) {
        Field[] fields = new Field[] {};
        if (Objects.nonNull(clz)) {
            do {
                fields = ArrayUtils.addAll(fields, clz.getDeclaredFields());
                clz = clz.getSuperclass();
            } while (clz != Object.class && clz != null);
        }
        return fields;
    }

    public static String objectToJSONString(Object obj) {
        return objectToJSONString(obj, DateTimeUtils.DEFAULT_DATETIME_FORMAT);
    }

    public static String objectToJSONString(Object obj, String dateFormat) {
        try {
            return objectMapper
                    .writer()
                    .with(new SimpleDateFormat(dateFormat))
                    .writeValueAsString(obj);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public static void writeJsonObjectToStream(Object obj, OutputStream outputStream)
            throws Exception {
        try {
            objectMapper.writer().writeValue(outputStream, obj);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public static <T> T jsonStringToObject(String jsonStr, Class<T> clazz, String dateFormat)
            throws Exception {

        return objectMapper.readerFor(clazz).readValue(jsonStr);
    }

    public static <T> T readJsonObjectFromInputStream(InputStream inputStream, Class<T> clazz)
            throws Exception {
        try {

            return objectMapper.readerFor(clazz).readValue(inputStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public static <T> T readJsonObjectFromInputStream(InputStream inputStream) throws Exception {

        return objectMapper.reader().readValue(inputStream);
    }

    public static <T> T jsonStringToObject(String jsonStr, Class<T> clazz) throws Exception {
        return jsonStringToObject(jsonStr, clazz, DateTimeUtils.DEFAULT_DATETIME_FORMAT);
    }

    public static <T> T jsonStringToObject(
            String jsonStr, String dateFormat, Class<?> collectionClass, Class<?>... elementClasses)
            throws Exception {
        JavaType javaType =
                objectMapper
                        .getTypeFactory()
                        .constructParametricType(collectionClass, elementClasses);

        return objectMapper.readerFor(javaType).readValue(jsonStr);
    }

    public static <T> T jsonStringToObject(
            String jsonStr, Class<?> collectionClass, Class<?>... elementClasses) throws Exception {
        return jsonStringToObject(
                jsonStr, DateTimeUtils.DEFAULT_DATETIME_FORMAT, collectionClass, elementClasses);
    }

    /**
     * 将sourceValue转换成指定Java类型（类型兼容模式）
     *
     * @param sourceValue
     * @param targetValueJavaTypeName
     * @return
     */
    public static Object convertValueByTypeCompatiable(
            Object sourceValue, String targetValueJavaTypeName) {
        Object value = sourceValue;
        String type = StringUtils.trimToEmpty(targetValueJavaTypeName);
        if (type.equalsIgnoreCase("Float")) {
            if (value instanceof Number) value = Float.valueOf(((Number) value).floatValue());
            else if (value instanceof String) value = Float.parseFloat((String) value);
        } else if (type.equalsIgnoreCase("Double")) {
            if (value instanceof Number) value = Double.valueOf(((Number) value).doubleValue());
            else if (value instanceof String) value = Double.parseDouble((String) value);
        } else if (type.equalsIgnoreCase("Long")) {
            if (value instanceof Number) value = Long.valueOf(((Number) value).longValue());
            else if (value instanceof String) value = Long.parseLong((String) value);
        } else if (type.equalsIgnoreCase("Short")) {
            if (value instanceof Number) value = Short.valueOf(((Number) value).shortValue());
            else if (value instanceof String) value = Short.parseShort((String) value);
        } else if (type.equalsIgnoreCase("Byte")) {
            if (value instanceof Number) value = Byte.valueOf(((Number) value).byteValue());
            else if (value instanceof String) value = Byte.parseByte((String) value);
        } else if (type.equalsIgnoreCase("Integer")) {
            if (value instanceof Number) value = Integer.valueOf(((Number) value).intValue());
            else if (value instanceof String) value = Integer.parseInt((String) value);
        } else if (type.equalsIgnoreCase("BigInteger")) {
            if (value instanceof Number) value = BigInteger.valueOf(((Number) value).longValue());
            else if (value instanceof String)
                value = BigInteger.valueOf(Long.parseLong((String) value));
        } else if (type.equalsIgnoreCase("BigDecimal")) {
            if (value instanceof Number) value = BigDecimal.valueOf(((Number) value).doubleValue());
            else if (value instanceof String) value = new BigDecimal((String) value);
        } else if (type.equalsIgnoreCase("Date")) {
            if (value instanceof LocalDate)
                value = DateTimeUtils.localDateToDate((LocalDate) value);
            else if (value instanceof LocalDateTime)
                value = DateTimeUtils.localDateTimeToDate((LocalDateTime) value);
            else if (value instanceof String) value = DateTimeUtils.strToDate((String) value);
        } else if (type.equalsIgnoreCase("LocalDate")) {
            if (value instanceof Date) value = DateTimeUtils.dateToLocalDate((Date) value);
            else if (value instanceof LocalDateTime) value = ((LocalDateTime) value).toLocalDate();
            else if (value instanceof String) value = DateTimeUtils.strToLocalDate((String) value);
        } else if (type.equalsIgnoreCase("LocalDateTime")) {
            if (value instanceof Date) value = DateTimeUtils.dateToLocalDateTime((Date) value);
            else if (value instanceof LocalDate)
                value = DateTimeUtils.localDateToLocalDateTime((LocalDate) value);
            else if (value instanceof String)
                value = DateTimeUtils.strToLocalDateTime((String) value);
        } else if (type.equalsIgnoreCase("LocalTime")) {
            if (value instanceof Date) value = DateTimeUtils.dateToLocalTime((Date) value);
            else if (value instanceof LocalDateTime) value = ((LocalDateTime) value).toLocalTime();
            else if (value instanceof String) value = DateTimeUtils.strToLocalTime((String) value);
        } else if (type.equalsIgnoreCase("String")) {
            if (value instanceof Number) value = String.valueOf(value);
            else if (value instanceof LocalDate)
                value = DateTimeUtils.localDateToStr((LocalDate) value);
            else if (value instanceof LocalDateTime)
                value = DateTimeUtils.localDateTimeToStr((LocalDateTime) value);
            else if (value instanceof LocalTime)
                value = DateTimeUtils.localTimeToStr((LocalTime) value);
            else if (value instanceof Date) value = DateTimeUtils.dateToStr((Date) value);
        }
        return value;
    }

    /**
     * 将HashMap转换成JavaBean（类型兼容模式）
     *
     * @param map
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) {
        T bean = null;
        try {
            bean = clazz.getDeclaredConstructor().newInstance();
            mapToExistedBean(map, bean);
        } catch (Exception e) {
            throw new IllegalArgumentException("Entity conversion failed", e);
        }
        return bean;
    }

    public static <T> void mapToExistedBean(Map<String, Object> map, T bean) {
        try {
            PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(bean.getClass());
            for (PropertyDescriptor pd : pds) {
                Method writeMethod = pd.getWriteMethod();
                Object value = map.get(pd.getName());
                if (writeMethod != null && value != null) {
                    String type = pd.getPropertyType().getSimpleName();
                    try {
                        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                            writeMethod.setAccessible(true);
                        }
                        value = convertValueByTypeCompatiable(value, type);
                        writeMethod.invoke(bean, value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Entity conversion failed", e);
                    }
                }
            }
        } catch (BeansException e) {
            throw new IllegalArgumentException("Entity conversion failed", e);
        }
    }

    public static <T> T xmlToObject(InputStream inputStream, Class<T> clazz) throws Exception {
        try {
            return xmlMapper.readValue(inputStream, clazz);
        } catch (Exception e) {
            throw e;
        }
    }

    public static <T> T xmlToObject(String xmlContent, Class<T> clazz) {
        try {
            return xmlMapper.readValue(xmlContent, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Entity conversion failed", e);
        }
    }

    public static void objectToXml(Object object, OutputStream outputStream) throws Exception {
        try {
            xmlMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, object);
        } catch (Exception e) {
            throw e;
        }
    }

    public static <T extends Serializable> T deepCopy(T object)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (T) ois.readObject();
    }
}
