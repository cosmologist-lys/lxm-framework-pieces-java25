package com.lxm.framework.mongo.core;

import com.lxm.framework.common.AppException;
import com.lxm.framework.mongo.entity.MongoBaseEntity;
import com.lxm.framework.mongo.entity.MongoRootEntity;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.List;

/**
 * @Author: Lys
 * @Date 2022/6/17
 * @Describe
 **/
public class MongoHelper {

    public static MongoTemplate mgt;

    /**
     * 获取集合名称
     *
     * @param entity 实体类
     * @return String
     */
    public static String getColName(Object entity) {
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.getCollectionName(ClassUtils.getUserClass(entity));
    }


    /**
     * 获取集合名称
     *
     * @param clz 实体类
     * @return String
     */
    public static String getColName(Class<?> clz) {
        checkMongoEntity(clz);
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.getCollectionName(clz);
    }

    /**
     * 获取list
     *
     * @param query 查询类
     * @param clz   实体类
     * @param col   集合名称
     * @param <T>   实体
     * @return List
     */
    public static <T> List<T> getList(Query query, Class<T> clz, String col) {
        if (query == null || clz == null || StringUtils.isBlank(col)) {
            throw new AppException(500, "mongo helper get list error, param cannot be null");
        }
        checkMongoEntity(clz);
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.find(query, clz, col);
    }

    /**
     * 获取总数
     *
     * @param query 查询类
     * @param col   集合名称
     * @return long
     */
    public static long getCount(Query query, String col) {
        if (query == null || StringUtils.isBlank(col)) {
            throw new AppException(500, "mongo helper get count error, param cannot be null");
        }
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.count(query, col);
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
    public static <T> T getById(Object id, Class<T> clz, String col) {
        if (id == null || clz == null || StringUtils.isBlank(col)) {
            throw new AppException(500, "mongo helper get by id error, param cannot be null");
        }
        checkMongoEntity(clz);
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.findById(id, clz, col);
    }

    /**
     * 保存实体
     *
     * @param t   实体参数
     * @param col 集合名称
     * @param <T> 实体
     * @return T
     */
    public static <T> T save(T t, String col) {
        if (t == null || StringUtils.isBlank(col)) {
            throw new AppException(500, "mongo helper save error, param cannot be null");
        }
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.insert(t, col);
    }

    /**
     * 批量插入
     *
     * @param objs 数据集合
     * @param col  集合名称
     * @param <T>  实体
     * @return Collection
     */
    public static <T> Collection<T> saveBatch(Collection<? extends T> objs, String col) {
        if (objs == null || StringUtils.isBlank(col)) {
            throw new AppException(500, "mongo helper batch save error, param cannot be null");
        }
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.insert(objs, col);
    }

    /**
     * 更新数据
     *
     * @param query  查询条件
     * @param update 更新类容
     * @param clz    更新类
     * @param col    集合名称
     * @param multi  是否更新所有
     * @return UpdateResult
     */
    public static UpdateResult edit(Query query, Update update, Class<?> clz, String col, boolean multi) {
        if (query == null || clz == null || StringUtils.isBlank(col)) {
            throw new AppException(500, "mongo helper edit error, param cannot be null");
        }
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        UpdateResult result;
        if (multi) {
            result = mgt.updateMulti(query, update, clz, col);
        } else {
            result = mgt.updateFirst(query, update, clz, col);
        }
        return result;
    }

    /**
     * 删除数据
     *
     * @param query 查询条件
     * @param col   集合名称
     * @return DeleteResult
     */
    public static DeleteResult remove(Query query, String col) {
        if (query == null || StringUtils.isBlank(col)) {
            throw new AppException(500, "mongo helper remove error, param cannot be null");
        }
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.remove(query, col);
    }

    /**
     * 获取主键属性名
     *
     * @param clz 实体类
     * @return String
     */
    public static String getPkName(Class<?> clz) {
        MongoPersistentEntity<?> persistentEntity = getPersistentEntity(clz);
        if (persistentEntity == null || persistentEntity.getIdProperty() == null) {
            throw new AppException(500, "class " + clz.getName() + " mongo pk name is null, please check annotation @MongoId");
        }
        return persistentEntity.getIdProperty().getName();
    }

    /**
     * 获取主键列名
     *
     * @param clz 实体类
     * @return String
     */
    public static String getPkColumnName(Class<?> clz) {
        MongoPersistentEntity<?> persistentEntity = getPersistentEntity(clz);
        if (persistentEntity == null || persistentEntity.getIdProperty() == null) {
            throw new AppException(500, "class " + clz.getName() + " mongo pk column name is null, please check annotation @MongoId");
        }
        return persistentEntity.getIdProperty().getFieldName();
    }

    /**
     * 转换Document类型
     *
     * @param t 实体
     * @return Document
     */
    public static Document convertToMongoType(Object t) {
        return (Document) mgt.getConverter().convertToMongoType(t);
    }

    /**
     * 获取mongodb实体
     *
     * @param clz 实体类
     * @return MongoPersistentEntity
     */
    public static MongoPersistentEntity<?> getPersistentEntity(Class<?> clz) {
        if (clz == null) {
            throw new AppException(500, "mongo helper get id name error, param cannot be null");
        }
        Assert.notNull(mgt, "mongodb template cannot be null, please check the configuration!");
        return mgt.getConverter().getMappingContext().getPersistentEntity(clz);
    }

    /**
     * 判断实体类是否为MongoEntity的子类
     *
     * @param clz 实体类
     */
    private static void checkMongoEntity(Class<?> clz) {
        if (clz == null || !MongoRootEntity.class.isAssignableFrom(clz))
            throw new AppException(500, "mongo helper get list error, the returned entity must be a subclass of MongoEntity");
    }
}
