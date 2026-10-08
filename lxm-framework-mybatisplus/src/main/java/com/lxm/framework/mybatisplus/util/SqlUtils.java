package com.lxm.framework.mybatisplus.util;

import com.baomidou.mybatisplus.annotation.DbType;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author twenty2
 */
public class SqlUtils {

    /**
     * 返回数据库类型
     *
     * @return String
     */
    public static String getDbTypeStr() {
        return DbType.MYSQL.getDb();
    }

    /**
     * 模糊查询
     *
     * @param column     列名
     * @param expression 值表达式
     * @return String
     */
    public static String like(String column, String expression) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "charindex(#{" + expression + "}, " + column + ") > 0";
        } else if (DbType.ORACLE == dbType) {
            res = column + " LIKE CONCAT(%, #{" + expression + "}, %)";
        } else {
            res = "instr(" + column + ",#{" + expression + "}) > 0";
        }
        return res;
    }

    /**
     * 模糊查询
     *
     * @param column     列名
     * @param expression 值表达式
     * @return String
     */
    public static String likeLeft(String column, String expression) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "charindex(#{" + expression + "}, " + column + ") > 0";
        } else if (DbType.ORACLE == dbType) {
            res = column + " LIKE CONCAT(%, #{" + expression + "})";
        } else {
            res = "instr(" + column + ",#{" + expression + "}) > 0";
        }
        return res;
    }

    /**
     * 模糊查询
     *
     * @param column     列名
     * @param expression 值表达式
     * @return String
     */
    public static String likeRight(String column, String expression) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "charindex(#{" + expression + "}, " + column + ") > 0";
        } else if (DbType.ORACLE == dbType) {
            res = column + " LIKE CONCAT(#{" + expression + "}, %)";
        } else {
            res = "instr(" + column + ",#{" + expression + "}) > 0";
        }
        return res;
    }

    /**
     * 拼接字符串
     *
     * @param expression 值表达式，多个以逗号分隔
     * @return String
     */
    public static String concat(String expression) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        if (Objects.isNull(expression)) {
            return null;
        }
        String[] arr = StringUtils.split(expression, ",");
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = String.join("+", arr);
        } else if (DbType.ORACLE == dbType) {
            res = "CONCAT(" + String.join(",", arr) + ")";
        } else {
            res = "CONCAT(" + String.join(",", arr) + ")";
        }
        return res;
    }

    /**
     * 时间 ==
     *
     * @param column     列名
     * @param expression 值表达式
     * @param pattern    格式化
     *                   %W 星期名字(Sunday……Saturday)
     *                   %D 有英语前缀的月份的日期(1st, 2nd, 3rd, 等等）
     *                   %Y 年, 数字, 4 位
     *                   %y 年, 数字, 2 位
     *                   %a 缩写的星期名字(Sun……Sat)
     *                   %d 月份中的天数, 数字(00……31)
     *                   %e 月份中的天数, 数字(0……31)
     *                   %m 月, 数字(01……12)
     *                   %c 月, 数字(1……12)
     *                   %b 缩写的月份名字(Jan……Dec)
     *                   %j 一年中的天数(001……366)
     *                   %H 小时(00……23)
     *                   %k 小时(0……23)
     *                   %h 小时(01……12)
     *                   %I 小时(01……12)
     *                   %l 小时(1……12)
     *                   %i 分钟, 数字(00……59)
     *                   %r 时间,12 小时(hh:mm:ss [AP]M)
     *                   %T 时间,24 小时(hh:mm:ss)
     *                   %S 秒(00……59)
     *                   %s 秒(00……59)
     *                   %p AM或PM
     *                   %w 一个星期中的天数(0=Sunday ……6=Saturday ）
     *                   %U 星期(0……52), 这里星期天是星期的第一天
     *                   %u 星期(0……52), 这里星期一是星期的第一天
     *                   %% 一个文字“%"
     * @return String
     */
    public static String compareDate(String column, String expression, String pattern) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') = #{" + expression + "}";
        } else if (DbType.ORACLE == dbType) {
            res = "TO_CHAR(" + column + ", '" + pattern + "') = #{" + expression + "}";
        } else {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') = #{" + expression + "}";
        }
        return res;
    }

    /**
     * 时间 >=
     *
     * @param column
     * @param expression
     * @param pattern
     * @return
     */
    public static String compareDateGE(String column, String expression, String pattern) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') >= #{" + expression + "}";
        } else if (DbType.ORACLE == dbType) {
            res = "TO_CHAR(" + column + ", '" + pattern + "') >= #{" + expression + "}";
        } else {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') >= #{" + expression + "}";
        }
        return res;
    }

    /**
     * 时间 <=
     *
     * @param column
     * @param expression
     * @param pattern
     * @return
     */
    public static String compareDateLE(String column, String expression, String pattern) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') <= #{" + expression + "}";
        } else if (DbType.ORACLE == dbType) {
            res = "TO_CHAR(" + column + ", '" + pattern + "') <= #{" + expression + "}";
        } else {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') <= #{" + expression + "}";
        }
        return res;
    }

    /**
     * 时间 >
     *
     * @param column
     * @param expression
     * @param pattern
     * @return
     */
    public static String compareDateGT(String column, String expression, String pattern) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') > #{" + expression + "}";
        } else if (DbType.ORACLE == dbType) {
            res = "TO_CHAR(" + column + ", '" + pattern + "') > #{" + expression + "}";
        } else {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') > #{" + expression + "}";
        }
        return res;
    }

    /**
     * 时间 <
     *
     * @param column
     * @param expression
     * @param pattern
     * @return
     */
    public static String compareDateLT(String column, String expression, String pattern) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') < #{" + expression + "}";
        } else if (DbType.ORACLE == dbType) {
            res = "TO_CHAR(" + column + ", '" + pattern + "') < #{" + expression + "}";
        } else {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "') < #{" + expression + "}";
        }
        return res;
    }

    /**
     * 时间格式化
     *
     * @param column  列名
     * @param pattern 格式化
     *                %W 星期名字(Sunday……Saturday)
     *                %D 有英语前缀的月份的日期(1st, 2nd, 3rd, 等等）
     *                %Y 年, 数字, 4 位
     *                %y 年, 数字, 2 位
     *                %a 缩写的星期名字(Sun……Sat)
     *                %d 月份中的天数, 数字(00……31)
     *                %e 月份中的天数, 数字(0……31)
     *                %m 月, 数字(01……12)
     *                %c 月, 数字(1……12)
     *                %b 缩写的月份名字(Jan……Dec)
     *                %j 一年中的天数(001……366)
     *                %H 小时(00……23)
     *                %k 小时(0……23)
     *                %h 小时(01……12)
     *                %I 小时(01……12)
     *                %l 小时(1……12)
     *                %i 分钟, 数字(00……59)
     *                %r 时间,12 小时(hh:mm:ss [AP]M)
     *                %T 时间,24 小时(hh:mm:ss)
     *                %S 秒(00……59)
     *                %s 秒(00……59)
     *                %p AM或PM
     *                %w 一个星期中的天数(0=Sunday ……6=Saturday ）
     *                %U 星期(0……52), 这里星期天是星期的第一天
     *                %u 星期(0……52), 这里星期一是星期的第一天
     *                %% 一个文字“%"
     * @return String
     */
    public static String dateFormat(String column, String pattern) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "')";
        } else if (DbType.ORACLE == dbType) {
            res = "TO_CHAR(" + column + ", '" + pattern + "')";
        } else {
            res = "DATE_FORMAT(" + column + ", '" + pattern + "')";
        }
        return res;
    }

    /**
     * 比较两个日期相差天数
     *
     * @param column
     * @param expression
     * @param pattern
     * @return
     */
    public static String DiffDateByDay(String column, String expression, String pattern) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res =
                    "DATEDIFF(day,DATE_FORMAT("
                            + column
                            + ", '"
                            + pattern
                            + "'),#{"
                            + expression
                            + "})";
        } else if (DbType.ORACLE == dbType) {
            res =
                    "ROUND(TO_NUMBER(TO_CHAR("
                            + expression
                            + ", '"
                            + pattern
                            + "') - TO_CHAR("
                            + column
                            + ", '"
                            + pattern
                            + "')))";
        } else {
            res = "DATEDIFF(#{" + expression + "},DATE_FORMAT(" + column + ", '" + pattern + "'))";
        }
        return res;
    }

    /**
     * 获取当前时间
     */
    public static String getNow() {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "GETDATE()";
        } else if (DbType.ORACLE == dbType) {
            res = "SYSDATE";
        } else {
            res = "NOW()";
        }
        return res;
    }

    /**
     * 字符串截取
     *
     * @param column     列名
     * @param expression 截取开始位置
     * @param len        截取长度
     * @return String
     */
    public static String subStr(String column, String expression, int len) {
        DbType dbType = DbType.MYSQL;
        /*if (StringUtils.isNotBlank(url)) {
            dbType = JdbcUtils.getDbType(url);
        }*/
        String res;
        if (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005) {
            res = "substring(" + column + ", #{" + expression + "}, " + len + ")";
        } else if (DbType.ORACLE == dbType) {
            res = "substr(" + column + ", #{" + expression + "}, " + len + ")";
        } else {
            res = "substr(" + column + ", #{" + expression + "}, " + len + ")";
        }
        return res;
    }
}
