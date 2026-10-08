package com.lxm.framework.common.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: Lys
 * @Date 2023/1/10
 * @Describe
 **/
public class ObjectMapUtils {

    private static final Pattern pattern = Pattern.compile("[A-Z]");

    public static Map<String, String> obj2Map(Object obj, boolean toUnderline) throws Exception {
        Map<String, String> map = new HashMap<>();
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (key.compareToIgnoreCase("class") == 0) {
                continue;
            }
            Method getter = property.getReadMethod();
            Object value = getter != null ? getter.invoke(obj) : null;
            if (null != value) {
                String stringValue = String.valueOf(value);
                if (toUnderline) {
                    key = StringFormatUtils.underline(key);
                }
                map.put(key, stringValue);
            }
        }
        return map;
    }

    /**
     * 字典排序，然后组装成url带参形式的字符串
     *
     * @param arrs
     * @return
     */
    public static String sort(String[] arrs) {
        Arrays.sort(arrs);
        StringBuilder sb = new StringBuilder();
        int length = arrs.length;
        for (int i = 0; i < length; i++) {
            var one = arrs[i];
            if (i != length - 1) {
                sb.append(one).append("&");
            } else {
                sb.append(one);
            }
        }
        return sb.toString();
    }

    public static String[] map2Arr(Map<String, String> map) {
        String[] arrs = new String[map.size()];
        List<String> strs = new ArrayList<>();
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String temp = entry.getKey() + "=" + entry.getValue();
            strs.add(temp);
        }
        strs.toArray(arrs);
        return arrs;
    }
}
