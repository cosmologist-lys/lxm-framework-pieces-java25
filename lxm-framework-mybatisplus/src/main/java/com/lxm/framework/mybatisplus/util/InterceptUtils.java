package com.lxm.framework.mybatisplus.util;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lxm.framework.mybatisplus.common.SqlConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author: Lys
 * @Date 2022/2/25
 * @Describe
 **/
public class InterceptUtils {

    public static void setFieldTenantId(BoundSql boundSql, MetaObject metaObject) {
        var tid =
                metaObject.hasGetter(SqlConstant.TID) ? metaObject.getValue(SqlConstant.TID) : null;
        if (Objects.isNull(tid)) {
            throw new IllegalStateException(
                    "Tenant identity must be supplied by PrincipleProvider");
        } else {
            setFieldValueByName(boundSql, SqlConstant.TID, tid, metaObject);
        }
    }

    public static void setFieldUpdatedBy(BoundSql boundSql, MetaObject metaObject) {
        var byId =
                metaObject.hasGetter(SqlConstant.UPDATED_BY_ID)
                        ? metaObject.getValue(SqlConstant.UPDATED_BY_ID)
                        : null;
        if (Objects.isNull(byId)) {
            throw new IllegalStateException("User identity must be supplied by PrincipleProvider");
        } else {
            setFieldValueByName(boundSql, SqlConstant.UPDATED_BY_ID, byId, metaObject);
        }
    }

    public static void setFieldCreatedBy(BoundSql boundSql, MetaObject metaObject) {
        var byId =
                metaObject.hasGetter(SqlConstant.CREATED_BY_ID)
                        ? metaObject.getValue(SqlConstant.CREATED_BY_ID)
                        : null;
        if (Objects.isNull(byId)) {
            throw new IllegalStateException("User identity must be supplied by PrincipleProvider");
        } else {
            setFieldValueByName(boundSql, SqlConstant.CREATED_BY_ID, byId, metaObject);
        }
    }

    public static void setFieldUpdatedTime(
            BoundSql boundSql, MetaObject metaObject, LocalDateTime time) {
        setFieldValueByName(boundSql, SqlConstant.UPDATED_AT, time, metaObject);
    }

    public static void setFieldCreatedTime(
            BoundSql boundSql, MetaObject metaObject, LocalDateTime time) {
        var cat =
                metaObject.hasGetter(SqlConstant.CREATED_AT)
                        ? metaObject.getValue(SqlConstant.CREATED_AT)
                        : null;
        if (Objects.isNull(cat)) {
            setFieldValueByName(boundSql, SqlConstant.CREATED_AT, time, metaObject);
        } else {
            setFieldValueByName(boundSql, SqlConstant.CREATED_AT, cat, metaObject);
        }
    }

    public static void setFieldDeletedTime(
            BoundSql boundSql, MetaObject metaObject, LocalDateTime time) {
        Object finalTime = Objects.isNull(time) ? LocalDateTime.of(1900, 1, 1, 0, 0) : time;
        setFieldValueByName(boundSql, SqlConstant.DELETED_TIME, finalTime, metaObject);
    }

    public static void setFieldDeleted(BoundSql boundSql, MetaObject metaObject, Boolean flag) {
        final boolean mark = !Objects.isNull(flag) && flag;
        setFieldValueByName(boundSql, SqlConstant.DELETED, mark, metaObject);
    }

    public static boolean isDeleteMapper(String statementId) {
        String pureStatementId = StringUtils.substringAfterLast(statementId, ".");
        return pureStatementId.startsWith(SqlConstant.FLAG_DELETE)
                || pureStatementId.startsWith(SqlConstant.FLAG_DELETE_SHORT)
                || pureStatementId.startsWith(SqlConstant.FLAG_REMOVE);
    }

    public static boolean escape(String statementId) {
        return StringUtils.endsWith(statementId, SqlConstant.FLAG_ESCAPE);
    }

    private static void setFieldValueByName(
            BoundSql boundSql, String fieldName, Object fieldVal, MetaObject metaObject) {
        if (Objects.isNull(fieldVal)) {
            return;
        }
        boundSql.setAdditionalParameter(fieldName, fieldVal);
        if (metaObject.hasSetter(fieldName) && metaObject.hasGetter(fieldName)) {
            setFieldValue(fieldName, fieldVal, metaObject);
        } else if (metaObject.hasGetter(Constants.ENTITY)) {
            var et = metaObject.getValue(Constants.ENTITY);
            if (Objects.nonNull(et)) {
                var etMeta = SystemMetaObject.forObject(et);
                if (etMeta.hasSetter(fieldName)) {
                    setFieldValue(fieldName, fieldVal, etMeta);
                }
            }
        }
    }

    private static void setFieldValue(String fieldName, Object fieldVal, MetaObject metaObject) {
        if (SqlConstant.TID.equals(fieldName)
                && metaObject.getValue(fieldName) != null
                && ((Number) metaObject.getValue(fieldName)).longValue() >= 0) {
            return;
        }
        metaObject.setValue(fieldName, fieldVal);
    }

    public static void fillIdentity(
            Object parameter,
            com.lxm.framework.common.principle.StandardPrinciple principle,
            boolean insert) {
        if (parameter == null) return;
        if (parameter instanceof java.util.Collection<?> items) {
            items.forEach(item -> fillIdentity(item, principle, insert));
            return;
        }
        if (parameter instanceof java.util.Map<?, ?> map) {
            for (String key : java.util.List.of("et", "list", "collection")) {
                if (map.containsKey(key)) {
                    fillIdentity(map.get(key), principle, insert);
                    return;
                }
            }
        }
        var target = SystemMetaObject.forObject(parameter);
        if (target.hasSetter(SqlConstant.TID)) {
            if (principle == null || principle.getTenantId() < 0)
                throw new IllegalStateException("Trusted tenant identity required");
            Object existing =
                    target.hasGetter(SqlConstant.TID) ? target.getValue(SqlConstant.TID) : null;
            if (existing instanceof Number number
                    && number.longValue() >= 0
                    && number.longValue() != principle.getTenantId()) {
                throw new IllegalArgumentException("Entity tenant does not match current identity");
            }
            target.setValue(SqlConstant.TID, principle.getTenantId());
        }
        for (String field :
                insert
                        ? java.util.List.of(SqlConstant.CREATED_BY_ID, SqlConstant.UPDATED_BY_ID)
                        : java.util.List.of(SqlConstant.UPDATED_BY_ID)) {
            if (target.hasSetter(field)) {
                if (principle == null || principle.getUserId() < 0)
                    throw new IllegalStateException("Trusted user identity required");
                target.setValue(field, principle.getUserId());
            }
        }
    }

    public static String genKey(String statementId, String originalSql) {
        return statementId + "_" + originalSql;
    }
}
