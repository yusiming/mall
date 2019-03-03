package com.mall.util;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * 使用joda-time完成时间日期的转化
 *
 * @author yusiming
 * @date 2018/11/25 20:15
 */
public class DateTimeUtil {
    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 使用指定的格式，完成从字符串到Date对象的转换
     *
     * @param dateTimeStr 需要转换为Date对象的字符串
     * @param formatStr   转化的格式
     * @return 转换之后的Date对象
     */
    public static Date strToDate(String dateTimeStr, String formatStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 使用默认的格式完成从字符串到Date对象的转换
     *
     * @param dateTimeStr 需要转换的字符串
     * @return 转换之后的Date对象
     */
    public static Date strToDate(String dateTimeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 使用指定的格式，完成从Date对象到字符串的转换
     *
     * @param date      被转化弄得date对象
     * @param formatStr 格式
     * @return 转换之后的字符串对象
     */
    public static String DateToStr(Date date, String formatStr) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }

    /**
     * 使用默认的格式，完成从Date对象到字符串的转换
     *
     * @param date 被转换的date对象
     * @return 转换之后的字符串对象
     */
    public static String DateToStr(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }
}
