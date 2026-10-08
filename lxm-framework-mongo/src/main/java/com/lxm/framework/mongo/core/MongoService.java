package com.lxm.framework.mongo.core;

import com.lxm.framework.common.AppException;
import com.lxm.framework.common.page.StandardPage;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;

/**
 * @Author: Lys
 * @Date 2022/6/17
 * @Describe
 **/
public class MongoService {

    /**
     * 查询集合
     *
     * @param mongoQuery 查询参数
     * @param clz        实体类
     * @param <T>        实体
     * @return pagination
     */
    public <T> List<T> getList(MongoQuery mongoQuery, Class<T> clz) {
        return getList(mongoQuery, clz, MongoHelper.getColName(clz));
    }

    /**
     * 查询集合
     *
     * @param mongoQuery 查询参数
     * @param clz        实体类
     * @param col        集合名称
     * @param <T>        实体
     * @return pagination
     */
    public <T> List<T> getList(MongoQuery mongoQuery, Class<T> clz, String col) {
        if (mongoQuery == null) {
            throw new AppException(500, "mongo service get list error, param cannot be null");
        }
        return MongoHelper.getList(mongoQuery.getQuery(), clz, col);
    }

    /**
     * 查询分页集合
     *
     * @param pagination 分页参数
     * @param mongoQuery 查询参数
     * @param clz        实体类
     * @param <T>        实体
     * @return pagination
     */
    public <T> StandardPage<T> getPage(StandardPage<T> pagination, MongoQuery mongoQuery, Class<T> clz) {
        return getPage(pagination, mongoQuery, clz, MongoHelper.getColName(clz));
    }

    /**
     * 查询分页集合
     *
     * @param pagination 分页参数
     * @param mongoQuery 查询参数
     * @param clz        实体类
     * @param col        集合名称
     * @param <T>        实体
     * @return pagination
     */
    public <T> StandardPage<T> getPage(StandardPage<T> pagination, MongoQuery mongoQuery, Class<T> clz, String col) {
        if (pagination == null || mongoQuery == null) {
            throw new AppException(500, "mongo service get page error, param cannot be null");
        }
        if (pagination.getCurrent() < 1 || pagination.getSize() < 1 || pagination.getSize() > 1000)
            throw new IllegalArgumentException("Mongo page current must be positive and size must be 1..1000");
        Query query = Query.of(mongoQuery.getQuery());
        long count = MongoHelper.getCount(Query.of(query).skip(0).limit(0), col);
        pagination.putTotal(count);
        // 排序
        if (pagination.getAscs() != null && pagination.getAscs().length > 0) {
            query.with(org.springframework.data.domain.Sort.by(pagination.getAscs()).ascending());
        }
        if (pagination.getDescs() != null && pagination.getDescs().length > 0) {
            query.with(org.springframework.data.domain.Sort.by(pagination.getDescs()).descending());
        }
        query.skip(Math.multiplyExact(pagination.getCurrent() - 1, pagination.getSize()));
        query.limit(Long.valueOf(pagination.getSize()).intValue());
        pagination.putRecords(MongoHelper.getList(query, clz, col));
        return pagination;
    }

    /**
     * 根据id获取数据，id为entity被注解@MongoId标记
     * 集合名由实体@Document注释
     *
     * @param id  主键id
     * @param clz 返回类
     * @param <T> 实体
     * @return T
     */
    public <T> T getById(Object id, Class<T> clz) {
        return getById(id, clz, MongoHelper.getColName(clz));
    }

    /**
     * 根据id获取数据，id为entity被注解@MongoId标记
     *
     * @param id  主键id
     * @param clz 返回类
     * @param col 集合名称
     * @param <T> 实体
     * @return T
     */
    public <T> T getById(Object id, Class<T> clz, String col) {
        return MongoHelper.getById(id, clz, col);
    }

    /**
     * 保存实体
     *
     * @param t   实体参数
     * @param <T> 实体
     * @return T
     */
    public <T> T save(T t) {
        return save(t, MongoHelper.getColName(t));
    }

    /**
     * 保存实体
     *
     * @param t   实体参数
     * @param col 集合名称
     * @param <T> 实体
     * @return T
     */
    public <T> T save(T t, String col) {
        return MongoHelper.save(t, col);
    }

    /**
     * 批量插入
     *
     * @param objs 数据集合
     * @param clz  实体类
     * @param <T>  实体
     * @return Collection
     */
    public <T> Collection<T> saveBatch(Collection<? extends T> objs, Class<T> clz) {
        return saveBatch(objs, MongoHelper.getColName(clz));
    }

    /**
     * 批量插入
     *
     * @param objs 数据集合
     * @param col  集合名称
     * @param <T>  实体
     * @return Collection
     */
    public <T> Collection<T> saveBatch(Collection<? extends T> objs, String col) {
        return MongoHelper.saveBatch(objs, col);
    }

    /**
     * 更新所有数据
     *
     * @param mongoQuery 查询条件
     * @param update     更新类容
     * @param clz        更新类
     * @return UpdateResult
     */
    public UpdateResult editMulti(MongoQuery mongoQuery, Update update, Class<?> clz) {
        return edit(mongoQuery, update, clz, MongoHelper.getColName(clz), true);
    }

    /**
     * 更新第一条数据
     *
     * @param mongoQuery 查询条件
     * @param update     更新类容
     * @param clz        更新类
     * @return UpdateResult
     */
    public UpdateResult edit(MongoQuery mongoQuery, Update update, Class<?> clz) {
        return edit(mongoQuery, update, clz, MongoHelper.getColName(clz), false);
    }

    /**
     * 更新数据
     *
     * @param mongoQuery 查询条件
     * @param update     更新类容
     * @param clz        更新类
     * @param col        集合名称
     * @param multi      是否更新所有
     * @return UpdateResult
     */
    public UpdateResult edit(MongoQuery mongoQuery, Update update, Class<?> clz, String col, boolean multi) {
        if (mongoQuery == null) {
            throw new AppException(500, "mongo service edit error, param cannot be null");
        }
        return MongoHelper.edit(mongoQuery.getQuery(), update, clz, col, multi);
    }

    /**
     * 删除数据
     *
     * @param mongoQuery 查询条件
     * @param clz        实体类
     * @return DeleteResult
     */
    public DeleteResult remove(MongoQuery mongoQuery, Class<?> clz) {
        return remove(mongoQuery, MongoHelper.getColName(clz));
    }

    /**
     * 删除数据
     *
     * @param mongoQuery 查询条件
     * @param col        集合名称
     * @return DeleteResult
     */
    public DeleteResult remove(MongoQuery mongoQuery, String col) {
        if (mongoQuery == null) {
            throw new AppException(500, "mongo service remove error, param cannot be null");
        }
        return MongoHelper.remove(mongoQuery.getQuery(), col);
    }
}
