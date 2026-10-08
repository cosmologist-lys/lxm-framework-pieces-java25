package com.lxm.framework.excel.entity;

import com.lxm.framework.excel.common.ExcelBaseParams;
import com.lxm.framework.excel.common.enmus.ExcelType;
import com.lxm.framework.excel.export.styler.IExcelExportStyler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

/**
 * Excel 导出参数
 *
 * @author twenty2
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ExportParams extends ExcelBaseParams {

    /**
     * Excel 导出版本，默认07以上版本
     */
    private ExcelType type = ExcelType.XSSF;

    /**
     * sheet名称
     */
    private String sheetName;

    /**
     * 是否创建标题
     */
    private boolean createTitle = true;

    /**
     * 标题
     */
    private String title = "文件";

    /**
     * 标题高度
     */
    private short titleHeight = 10;

    /**
     * 标题颜色 例如:HSSFColor.SKY_BLUE.index 默认
     */
    private short titleColor = HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE.getIndex();

    /**
     * 第二标题
     */
    private String secondTitle;

    /**
     * 第二标题高度
     */
    private short secondTitleHeight = 8;

    /**
     * 第二标题水平位置，默认靠左
     */
    private HorizontalAlignment secondTitleStyle = HorizontalAlignment.LEFT;

    /**
     * 第二标题字体颜色，默认黑色
     */
    private short secondTitleFontColor = HSSFColor.HSSFColorPredefined.BLACK.getIndex();

    /**
     * 第二标题字体大小，默认10
     */
    private short secondTitleFontSize = 10;

    /**
     * 是否需要添加序号
     */
    private boolean addIndex;

    /**
     * 自定义序号名称
     */
    private String indexName = "序号";

    /**
     * 是否创建表头
     */
    private boolean createHeader = true;

    /**
     * 是否固定表头
     */
    private boolean isFixedTitle = true;

    /**
     * 表头颜色
     */
    private short headerColor = HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE.getIndex();

    /**
     * 冰冻列
     */
    private int freezeCol;

    /**
     * 自定义导出样式
     */
    private IExcelExportStyler style;

    /**
     * 单sheet最大值
     * 03版本默认6W行,07默认100W
     */
    private int maxNum;

    /**
     * 导出时在excel中每个列的高度 单位为字符，一个汉字=2个字符
     * 全局设置,优先使用
     */
    public short height = 10;

    /**
     * 过滤的属性，pojo属性
     */
    private String[] exclusions;

    /**
     * 包含的属性，pojo属性
     */
    private String[] inclusions;

    public ExportParams() {}

    public ExportParams(String title, String sheetName) {
        this.title = title;
        this.sheetName = sheetName;
    }

    public ExportParams(String title, String sheetName, ExcelType type) {
        this.title = title;
        this.sheetName = sheetName;
        this.type = type;
    }

    public ExportParams(String title, String secondTitle, String sheetName) {
        this.title = title;
        this.secondTitle = secondTitle;
        this.sheetName = sheetName;
    }

    public short getSecondTitleHeight() {
        return (short) (secondTitleHeight * 50);
    }

    public short getTitleHeight() {
        return (short) (titleHeight * 50);
    }

    public short getHeight() {
        return (short) (height * 50);
    }

    public short getHeaderHeight() {
        return (short) (titleHeight * 50);
    }

    public int getMaxNum() {
        if (maxNum == 0) {
            if (type == ExcelType.HSSF) {
                maxNum = 60000;
            } else {
                maxNum = 1000000;
            }
        } else if (maxNum > 60000) {
            if (type == ExcelType.HSSF) {
                maxNum = 60000;
            }
        }
        return maxNum;
    }
}
