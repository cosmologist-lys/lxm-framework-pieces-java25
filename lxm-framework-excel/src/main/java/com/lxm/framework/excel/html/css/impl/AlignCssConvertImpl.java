package com.lxm.framework.excel.html.css.impl;

import com.lxm.framework.excel.html.css.ICssConvertToExcel;
import com.lxm.framework.excel.html.css.ICssConvertToHtml;
import com.lxm.framework.excel.html.entity.HtmlCssConstant;
import com.lxm.framework.excel.html.entity.style.CellStyleEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public class AlignCssConvertImpl implements ICssConvertToExcel, ICssConvertToHtml {

    @Override
    public String convertToHtml(Cell cell, CellStyle cellStyle, CellStyleEntity style) {

        return null;
    }

    @Override
    public void convertToExcel(Cell cell, CellStyle cellStyle, CellStyleEntity style) {
        // align
        if (HtmlCssConstant.RIGHT.equals(style.getAlign())) {
            cellStyle.setAlignment(HorizontalAlignment.RIGHT);
        } else if (HtmlCssConstant.CENTER.equals(style.getAlign())) {
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
        } else if (HtmlCssConstant.LEFT.equals(style.getAlign())) {
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
        } else if (HtmlCssConstant.JUSTIFY.equals(style.getAlign())) {
            cellStyle.setAlignment(HorizontalAlignment.JUSTIFY);
        }
        // vertical align
        if (HtmlCssConstant.TOP.equals(style.getVetical())) {
            cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        } else if (HtmlCssConstant.CENTER.equals(style.getAlign())) {
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        } else if (HtmlCssConstant.BOTTOM.equals(style.getAlign())) {
            cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
        } else if (HtmlCssConstant.JUSTIFY.equals(style.getAlign())) {
            cellStyle.setVerticalAlignment(VerticalAlignment.JUSTIFY);
        }
    }

}
