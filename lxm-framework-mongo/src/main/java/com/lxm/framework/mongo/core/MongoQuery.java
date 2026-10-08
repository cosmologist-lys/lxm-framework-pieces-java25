package com.lxm.framework.mongo.core;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: Lys
 * @Date 2022/6/17
 * @Describe
 **/
public class MongoQuery {

    @Getter
    private final Query query = new Query();

    /**
     * 等于
     *
     * @param column 列名
     * @param val    值
     * @return MongoQuery
     */
    public MongoQuery eq(String column, Object val) {
        query.addCriteria(Criteria.where(column).is(val));
        return this;
    }

    /**
     * 不等于
     *
     * @param column 列名
     * @param val    值集
     * @return MongoQuery
     */
    public MongoQuery ne(String column, Object val) {
        query.addCriteria(Criteria.where(column).ne(val));
        return this;
    }

    /**
     * 模糊查询
     *
     * @param column 列名
     * @param val    值
     * @return MongoQuery
     */
    public MongoQuery like(String column, String val) {
        if (StringUtils.isNotBlank(column) && StringUtils.isNotBlank(val)) {
            Pattern pattern = Pattern.compile("^.*" + Pattern.quote(val) + ".*$", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where(column).regex(pattern));
        }
        return this;
    }

    /**
     * 包含
     *
     * @param column 列名
     * @param vals   值集合
     * @return MongoQuery
     */
    public MongoQuery in(String column, Object... vals) {
        query.addCriteria(Criteria.where(column).in(vals));
        return this;
    }

    /**
     * 不包含
     *
     * @param column 列名
     * @param vals   值集合
     * @return MongoQuery
     */
    public MongoQuery notin(String column, Object... vals) {
        query.addCriteria(Criteria.where(column).nin(vals));
        return this;
    }

    /**
     * 大于
     *
     * @param column 列名
     * @param val    值集
     * @return MongoQuery
     */
    public MongoQuery gt(String column, Object val) {
        query.addCriteria(Criteria.where(column).gt(val));
        return this;
    }

    /**
     * 小于
     *
     * @param column 列名
     * @param val    值集
     * @return MongoQuery
     */
    public MongoQuery lt(String column, Object val) {
        query.addCriteria(Criteria.where(column).lt(val));
        return this;
    }

    /**
     * 大于等于
     *
     * @param column 列名
     * @param val    值集
     * @return MongoQuery
     */
    public MongoQuery gte(String column, Object val) {
        query.addCriteria(Criteria.where(column).gte(val));
        return this;
    }

    /**
     * 小于等于
     *
     * @param column 列名
     * @param val    值集
     * @return MongoQuery
     */
    public MongoQuery lte(String column, Object val) {
        query.addCriteria(Criteria.where(column).lte(val));
        return this;
    }

    /**
     * 范围查询
     *
     * @param column 列名
     * @param val1   值1
     * @param val2   值2
     * @return MongoQuery
     */
    public MongoQuery between(String column, Object val1, Object val2) {
        query.addCriteria(Criteria.where(column).gte(val1).lte(val2));
        return this;
    }

    /**
     * 时间范围查询
     *
     * @param column 列名
     * @param val1   值1
     * @param val2   值2
     * @return MongoQuery
     */
    public MongoQuery betweenDateTime(String column, Object val1, Object val2) {
        query.addCriteria(Criteria.where(column).gte(val1).lte(val2));
        return this;
    }


    /**
     * 正序排序
     *
     * @param columns 列名集合
     * @return MongoQuery
     */
    public MongoQuery orderByAsc(String... columns) {
        List<Sort.Order> list = new ArrayList<>();
        for (String c : columns) {
            list.add(Sort.Order.asc(c));
        }
        query.with(Sort.by(list));
        return this;
    }

    /**
     * 倒序排序
     *
     * @param columns 列名集合
     * @return MongoQuery
     */
    public MongoQuery orderByDesc(String... columns) {
        List<Sort.Order> list = new ArrayList<>();
        for (String c : columns) {
            list.add(Sort.Order.desc(c));
        }
        query.with(Sort.by(list));
        return this;
    }
}
