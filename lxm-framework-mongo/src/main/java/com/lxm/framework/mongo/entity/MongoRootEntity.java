package com.lxm.framework.mongo.entity;

import com.lxm.framework.mongo.core.MongoHelper;

import java.io.Serializable;

/**
 * @Author: Lys
 * @Date 2022/6/17
 * @Describe
 **/
public class MongoRootEntity implements Serializable {
    private static final long serialVersionUID = 4434734316240245726L;

    /**
     * 插入数据
     */
    public void insert() {
        MongoHelper.save(this, MongoHelper.getColName(getClass()));
    }

    /*

     *//**
     * 根据主键id更新
     *
     * @param excludes 排除更新属性
     * @return UpdateResult
     *//*
    public UpdateResult updateById(String... excludes) {
        return MongoHelper.edit(Query.query(Criteria.where(MongoHelper.getPkName(getClass())).is(pkVal())),
                Update.fromDocument(MongoHelper.convertToMongoType(this), excludes),
                getClass(),
                MongoHelper.getColName(getClass()),
                false);
    }


    *//**
     * 根据主键id删除
     *//*
    public void deleteById() {
        Query query = new Query();
        query.addCriteria(Criteria.where(MongoHelper.getPkName(getClass())).is(pkVal()));
        MongoHelper.remove(query, MongoHelper.getColName(getClass()));
    }

    *//**
     * 获取主键的值
     *
     * @return Serializable
     *//*
    public Serializable pkVal() {
        return (Serializable) ReflectionKit.getFieldValue(this, MongoHelper.getPkName(getClass()));
    }

*/
}
