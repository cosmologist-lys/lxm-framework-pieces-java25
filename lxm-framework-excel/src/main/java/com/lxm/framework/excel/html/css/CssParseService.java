package com.lxm.framework.excel.html.css;

import com.lxm.framework.excel.html.entity.HtmlCssConstant;
import com.lxm.framework.excel.html.entity.style.CellStyleBorderEntity;
import com.lxm.framework.excel.html.entity.style.CellStyleEntity;
import com.lxm.framework.excel.html.entity.style.CssStyleFontEnity;
import com.lxm.framework.excel.util.PoiCssUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把Css样式解析成对应的Model
 */
@Slf4j
public class CssParseService {

    private static final Pattern STYLE_PATTERN = Pattern.compile("(?:^|\\s+)(italic|oblique)(?:\\s+|$)");
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("(?:^|\\s+)(bold(?:er)?|[7-9]00)(?:\\s+|$)");

    @SuppressWarnings("serial")
    private final static Set<String> BORDER_STYLES = new HashSet<String>() {
        {
            // Specifies no border
            add(HtmlCssConstant.NONE);
            // The same as "none", except in border conflict resolution for table elements
            add(HtmlCssConstant.HIDDEN);
            // Specifies a dotted border
            add(HtmlCssConstant.DOTTED);
            // Specifies a dashed border
            add(HtmlCssConstant.DASHED);
            // Specifies a solid border
            add(HtmlCssConstant.SOLID);
            // Specifies a double border
            add(HtmlCssConstant.DOUBLE);
        }
    };

    public CellStyleEntity parseStyle(String style) {
        Map<String, String> mapStyle = new HashMap<String, String>();
        for (String s : style.split("\\s*;\\s*")) {
            if (StringUtils.isNotBlank(s)) {
                String[] ss = s.split("\\s*\\:\\s*");
                if (ss.length == 2 && StringUtils.isNotBlank(ss[0])
                        && StringUtils.isNotBlank(ss[1])) {
                    String attrName = ss[0].toLowerCase();
                    String attrValue = ss[1];
                    // do not change font name
                    if (!HtmlCssConstant.FONT.equals(attrName) && !HtmlCssConstant.FONT_FAMILY.equals(attrName)) {
                        attrValue = attrValue.toLowerCase();
                    }
                    mapStyle.put(attrName, attrValue);
                }
            }
        }

        parseFontAttr(mapStyle);

        parseBackground(mapStyle);

        parseBorder(mapStyle);

        return mapToCellStyleEntity(mapStyle);
    }

    private CellStyleEntity mapToCellStyleEntity(Map<String, String> mapStyle) {
        CellStyleEntity entity = new CellStyleEntity();
        entity.setAlign(mapStyle.get(HtmlCssConstant.TEXT_ALIGN));
        entity.setVetical(mapStyle.get(HtmlCssConstant.VETICAL_ALIGN));
        entity.setBackground(parseBackground(mapStyle));
        entity.setHeight(mapStyle.get(HtmlCssConstant.HEIGHT));
        entity.setWidth(mapStyle.get(HtmlCssConstant.WIDTH));
        entity.setFont(getCssStyleFontEnity(mapStyle));
        entity.setBackground(mapStyle.get(HtmlCssConstant.BACKGROUND_COLOR));
        entity.setBorder(getCssStyleBorderEntity(mapStyle));
        return entity;
    }

