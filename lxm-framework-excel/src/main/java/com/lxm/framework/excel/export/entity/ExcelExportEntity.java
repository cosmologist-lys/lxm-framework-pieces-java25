package com.lxm.framework.excel.export.entity;

import com.lxm.framework.excel.common.enmus.ExcelStyleType;
import lombok.Data;
import org.apache.poi.ss.usermodel.CellType;

import java.lang.reflect.Method;
import java.util.List;

/**
 * excel 导出工具类,对cell类型做映射
 *
 * @author twenty2
 */
@Data
public class ExcelExportEntity {
    /**
     * 单元格类型
     */
    private CellType type;
    /**
     * 列名
     */
    protected String name;
    /**
     * 分组组名
     */
    protected String groupName;
    /**
     * 如果是MAP导出,这个是map的key
     */
    private Object key;
    /**
     * 列宽度，默认为0，根据列文本自适应，不超过30
     */
    private int width = 0;
    /**
     * 排序顺序
     */
    private int orderNum = 0;
    /**
     * 是否支持换行
     */
    private boolean isWrap;
    /**
     * 单元格纵向合并
     */
    private boolean mergeVertical;
    /**
     * 合并依赖`
     */
    private int[] mergeRely;
    /**
     * 后缀
     */
    private String suffix;
    /**
     * 统计
     */
    private boolean statistics;
    /**
     * 导出日期格式
     */
    private String format;
    /**
     * 数字格式化
     */
    private String numFormat;
    /**
     * 是否隐藏列
     */
    private boolean columnHidden;
    /**
     * 枚举导出属性字段
     */
    private String enumExportField;
    /**
     * 冰冻列
     */
    private ExcelStyleType styleType;
    /**
     * 这个是不是超链接,如果是需要实现接口返回对象
     */
    private boolean hyperlink;
    /**
     * 替换
     */
    private String[] replace;
    /**
     * 字典名称
     */
    private String dict;
    /**
     * 子对象
     */
    private List<ExcelExportEntity> list;
    /**
     * get方法，可能会多个，例如集合、pojo
     */
    private List<Method> getMethods;


    public int[] getMergeRely() {
        return mergeRely == null ? new int[0] : mergeRely;
    }
}
