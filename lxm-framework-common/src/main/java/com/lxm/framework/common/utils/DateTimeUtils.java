package com.lxm.framework.common.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {
    public final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public final static String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public final static String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public final static String DEFAULT_DATE_FORMAT_SHORT = "yyMMdd";
    public final static String DEFAULT_TIME_FORMAT_SHORT = "HHmmss";
    public final static String DEFAULT_DATETIME_FORMAT_SHORT = "yyMMddHHmmss";

    public static LocalDate dateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalDate();
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static LocalTime dateToLocalTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalTime();
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    public static Date localDateToDate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }

    public static Date localTimeToDate(Date targetDate, LocalTime localTime) {
        LocalDate localDate = dateToLocalDate(targetDate);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    public static String localDateToStr(LocalDate value) {
        return value.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    public static String localDateTimeToStr(LocalDateTime value) {
        return value.format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }

    public static String localDateTimeToStr(LocalDateTime value, String format) {
        return value.format(DateTimeFormatter.ofPattern(format));
    }

    public static String localTimeToStr(LocalTime value) {
        return value.format(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT));
    }

    public static LocalDate strToLocalDate(String value) {
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    public static LocalDateTime localDateToLocalDateTime(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return dateToLocalDateTime(Date.from(instant));
    }

    public static LocalDateTime strToLocalDateTime(String value) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }

    public static LocalTime strToLocalTime(String value) {
        return LocalTime.parse(value, DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT));
    }

    public static Date strToDate(String value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
        try {
            return sdf.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateToStr(Date value) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
        return sdf.format(value);
    }

    public static String localDateTimeToYmdStr(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    /**
     * 获取某天开始时间
     */
    public static LocalDateTime getDayBeginTime(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().atStartOfDay();
    }

    /**
     * 获取某天结束时间
     */
    public static LocalDateTime getDayEndTime(LocalDateTime localDateTime) {
        localDateTime.atZone(ZoneId.systemDefault());
        return localDateTime.toLocalDate().atTime(23, 59, 59, 999);
    }

    /**
     * 获取某月开始时间
     */
    public static LocalDateTime getMonthBeginTime(LocalDateTime localDateTime) {
        YearMonth yearMonth = YearMonth.of(localDateTime.getYear(), localDateTime.getMonth());
        LocalDate localDate = yearMonth.atDay(1);
        return localDate.atStartOfDay();
    }

    /**
     * 获取某月结束时间
     */
    public static LocalDateTime getMonthEndTime(LocalDateTime localDateTime) {
        YearMonth yearMonth = YearMonth.of(localDateTime.getYear(), localDateTime.getMonth());
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        return endOfMonth.atTime(23, 59, 59, 999);
    }

    /**
     * 时间戳转LocalDateTime
     *
     * @param time 单位 秒
     */
    public static LocalDateTime millisToLocalTime(long time) {
        Instant instant = Instant.ofEpochMilli(time);
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static long toMillis(LocalDateTime time){
        return Timestamp.valueOf(time).getTime();
    }

    public static LocalDateTime toLocalDateTime(long expire, TimeUnit timeUnit) {
        final int jump = 1000;
        var now = LocalDateTime.now();
        long nanos = 0;
        switch (timeUnit) {
            case NANOSECONDS:
                nanos = expire;
                break;
            case MICROSECONDS:
                nanos = expire * jump;
                break;
            case MILLISECONDS:
                nanos = expire * jump * jump;
                break;
            case SECONDS:
                nanos = expire * jump * jump * jump;
                break;
            case MINUTES:
                nanos = expire * 60 * jump * jump * jump;
                break;
            case HOURS:
                nanos = expire * 3600 * jump * jump * jump;
                break;
            case DAYS:
                nanos = expire * 3600 * 24 * jump * jump * jump;
                break;
        }
        return now.plus(nanos, ChronoUnit.NANOS);
    }

    /**
     * Local时间格式化
     *
     * @param temporal
     */
    public static String format(Temporal temporal, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        if (temporal instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) temporal;
            return localDateTime.format(formatter);
        } else if (temporal instanceof LocalDate) {
            LocalDate localDate = (LocalDate) temporal;
            return localDate.format(formatter);
        } else if (temporal instanceof LocalTime) {
            LocalTime localTime = (LocalTime) temporal;
            return localTime.format(formatter);
        }
        return null;
    }

}