    private CellStyleBorderEntity getCssStyleBorderEntity(Map<String, String> mapStyle) {
        CellStyleBorderEntity border = new CellStyleBorderEntity();
        border.setBorderTopColor(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.TOP + "-" + HtmlCssConstant.COLOR));
        border.setBorderBottomColor(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.BOTTOM + "-" + HtmlCssConstant.COLOR));
        border.setBorderLeftColor(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.LEFT + "-" + HtmlCssConstant.COLOR));
        border.setBorderRightColor(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.RIGHT + "-" + HtmlCssConstant.COLOR));

        border.setBorderTopWidth(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.TOP + "-" + HtmlCssConstant.WIDTH));
        border.setBorderBottomWidth(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.BOTTOM + "-" + HtmlCssConstant.WIDTH));
        border.setBorderLeftWidth(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.LEFT + "-" + HtmlCssConstant.WIDTH));
        border.setBorderRightWidth(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.RIGHT + "-" + HtmlCssConstant.WIDTH));

        border.setBorderTopStyle(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.TOP + "-" + HtmlCssConstant.STYLE));
        border.setBorderBottomStyle(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.BOTTOM + "-" + HtmlCssConstant.STYLE));
        border.setBorderLeftStyle(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.LEFT + "-" + HtmlCssConstant.STYLE));
        border.setBorderRightStyle(mapStyle.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.RIGHT + "-" + HtmlCssConstant.STYLE));
        return border;
    }

    private CssStyleFontEnity getCssStyleFontEnity(Map<String, String> style) {
        CssStyleFontEnity font = new CssStyleFontEnity();
        font.setStyle(style.get(HtmlCssConstant.FONT_STYLE));
        int fontSize = PoiCssUtils.getInt(style.get(HtmlCssConstant.FONT_SIZE));
        if (fontSize > 0) {
            font.setSize(fontSize);
        }
        font.setWeight(style.get(HtmlCssConstant.FONT_WEIGHT));
        font.setFamily(style.get(HtmlCssConstant.FONT_FAMILY));
        font.setDecoration(style.get(HtmlCssConstant.TEXT_DECORATION));
        font.setColor(style.get(HtmlCssConstant.COLOR));
        return font;
    }

    public void parseBorder(Map<String, String> style) {
        for (String pos : new String[]{null, HtmlCssConstant.TOP, HtmlCssConstant.RIGHT, HtmlCssConstant.BOTTOM, HtmlCssConstant.LEFT}) {
            // border[-attr]
            if (pos == null) {
                setBorderAttr(style, pos, style.get(HtmlCssConstant.BORDER));
                setBorderAttr(style, pos, style.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.COLOR));
                setBorderAttr(style, pos, style.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.WIDTH));
                setBorderAttr(style, pos, style.get(HtmlCssConstant.BORDER + "-" + HtmlCssConstant.STYLE));
            }
            // border-pos[-attr]
            else {
                setBorderAttr(style, pos, style.get(HtmlCssConstant.BORDER + "-" + pos));
                for (String attr : new String[]{HtmlCssConstant.COLOR, HtmlCssConstant.WIDTH, HtmlCssConstant.STYLE}) {
                    String attrName = HtmlCssConstant.BORDER + "-" + pos + "-" + attr;
                    String attrValue = style.get(attrName);
                    if (StringUtils.isNotBlank(attrValue)) {
                        style.put(attrName, attrValue);
                    }
                }
            }
        }
    }

    private void setBorderAttr(Map<String, String> mapBorder, String pos, String value) {
        if (StringUtils.isNotBlank(value)) {
            String borderColor = null;
            for (String borderAttr : value.split("\\s+")) {
                if ((borderColor = PoiCssUtils.processColor(borderAttr)) != null) {
                    setBorderAttr(mapBorder, pos, HtmlCssConstant.COLOR, borderColor);
                } else if (PoiCssUtils.isNum(borderAttr)) {
                    setBorderAttr(mapBorder, pos, HtmlCssConstant.WIDTH, borderAttr);
                } else if (BORDER_STYLES.contains(borderAttr)) {
                    setBorderAttr(mapBorder, pos, HtmlCssConstant.STYLE, borderAttr);
                } else {
                    log.debug("Border Attr [{}] Is Not Suppoted.", borderAttr);
                }
            }
        }
    }

    private void setBorderAttr(Map<String, String> mapBorder, String pos, String attr,
                               String value) {
        if (StringUtils.isNotBlank(pos)) {
            mapBorder.put(HtmlCssConstant.BORDER + "-" + pos + "-" + attr, value);
        } else {
            for (String name : new String[]{HtmlCssConstant.TOP, HtmlCssConstant.RIGHT, HtmlCssConstant.BOTTOM, HtmlCssConstant.LEFT}) {
                mapBorder.put(HtmlCssConstant.BORDER + "-" + name + "-" + attr, value);
            }
        }
    }

    private void parseFontAttr(Map<String, String> mapRtn) {

        log.debug("Parse Font Style.");
        // color
        String color = PoiCssUtils.processColor(mapRtn.get(HtmlCssConstant.COLOR));
        if (StringUtils.isNotBlank(color)) {
            log.debug("Text Color [{}] Found.", color);
            mapRtn.put(HtmlCssConstant.COLOR, color);
        }
        // font
        String font = mapRtn.get(HtmlCssConstant.FONT);
        if (StringUtils.isNotBlank(font)
                && !ArrayUtils.contains(new String[]{"small-caps", "caption", "icon", "menu",
                        "message-box", "small-caption", "status-bar"},
                font)) {
            log.debug("Parse Font Attr [{}].", font);
            String[] ignoreStyles = new String[]{"normal",
                    // font weight normal
                    "[1-3]00"};
            StringBuffer sbFont = new StringBuffer(
                    font.replaceAll("^|\\s*" + StringUtils.join(ignoreStyles, "|") + "\\s+|$", " "));
            log.debug("Font Attr [{}] After Process Ingore.", sbFont);
            // style
            Matcher m = STYLE_PATTERN.matcher(sbFont.toString());
            if (m.find()) {
                sbFont.setLength(0);
                if (log.isDebugEnabled()) {
                    log.debug("Font Style [{}] Found.", m.group(1));
                }
                mapRtn.put(HtmlCssConstant.FONT_STYLE, HtmlCssConstant.ITALIC);
                m.appendReplacement(sbFont, " ");
                m.appendTail(sbFont);
            }
            // weight
            m = WEIGHT_PATTERN.matcher(sbFont.toString());
            if (m.find()) {
                sbFont.setLength(0);
                if (log.isDebugEnabled()) {
                    log.debug("Font Weight [{}](bold) Found.", m.group(1));
                }
                mapRtn.put(HtmlCssConstant.FONT_WEIGHT, HtmlCssConstant.BOLD);
                m.appendReplacement(sbFont, " ");
                m.appendTail(sbFont);
            }
            // size xx-small | x-small | small | medium | large | x-large | xx-large | 18px [/2]
            m = Pattern.compile(
                    // before blank or start
                    new StringBuilder("(?:^|\\s+)")
                            // font size
                            .append("(xx-small|x-small|small|medium|large|x-large|xx-large|").append("(?:")
                            .append(HtmlCssConstant.PATTERN_LENGTH).append("))")
                            // line height
                            .append("(?:\\s*\\/\\s*(").append(HtmlCssConstant.PATTERN_LENGTH).append("))?")
                            // after blank or end
                            .append("(?:\\s+|$)").toString())
                    .matcher(sbFont.toString());
            if (m.find()) {
                sbFont.setLength(0);
                log.debug("Font Size[/line-height] [{}] Found.", m.group());
                String fontSize = m.group(1);
                if (StringUtils.isNotBlank(fontSize)) {
                    fontSize = StringUtils.deleteWhitespace(fontSize);
                    log.debug("Font Size [{}].", fontSize);
                    if (fontSize.matches(HtmlCssConstant.PATTERN_LENGTH)) {
                        mapRtn.put(HtmlCssConstant.FONT_SIZE, fontSize);
                    } else {
                        log.debug("Font Size [{}] Not Supported, Ignore.", fontSize);
                    }
                }
                String lineHeight = m.group(2);
                if (StringUtils.isNotBlank(lineHeight)) {
                    log.debug("Line Height [{}] Not Supported, Ignore.", lineHeight);
                }
                m.appendReplacement(sbFont, " ");
                m.appendTail(sbFont);
            }
            // font family
            if (sbFont.length() > 0) {
                log.debug("Font Families [{}].", sbFont);
                // trim & remove '"
                String fontFamily = sbFont.toString().split("\\s*,\\s*")[0].trim()
                        .replaceAll("'|\"", "");
                log.debug("Use First Font Family [{}].", fontFamily);
                mapRtn.put(HtmlCssConstant.FONT_FAMILY, fontFamily);
            }
        }
        font = mapRtn.get(HtmlCssConstant.FONT_STYLE);
        if (ArrayUtils.contains(new String[]{HtmlCssConstant.ITALIC, "oblique"}, font)) {
            log.debug("Font Italic [{}] Found.", font);
            mapRtn.put(HtmlCssConstant.FONT_STYLE, HtmlCssConstant.ITALIC);
        }
        font = mapRtn.get(HtmlCssConstant.FONT_WEIGHT);
        if (StringUtils.isNotBlank(font) && Pattern.matches("^bold(?:er)?|[7-9]00$", font)) {
            log.debug("Font Weight [{}](bold) Found.", font);
            mapRtn.put(HtmlCssConstant.FONT_WEIGHT, HtmlCssConstant.BOLD);
        }
        font = mapRtn.get(HtmlCssConstant.FONT_SIZE);
        if (!PoiCssUtils.isNum(font)) {
            log.debug("Font Size [{}] Error.", font);
            mapRtn.remove(HtmlCssConstant.FONT_SIZE);
        }
    }

    private String parseBackground(Map<String, String> style) {
        String bg = style.get(HtmlCssConstant.BACKGROUND);
        String bgColor = null;
        if (StringUtils.isNotBlank(bg)) {
            for (String bgAttr : bg.split("(?<=\\)|\\w|%)\\s+(?=\\w)")) {
                if ((bgColor = PoiCssUtils.processColor(bgAttr)) != null) {
                    style.put(HtmlCssConstant.BACKGROUND_COLOR, bgColor);
                    break;
                }
            }
        }
        bg = style.get(HtmlCssConstant.BACKGROUND_COLOR);
        if (StringUtils.isNotBlank(bg) && (bgColor = PoiCssUtils.processColor(bg)) != null) {
            style.put(HtmlCssConstant.BACKGROUND_COLOR, bgColor);

        }
        if (bgColor != null) {
            bgColor = style.get(HtmlCssConstant.BACKGROUND_COLOR);
            if ("#ffffff".equals(bgColor)) {
                style.remove(HtmlCssConstant.BACKGROUND_COLOR);
            }
        }
        return null;
    }

}
