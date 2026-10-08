package com.lxm.framework.excel.annotation;

import com.lxm.framework.excel.common.enmus.ExcelStyleType;
import org.apache.poi.ss.usermodel.CellType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 导出基本注释
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Excel {
    /**
     * 列名
     */
    String name();

    /**
     * 导出时有效，表头双行显示，聚合，排序以最小的值参与总体排序再内部排序，优先弱于 @ExcelEntity 的name属性
     */
    String groupName() default "";

    /**
     * 时间格式，相当于同时设置了exportFormat 和 importFormat
     */
    String format() default "";

    /**
     * 数字格式化，参数是Pattern，使用的对象是DecimalFormat
     */
    String numFormat() default "";

    /**
     * 导出时在excel中每个列的宽，导出时有效 单位为字符，一个汉字=2个字符 如 以列名列内容中较合适的长度 例如姓名列6 【姓名一般三个字】。默认为0，根据列文本自适应，不超过30
     * 性别列4【男女占1，但是列标题两个汉字】 限制1-255
     */
    int width() default 0;

    /**
     * 文字后缀，如% 90 变成90%
     */
    String suffix() default "";

    /**
     * 是否换行，导出时有效
     */
    boolean isWrap() default true;

    /**
     * 合并单元格依赖关系,索引从0开始，导出时有效，比如第二列合并是基于第一列 则{1}就可以了
     */
    int[] mergeRely() default {};

    /**
     * 纵向合并内容相同的单元格
     */
    boolean mergeVertical() default false;

    /**
     * 排序
     */
    int orderNum() default 0;

    /**
     * 值得替换  导出是{a_id，b_id} 导入反过来，所以只用写一个
     */
    String[] replace() default {};

    /**
     * 字典名称
     */
    String dict() default "";

    /**
     * 单元格类型
     */
    CellType type() default CellType._NONE;

    /**
     * 单元格样式，导出时有效
     */
    ExcelStyleType styleType() default ExcelStyleType.NONE;

    /**
     * 是否自动统计数据，导出时有效，如果是统计，true的话在最后追加一行统计，把所有数据都和
     */
    boolean statistics() default false;

    /**
     * 是否超链接，导出时有效，需要实现dataHandler接口返回对象
     */
    boolean hyperlink() default false;

    /**
     * 是否隐藏该列，导出时有效
     */
    boolean columnHidden() default false;

    /**
     * 枚举导出使用的字段，导出时有效
     */
    String enumExportField() default "";

    /**
     * 是否为导入字段，导入时有效，看看这个字段是不是导入的Excel中有，如果没有说明是错误的Excel
     */
    boolean importField() default false;

    /**
     * 枚举导入使用的函数
     */
    String enumImportMethod() default "";

}
