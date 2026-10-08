package com.lxm.framework.excel.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author twenty2
 */
@Slf4j
public class PoiCssUtils {
    /**
     * matches #rgb
     */
    private static final String COLOR_PATTERN_VALUE_SHORT = "^(#(?:[a-f]|\\d){3})$";

    /**
     * matches #rrggbb
     */
    private static final String COLOR_PATTERN_VALUE_LONG = "^(#(?:[a-f]|\\d{2}){3})$";

    /**
     * matches #rgb(r, g, b)
     **/
    private static final String COLOR_PATTERN_RGB =
            "^(rgb\\s*\\(\\s*(.+)\\s*,\\s*(.+)\\s*,\\s*(.+)\\s*\\))$";

    /**
     * matches #rgb abc
     **/
    private static final Pattern COLOR_PATTERN_VALUE_SHORT_PATTERN = Pattern.compile("([a-f]|\\d)");

    /**
     * matches #字母、百分数
     **/
    private static final Pattern INT_PATTERN = Pattern.compile("^(\\d+)(?:\\w+|%)?$");

    /**
     * matches #数字、百分数
     **/
    private static final Pattern INT_AND_PER_PATTERN = Pattern.compile("^(\\d*\\.?\\d+)\\s*(%)?$");

    /**
     * get color name
     *
     * @param color HSSFColor
     * @return color name
     */
    private static String colorName(Class<? extends HSSFColor> color) {
        return color.getSimpleName().replace("_", "").toLowerCase();
    }

    /**
     * get int value of string
     *
     * @param strValue string value
     * @return int value
     */
    public static int getInt(String strValue) {
        int value = 0;
        if (StringUtils.isNotBlank(strValue)) {
            Matcher m = INT_PATTERN.matcher(strValue);
            if (m.find()) {
                value = Integer.parseInt(m.group(1));
            }
        }
        return value;
    }

    /**
     * check number string
     *
     * @param strValue string
     * @return true if string is number
     */
    public static boolean isNum(String strValue) {
        return StringUtils.isNotBlank(strValue) && strValue.matches("^\\d+(\\w+|%)?$");
    }

    /**
     * process color
     *
     * @param color color to process
     * @return color after process
     */
    public static String processColor(String color) {
        log.debug("Process Color [{}].", color);
        String colorRtn = null;
        if (StringUtils.isNotBlank(color)) {
            HSSFColor poiColor = null;
            // #rgb -> #rrggbb
            if (color.matches(COLOR_PATTERN_VALUE_SHORT)) {
                log.debug("Short Hex Color [{}] Found.", color);
                StringBuffer sbColor = new StringBuffer();
                Matcher m = COLOR_PATTERN_VALUE_SHORT_PATTERN.matcher(color);
                while (m.find()) {
                    m.appendReplacement(sbColor, "$1$1");
                }
                colorRtn = sbColor.toString();
                log.debug("Translate Short Hex Color [{}] To [{}].", color, colorRtn);
            }
            // #rrggbb
            else if (color.matches(COLOR_PATTERN_VALUE_LONG)) {
                colorRtn = color;
                log.debug("Hex Color [{}] Found, Return.", color);
            }
            // rgb(r, g, b)
            else if (color.matches(COLOR_PATTERN_RGB)) {
                Matcher m = Pattern.compile(COLOR_PATTERN_RGB).matcher(color);
                if (m.matches()) {
                    log.debug("RGB Color [{}] Found.", color);
                    colorRtn =
                            convertColor(
                                    calcColorValue(m.group(2)),
                                    calcColorValue(m.group(3)),
                                    calcColorValue(m.group(4)));
                    log.debug("Translate RGB Color [{}] To Hex [{}].", color, colorRtn);
                }
            }
            // color name, red, green, ...
            else if ((poiColor = getHssfColor(color)) != null) {
                log.debug("Color Name [{}] Found.", color);
                short[] t = poiColor.getTriplet();
                colorRtn = convertColor(t[0], t[1], t[2]);
                log.debug("Translate Color Name [{}] To Hex [{}].", color, colorRtn);
            }
        }
        return colorRtn;
    }

