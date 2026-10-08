package com.lxm.framework.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringFormatUtils 格式化处理
 *
 * @author lvluogang
 */
public class StringFormatUtils {

    private static final Pattern PATTERN_LETTER = Pattern.compile("[A-Z]");
    private static final Pattern PATTERN_UNDERLINE = Pattern.compile("_(\\w)");

    /**
     * 判断是否为空
     *
     * @param obj 对象
     * @return Boolean
     */
    public static Boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        return String.valueOf(obj).trim().isEmpty();
    }

    /**
     * 判断是否不为空
     *
     * @param obj 对象
     * @return Boolean
     */
    public static Boolean isNotNullOrNotEmpty(Object obj) {
        return !isNullOrEmpty(obj);
    }

    /**
     * 下划线转驼峰
     *
     * @param str 字符
     * @return String
     */
    public static String camel(String str) {
        // 利用正则删除下划线，把下划线后一位改成大写
        Matcher matcher = PATTERN_UNDERLINE.matcher(str);
        StringBuffer sb = new StringBuffer(str);
        if (matcher.find()) {
            sb = new StringBuffer();
            // 将当前匹配子串替换为指定字符串，并且将替换后的子串以及其之前到上次匹配子串之后的字符串段添加到一个StringBuffer对象里。
            // 正则之前的字符和被替换的字符
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
            // 把之后的也添加到StringBuffer对象里
            matcher.appendTail(sb);
        } else {
            return sb.toString();
        }
        return camel(sb.toString());
    }

    /**
     * 驼峰转下划线
     *
     * @param str 字符
     * @return String
     */
    public static String underline(String str) {
        Matcher matcher = PATTERN_LETTER.matcher(str);
        StringBuffer sb = new StringBuffer(str);
        if (matcher.find()) {
            sb = new StringBuffer();
            // 将当前匹配子串替换为指定字符串，并且将替换后的子串以及其之前到上次匹配子串之后的字符串段添加到一个StringBuffer对象里。
            // 正则之前的字符和被替换的字符
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
            // 把之后的也添加到StringBuffer对象里
            matcher.appendTail(sb);
        } else {
            return sb.toString();
        }
        return underline(sb.toString());
    }

    public static String stripStart(String str, String stripStr) {
        StringBuilder sb = new StringBuilder(StringUtils.trimToEmpty(str));
        if (stripStr == null || stripStr.length() == 0) {
            return sb.toString();
        }
        while (sb.length() >= stripStr.length()
                && sb.substring(0, stripStr.length()).equalsIgnoreCase(stripStr)) {
            sb.delete(0, stripStr.length());
        }
        return sb.toString();
    }

    public static String stripEnd(String str, String stripStr) {
        StringBuilder sb = new StringBuilder(StringUtils.trimToEmpty(str));
        if (stripStr == null || stripStr.length() == 0) {
            return sb.toString();
        }
        while (sb.length() >= stripStr.length()
                && sb.substring(sb.length() - stripStr.length(), sb.length())
                        .equalsIgnoreCase(stripStr)) {
            sb.delete(sb.length() - stripStr.length(), sb.length());
        }
        return sb.toString();
    }

    public static String rightPadAlarmStatusString(String alarmStatusStr, int totalLenth) {
        String s = StringUtils.trimToEmpty(alarmStatusStr);
        return StringUtils.rightPad(s, totalLenth, '0');
    }

    public static String getDbName(String url) {
        if (StringUtils.contains(url, "3306/")) {
            return StringUtils.substringBetween(url, "3306/", "?");
        }
        String afterCut = StringUtils.substringAfterLast(url, "/");
        return StringUtils.substringBeforeLast(afterCut, "?");
    }
}
