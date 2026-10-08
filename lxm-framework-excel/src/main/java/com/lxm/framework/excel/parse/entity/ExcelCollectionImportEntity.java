package com.lxm.framework.excel.parse.entity;

import lombok.Data;

import java.util.Map;

/**
 * Excel 对于的 Collection
 *
 * @author twenty2
 */
@Data
public class ExcelCollectionImportEntity {

    /**
     * 集合对应的名称
     */
    private String name;

    /**
     * 实体对象
     */
    private Class<?> type;

    /**
     * 这个list下面的参数集合实体对象
     */
    private Map<Integer, Object> importEntityMap;
}