    private static HSSFColor getHssfColor(String color) {
        HSSFColor hssfColor;
        switch (color.replace("_", "").toUpperCase()) {
            case "BLACK":
                hssfColor = HSSFColor.HSSFColorPredefined.BLACK.getColor();
                break;
            case "BROWN":
                hssfColor = HSSFColor.HSSFColorPredefined.BROWN.getColor();
                break;
            case "OLIVEGREEN":
                hssfColor = HSSFColor.HSSFColorPredefined.OLIVE_GREEN.getColor();
                break;
            case "DARKGREEN":
                hssfColor = HSSFColor.HSSFColorPredefined.DARK_GREEN.getColor();
                break;
            case "DARKTEAL":
                hssfColor = HSSFColor.HSSFColorPredefined.DARK_TEAL.getColor();
                break;
            case "DARKBLUE":
                hssfColor = HSSFColor.HSSFColorPredefined.DARK_BLUE.getColor();
                break;
            case "INDIGO":
                hssfColor = HSSFColor.HSSFColorPredefined.INDIGO.getColor();
                break;
            case "GREY80PERCENT":
                hssfColor = HSSFColor.HSSFColorPredefined.GREY_80_PERCENT.getColor();
                break;
            case "ORANGE":
                hssfColor = HSSFColor.HSSFColorPredefined.ORANGE.getColor();
                break;
            case "DARKYELLOW":
                hssfColor = HSSFColor.HSSFColorPredefined.DARK_YELLOW.getColor();
                break;
            case "GREEN":
                hssfColor = HSSFColor.HSSFColorPredefined.GREEN.getColor();
                break;
            case "TEAL":
                hssfColor = HSSFColor.HSSFColorPredefined.TEAL.getColor();
                break;
            case "BLUE":
                hssfColor = HSSFColor.HSSFColorPredefined.BLUE.getColor();
                break;
            case "BLUEGREY":
                hssfColor = HSSFColor.HSSFColorPredefined.BLUE_GREY.getColor();
                break;
            case "GREY50PERCENT":
                hssfColor = HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.getColor();
                break;
            case "RED":
                hssfColor = HSSFColor.HSSFColorPredefined.RED.getColor();
                break;
            case "LIGHTORANGE":
                hssfColor = HSSFColor.HSSFColorPredefined.LIGHT_ORANGE.getColor();
                break;
            case "LIME":
                hssfColor = HSSFColor.HSSFColorPredefined.LIME.getColor();
                break;
            case "SEAGREEN":
                hssfColor = HSSFColor.HSSFColorPredefined.SEA_GREEN.getColor();
                break;
            case "AQUA":
                hssfColor = HSSFColor.HSSFColorPredefined.AQUA.getColor();
                break;
            case "LIGHTBLUE":
                hssfColor = HSSFColor.HSSFColorPredefined.LIGHT_BLUE.getColor();
                break;
            case "VIOLET":
                hssfColor = HSSFColor.HSSFColorPredefined.VIOLET.getColor();
                break;
            case "GREY40PERCENT":
                hssfColor = HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getColor();
                break;
            case "GOLD":
                hssfColor = HSSFColor.HSSFColorPredefined.GOLD.getColor();
                break;
            case "YELLOW":
                hssfColor = HSSFColor.HSSFColorPredefined.YELLOW.getColor();
                break;
            case "BRIGHTGREEN":
                hssfColor = HSSFColor.HSSFColorPredefined.BRIGHT_GREEN.getColor();
                break;
            case "TURQUOISE":
                hssfColor = HSSFColor.HSSFColorPredefined.TURQUOISE.getColor();
                break;
            case "DARKRED":
                hssfColor = HSSFColor.HSSFColorPredefined.DARK_RED.getColor();
                break;
            case "SKYBLUE":
                hssfColor = HSSFColor.HSSFColorPredefined.SKY_BLUE.getColor();
                break;
            case "PLUM":
                hssfColor = HSSFColor.HSSFColorPredefined.PLUM.getColor();
                break;
            case "GREY25PERCENT":
                hssfColor = HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getColor();
                break;
            case "ROSE":
                hssfColor = HSSFColor.HSSFColorPredefined.ROSE.getColor();
                break;
            case "LIGHTYELLOW":
                hssfColor = HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getColor();
                break;
            case "LIGHTGREEN":
                hssfColor = HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getColor();
                break;
            case "LIGHTTURQUOISE":
                hssfColor = HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE.getColor();
                break;
            case "PALEBLUE":
                hssfColor = HSSFColor.HSSFColorPredefined.PALE_BLUE.getColor();
                break;
            case "LAVENDER":
                hssfColor = HSSFColor.HSSFColorPredefined.LAVENDER.getColor();
                break;
            case "WHITE":
                hssfColor = HSSFColor.HSSFColorPredefined.WHITE.getColor();
                break;
            case "CORNFLOWERBLUE":
                hssfColor = HSSFColor.HSSFColorPredefined.CORNFLOWER_BLUE.getColor();
                break;
            case "LEMONCHIFFON":
                hssfColor = HSSFColor.HSSFColorPredefined.LEMON_CHIFFON.getColor();
                break;
            case "MAROON":
                hssfColor = HSSFColor.HSSFColorPredefined.MAROON.getColor();
                break;
            case "ORCHID":
                hssfColor = HSSFColor.HSSFColorPredefined.ORANGE.getColor();
                break;
            case "CORAL":
                hssfColor = HSSFColor.HSSFColorPredefined.CORAL.getColor();
                break;
            case "ROYALBLUE":
                hssfColor = HSSFColor.HSSFColorPredefined.ROYAL_BLUE.getColor();
                break;
            case "LIGHTCORNFLOWERBLUE":
                hssfColor = HSSFColor.HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE.getColor();
                break;
            case "TAN":
                hssfColor = HSSFColor.HSSFColorPredefined.TAN.getColor();
                break;
            default:
                hssfColor = null;
        }
        return hssfColor;
    }

