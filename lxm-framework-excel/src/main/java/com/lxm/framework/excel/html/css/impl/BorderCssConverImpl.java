/**
 * Copyright 2013-2015 JueYue (qrb.jueyue@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lxm.framework.excel.html.css.impl;

import com.lxm.framework.excel.html.css.ICssConvertToExcel;
import com.lxm.framework.excel.html.css.ICssConvertToHtml;
import com.lxm.framework.excel.html.entity.HtmlCssConstant;
import com.lxm.framework.excel.html.entity.style.CellStyleBorderEntity;
import com.lxm.framework.excel.html.entity.style.CellStyleEntity;
import com.lxm.framework.excel.util.PoiCssUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 * 边框转换实现类
 */
@Slf4j
public class BorderCssConverImpl implements ICssConvertToExcel, ICssConvertToHtml {

    @Override
    public String convertToHtml(Cell cell, CellStyle cellStyle, CellStyleEntity style) {
        return null;
    }

    @Override
    public void convertToExcel(Cell cell, CellStyle cellStyle, CellStyleEntity style) {
        if (style == null || style.getBorder() == null) {
            return;
        }
        CellStyleBorderEntity border = style.getBorder();
        for (String pos : new String[]{HtmlCssConstant.TOP, HtmlCssConstant.RIGHT, HtmlCssConstant.BOTTOM, HtmlCssConstant.LEFT}) {
            String posName = StringUtils.capitalize(pos.toLowerCase());
            // color
            String colorAttr = null;
            try {
                colorAttr = (String) MethodUtils.invokeMethod(border,
                        "getBorder" + posName + "Color");
            } catch (Exception e) {
                log.error("Set Border Style Error Caused.", e);
            }
            if (StringUtils.isNotEmpty(colorAttr)) {
                if (cell instanceof HSSFCell) {
                    HSSFColor poiColor = PoiCssUtils
                            .parseColor((HSSFWorkbook) cell.getSheet().getWorkbook(), colorAttr);
                    if (poiColor != null) {
                        try {
                            MethodUtils.invokeMethod(cellStyle, "set" + posName + "BorderColor",
                                    poiColor.getIndex());
                        } catch (Exception e) {
                            log.error("Set Border Color Error Caused.", e);
                        }
                    }
                }
                if (cell instanceof XSSFCell) {
                    XSSFColor poiColor = PoiCssUtils.parseColor(colorAttr);
                    if (poiColor != null) {
                        try {
                            MethodUtils.invokeMethod(cellStyle, "set" + posName + "BorderColor",
                                    poiColor);
                        } catch (Exception e) {
                            log.error("Set Border Color Error Caused.", e);
                        }
                    }
                }
            }
            // width
            int width = 0;
            try {
                String widthStr = (String) MethodUtils.invokeMethod(border,
                        "getBorder" + posName + "Width");
                if (PoiCssUtils.isNum(widthStr)) {
                    width = Integer.parseInt(widthStr);
                }
            } catch (Exception e) {
                log.error("Set Border Style Error Caused.", e);
            }
            String styleValue = null;
            try {
                styleValue = (String) MethodUtils.invokeMethod(border,
                        "getBorder" + posName + "Style");
            } catch (Exception e) {
                log.error("Set Border Style Error Caused.", e);
            }
            BorderStyle shortValue = BorderStyle.NONE;
            // empty or solid
            if (StringUtils.isBlank(styleValue) || "solid".equals(styleValue)) {
                if (width > 2) {
                    shortValue = BorderStyle.THICK;
                } else if (width > 1) {
                    shortValue = BorderStyle.MEDIUM;
                } else {
                    shortValue = BorderStyle.THIN;
                }
            } else if (ArrayUtils.contains(new String[]{HtmlCssConstant.NONE, HtmlCssConstant.HIDDEN}, styleValue)) {
                shortValue = BorderStyle.NONE;
            } else if (HtmlCssConstant.DOUBLE.equals(styleValue)) {
                shortValue = BorderStyle.DOUBLE;
            } else if (HtmlCssConstant.DOTTED.equals(styleValue)) {
                shortValue = BorderStyle.DOTTED;
            } else if (HtmlCssConstant.DASHED.equals(styleValue)) {
                if (width > 1) {
                    shortValue = BorderStyle.MEDIUM_DASHED;
                } else {
                    shortValue = BorderStyle.DASHED;
                }
            }
            // border style
            if (shortValue != BorderStyle.NONE) {
                try {
                    MethodUtils.invokeMethod(cellStyle, "setBorder" + posName, shortValue);
                } catch (Exception e) {
                    log.error("Set Border Style Error Caused.", e);
                }
            }
        }
    }

}
