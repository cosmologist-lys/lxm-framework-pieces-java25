package com.lxm.framework.mybatisplus.pagenation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.lxm.framework.common.page.StandardPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author twenty2
 */
public class Pagination<T> implements IPage<T>, StandardPage<T> {
    private static final long serialVersionUID = 1L;
    /**
     * 查询数据列表
     */
    private List<T> records = Collections.emptyList();
    /**
     * 总数
     */
    private long total = 0;
    /**
     * 当前页
     */
    private long current = 1;
    /**
     * 每页显示条数，默认 10
     */
    private long size = 10;
    /**
     * <p>
     * SQL 排序 ASC 数组
     * </p>
     */
    private String[] ascs;
    /**
     * <p>
     * SQL 排序 DESC 数组
     * </p>
     */
    private String[] descs;
    /**
     * <p>
     * 自动优化 COUNT SQL
     * </p>
     */
    private boolean optimizeCountSql = true;
    /**
     * <p>
     * 是否进行 count 查询
     * </p>
     */
    private boolean searchCount = true;
    /**
     * <p>
     * 排序属性描述
     * </p>
     */
    private Map<String, String> orderProperty;

    public Pagination() {
    }

    /**
     * <p>
     * 分页构造函数
     * </p>
     *
     * @param current 当前页
     * @param size    每页显示条数
     */
    public Pagination(long current, long size) {
        this(current, size, 0);
    }

    public Pagination(long current, long size, long total) {
        this(current, size, total, true);
    }

    public Pagination(long current, long size, boolean isSearchCount) {
        this(current, size, 0, isSearchCount);
    }

    public Pagination(long current, long size, long total, boolean searchCount) {
        if (current > 1) {
            this.current = current;
        }
        this.size = size;
        this.total = total;
        this.searchCount = searchCount;
    }

    /**
     * <p>
     * 是否存在上一页
     * </p>
     *
     * @return true / false
     */
    public boolean hasPrevious() {
        return this.current > 1;
    }

    /**
     * <p>
     * 是否存在下一页
     * </p>
     *
     * @return true / false
     */
    public boolean hasNext() {
        return this.current < this.getPages();
    }

    @Override
    public List<T> getRecords() {
        return this.records;
    }

    @Override
    public Pagination<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    @Override
    public long getTotal() {
        return this.total;
    }

    @Override
    public Pagination<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public Pagination<T> setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public long getCurrent() {
        return this.current;
    }

    @Override
    public Pagination<T> setCurrent(long current) {
        this.current = current;
        return this;
    }

    public String[] getAscs() {
        return ascs;
    }

    public void setAscs(String[] ascs) {
        this.ascs = ascs;
    }

    /**
     * <p>
     * 升序
     * </p>
     *
     * @param ascs 多个升序字段
     */
    public void setAsc(String... ascs) {
        this.ascs = ascs;
    }

    public String[] getDescs() {
        return descs;
    }

    public void setDescs(String[] descs) {
        this.descs = descs;
    }

    /**
     * <p>
     * 降序
     * </p>
     *
     * @param descs 多个降序字段
     */
    public void setDesc(String... descs) {
        this.descs = descs;
    }

    @Override
    public boolean optimizeCountSql() {
        return optimizeCountSql;
    }

    public boolean isSearchCount() {
        if (total < 0) {
            return false;
        }
        return searchCount;
    }

    public Pagination<T> setSearchCount(boolean searchCount) {
        this.searchCount = searchCount;
        return this;
    }

    public Pagination<T> setOptimizeCountSql(boolean optimizeCountSql) {
        this.optimizeCountSql = optimizeCountSql;
        return this;
    }

    @Override
    public List<OrderItem> orders() {
        List<OrderItem> list = new ArrayList<>();
        if (ascs != null) {
            for (String a : ascs) {
                list.add(OrderItem.asc(orderProperty == null ? a : orderProperty.getOrDefault(a, a)));
            }
        }
        if (descs != null) {
            for (String d : descs) {
                list.add(OrderItem.desc(orderProperty == null ? d : orderProperty.getOrDefault(d, d)));
            }
        }
        return list;
    }

    public Map<String, String> getOrderProperty() {
        return orderProperty;
    }

    public Pagination<T> setOrderProperty(Map<String, String> orderProperty) {
        this.orderProperty = orderProperty;
        return this;
    }

    //===================================//

    @Override
    public void putRecords(List<T> records) {
        this.records = records;
    }

    @Override
    public void putTotal(long total) {
        this.total = total;
    }

    @Override
    public void putSize(long size) {
        this.size = size;
    }

    @Override
    public void putCurrent(long current) {
        this.current = current;
    }
}