    /**
     * parse color
     *
     * @param workBook work book
     * @param color    string color
     * @return HSSFColor
     */
    public static HSSFColor parseColor(HSSFWorkbook workBook, String color) {
        HSSFColor poiColor = null;
        if (StringUtils.isNotBlank(color)) {
            Color awtColor = getAwtColor(color);
            if (awtColor != null) {
                int r = awtColor.getRed();
                int g = awtColor.getGreen();
                int b = awtColor.getBlue();
                HSSFPalette palette = workBook.getCustomPalette();
                poiColor = palette.findColor((byte) r, (byte) g, (byte) b);
                if (poiColor == null) {
                    poiColor = palette.findSimilarColor(r, g, b);
                }
            }
        }
        return poiColor;
    }

    private static Color getAwtColor(String color) {
        HSSFColor hssfColor = getHssfColor(color);
        if (hssfColor != null) {
            short[] t = hssfColor.getTriplet();
            return new Color(t[0], t[1], t[2]);
        }
        String tmpColor = color.replace("_", "").toLowerCase();
        switch (tmpColor) {
            case "lightgray":
                return Color.LIGHT_GRAY;
            case "gray":
                return Color.GRAY;
            case "darkgray":
                return Color.DARK_GRAY;
            case "pink":
                return Color.PINK;
            case "magenta":
                return Color.MAGENTA;
            case "cyan":
                return Color.CYAN;
            default:
                return Color.decode(color);
        }
    }

    public static XSSFColor parseColor(String color) {
        XSSFColor poiColor = null;
        if (StringUtils.isNotBlank(color)) {
            Color awtColor = getAwtColor(color);
            if (awtColor != null) {
                poiColor = new XSSFColor(awtColor, null);
            }
        }
        return poiColor;
    }

    private static String convertColor(int r, int g, int b) {
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private static int calcColorValue(String color) {
        int rtn = 0;
        // matches 64 or 64%
        Matcher m = INT_AND_PER_PATTERN.matcher(color);
        if (m.matches()) {
            // % not found
            if (m.group(2) == null) {
                rtn = Math.round(Float.parseFloat(m.group(1))) % 256;
            } else {
                rtn = Math.round(Float.parseFloat(m.group(1)) * 255 / 100) % 256;
            }
        }
        return rtn;
    }
}
