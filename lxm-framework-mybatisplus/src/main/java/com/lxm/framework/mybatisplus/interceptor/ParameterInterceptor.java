package com.lxm.framework.mybatisplus.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.lxm.framework.mybatisplus.util.InterceptUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * @Author: Lys
 * @Date 2022/2/25
 * @Describe
 **/
@Slf4j
@Intercepts({@Signature(type = ParameterHandler.class, method = "setParameters", args = PreparedStatement.class)})
public class ParameterInterceptor extends AbstractInterceptor implements Interceptor {


    private final com.lxm.framework.common.principle.PrincipleProvider principles;
    public ParameterInterceptor() { this(() -> null); }
    public ParameterInterceptor(com.lxm.framework.common.principle.PrincipleProvider principles) { this.principles = principles; }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invoke(invocation);
    }

    @Override
    Object invoke(Invocation invocation) throws Exception {
        ParameterHandler parameterHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(parameterHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
            insertOrUpdate(metaObject, mappedStatement);
        }
        return invocation.proceed();
    }

    private void insertOrUpdate(MetaObject metaObject, MappedStatement mappedStatement) {
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        if (boundSql.getParameterObject() == null) return;
        MetaObject pm = SystemMetaObject.forObject(boundSql.getParameterObject());
        InterceptUtils.fillIdentity(boundSql.getParameterObject(), principles.current(), SqlCommandType.INSERT.equals(mappedStatement.getSqlCommandType()));
        // 打了标记的，统统跳过
        if (InterceptUtils.escape(mappedStatement.getId())) {
            return;
        }
        final var now = LocalDateTime.now();
        // 处理更新时间和人
        InterceptUtils.setFieldUpdatedTime(boundSql, pm, now);

        // 处理创建时间和默认删除符
        if (SqlCommandType.INSERT.equals(mappedStatement.getSqlCommandType())) {
            InterceptUtils.setFieldDeleted(boundSql, pm, false);
            InterceptUtils.setFieldDeletedTime(boundSql, pm, null);
            InterceptUtils.setFieldCreatedTime(boundSql, pm, now);


        }
        // 处理删除时间
        if (InterceptUtils.isDeleteMapper(mappedStatement.getId())) {
            InterceptUtils.setFieldDeletedTime(boundSql, pm, null);
            InterceptUtils.setFieldDeleted(boundSql, pm, true);
        }
        log.debug("lxm parameter-interceptor have processed parameters , boundSql : {}", boundSql.getSql());
    }
}
