package com.lxm.framework.excel.entity;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 用于导出多个sheet
 *
 * @author Lys.
 * @date 2020/10/28 下午3:58
 */
public class ExcelMultiSheets {

    @Getter
    private final List<ExcelSingleSheet> sheetList;

    /**
     * 子sheet总个数，必须大于等于2个，不然做什么多sheet？
     */
    public boolean unqualified() {
        if (this.sheetList.size() < 2) {
            return true;
        }
        for (ExcelSingleSheet sheet : this.sheetList) {
            if (StringUtils.isEmpty(sheet.getSheetTitle()) || CollectionUtils.isEmpty(sheet.getDataList()) || null == sheet.getPojoClass()) {
                return true;
            }
        }
        return false;
    }

    public ExcelMultiSheets() {
        this.sheetList = new LinkedList<>();
    }

    public ExcelMultiSheets(ExcelSingleSheet... sheet) {
        this.sheetList = new LinkedList<>();
        this.sheetList.addAll(Arrays.asList(sheet));
    }

    public ExcelMultiSheets(String sheet1Name, Class<?> sheet1Class, List<?> sheet1List,
                            String sheet2Name, Class<?> sheet2Class, List<?> sheet2List) {
        this(new ExcelSingleSheet(sheet1Name, sheet1Class, sheet1List),
                new ExcelSingleSheet(sheet2Name, sheet2Class, sheet2List));
    }

    public ExcelMultiSheets(String sheet1Name, Class<?> sheet1Class, List<?> sheet1List,
                            String sheet2Name, Class<?> sheet2Class, List<?> sheet2List,
                            String sheet3Name, Class<?> sheet3Class, List<?> sheet3List) {
        this(new ExcelSingleSheet(sheet1Name, sheet1Class, sheet1List),
                new ExcelSingleSheet(sheet2Name, sheet2Class, sheet2List)
                , new ExcelSingleSheet(sheet3Name, sheet3Class, sheet3List));
    }

    public ExcelMultiSheets(String sheet1Name, Class<?> sheet1Class, List<?> sheet1List,
                            String sheet2Name, Class<?> sheet2Class, List<?> sheet2List,
                            String sheet3Name, Class<?> sheet3Class, List<?> sheet3List,
                            String sheet4Name, Class<?> sheet4Class, List<?> sheet4List) {
        this(new ExcelSingleSheet(sheet1Name, sheet1Class, sheet1List),
                new ExcelSingleSheet(sheet2Name, sheet2Class, sheet2List)
                , new ExcelSingleSheet(sheet3Name, sheet3Class, sheet3List),
                new ExcelSingleSheet(sheet4Name, sheet4Class, sheet4List));
    }

    public ExcelMultiSheets(String sheet1Name, Class<?> sheet1Class, List<?> sheet1List,
                            String sheet2Name, Class<?> sheet2Class, List<?> sheet2List,
                            String sheet3Name, Class<?> sheet3Class, List<?> sheet3List,
                            String sheet4Name, Class<?> sheet4Class, List<?> sheet4List,
                            String sheet5Name, Class<?> sheet5Class, List<?> sheet5List) {
        this(new ExcelSingleSheet(sheet1Name, sheet1Class, sheet1List),
                new ExcelSingleSheet(sheet2Name, sheet2Class, sheet2List),
                new ExcelSingleSheet(sheet3Name, sheet3Class, sheet3List),
                new ExcelSingleSheet(sheet4Name, sheet4Class, sheet4List),
                new ExcelSingleSheet(sheet5Name, sheet5Class, sheet5List));
    }

    public ExcelMultiSheets(String sheet1Name, Class<?> sheet1Class, List<?> sheet1List,
                            String sheet2Name, Class<?> sheet2Class, List<?> sheet2List,
                            String sheet3Name, Class<?> sheet3Class, List<?> sheet3List,
                            String sheet4Name, Class<?> sheet4Class, List<?> sheet4List,
                            String sheet5Name, Class<?> sheet5Class, List<?> sheet5List,
                            String sheet6Name, Class<?> sheet6Class, List<?> sheet6List) {
        this(new ExcelSingleSheet(sheet1Name, sheet1Class, sheet1List),
                new ExcelSingleSheet(sheet2Name, sheet2Class, sheet2List),
                new ExcelSingleSheet(sheet3Name, sheet3Class, sheet3List),
                new ExcelSingleSheet(sheet4Name, sheet4Class, sheet4List),
                new ExcelSingleSheet(sheet5Name, sheet5Class, sheet5List),
                new ExcelSingleSheet(sheet6Name, sheet6Class, sheet6List));
    }

    public ExcelMultiSheets(String sheet1Name, Class<?> sheet1Class, List<?> sheet1List,
                            String sheet2Name, Class<?> sheet2Class, List<?> sheet2List,
                            String sheet3Name, Class<?> sheet3Class, List<?> sheet3List,
                            String sheet4Name, Class<?> sheet4Class, List<?> sheet4List,
                            String sheet5Name, Class<?> sheet5Class, List<?> sheet5List,
                            String sheet6Name, Class<?> sheet6Class, List<?> sheet6List,
                            String sheet7Name, Class<?> sheet7Class, List<?> sheet7List) {
        this(new ExcelSingleSheet(sheet1Name, sheet1Class, sheet1List),
                new ExcelSingleSheet(sheet2Name, sheet2Class, sheet2List),
                new ExcelSingleSheet(sheet3Name, sheet3Class, sheet3List),
                new ExcelSingleSheet(sheet4Name, sheet4Class, sheet4List),
                new ExcelSingleSheet(sheet5Name, sheet5Class, sheet5List),
                new ExcelSingleSheet(sheet6Name, sheet6Class, sheet6List),
                new ExcelSingleSheet(sheet7Name, sheet7Class, sheet7List));
    }

    public ExcelMultiSheets(String sheet1Name, Class<?> sheet1Class, List<?> sheet1List,
                            String sheet2Name, Class<?> sheet2Class, List<?> sheet2List,
                            String sheet3Name, Class<?> sheet3Class, List<?> sheet3List,
                            String sheet4Name, Class<?> sheet4Class, List<?> sheet4List,
                            String sheet5Name, Class<?> sheet5Class, List<?> sheet5List,
                            String sheet6Name, Class<?> sheet6Class, List<?> sheet6List,
                            String sheet7Name, Class<?> sheet7Class, List<?> sheet7List,
                            String sheet8Name, Class<?> sheet8Class, List<?> sheet8List) {
        this(new ExcelSingleSheet(sheet1Name, sheet1Class, sheet1List),
                new ExcelSingleSheet(sheet2Name, sheet2Class, sheet2List),
                new ExcelSingleSheet(sheet3Name, sheet3Class, sheet3List),
                new ExcelSingleSheet(sheet4Name, sheet4Class, sheet4List),
                new ExcelSingleSheet(sheet5Name, sheet5Class, sheet5List),
                new ExcelSingleSheet(sheet6Name, sheet6Class, sheet6List),
                new ExcelSingleSheet(sheet7Name, sheet7Class, sheet7List),
                new ExcelSingleSheet(sheet8Name, sheet8Class, sheet8List));
    }


    public ExcelMultiSheets appendSheet(ExcelSingleSheet singleSheet) {
        this.sheetList.add(singleSheet);
        return this;
    }

    public ExcelMultiSheets appendSheet(String sheetName, Class<?> sheetClass, List<?> sheetList) {
        this.sheetList.add(new ExcelSingleSheet(sheetName, sheetClass, sheetList));
        return this;
    }
}
