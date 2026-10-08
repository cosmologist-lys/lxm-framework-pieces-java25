package com.lxm.framework.mybatisplus.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.lxm.framework.common.principle.PrincipleProvider;
import com.lxm.framework.mybatisplus.parser.CustomizedSqlParser;
import com.lxm.framework.mybatisplus.util.InterceptUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import java.sql.Connection;
import java.sql.SQLException;

/** 查询先改写再统计/分页；不缓存包含租户身份的 SQL。 */
public class StatementInterceptor implements InnerInterceptor {
    private final PrincipleProvider principles;

    public StatementInterceptor() {
        this(() -> null);
    }

    public StatementInterceptor(PrincipleProvider principles) {
        this.principles = principles;
    }

    // 第三方 InnerInterceptor 的签名使用 raw ResultHandler，必须匹配该签名。
    public void beforeQuery(
            Executor executor,
            MappedStatement statement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql)
            throws SQLException {
        rewrite(executor.getTransaction().getConnection(), statement, boundSql);
    }

    public void beforePrepare(StatementHandler handler, Connection connection, Integer timeout) {
        var mapped = PluginUtils.mpStatementHandler(handler);
        if (mapped.mappedStatement().getSqlCommandType() != SqlCommandType.SELECT) {
            rewrite(connection, mapped.mappedStatement(), mapped.boundSql());
        }
    }

    private void rewrite(Connection connection, MappedStatement statement, BoundSql sql) {
        if (statement.getSqlCommandType() == SqlCommandType.INSERT
                || statement.getSqlCommandType() == SqlCommandType.UPDATE)
            InterceptUtils.fillIdentity(
                    sql.getParameterObject(),
                    principles.current(),
                    statement.getSqlCommandType() == SqlCommandType.INSERT);
        var parser =
                new CustomizedSqlParser(
                        InterceptUtils.escape(statement.getId()),
                        connection,
                        sql.getSql(),
                        principles.current());
        PluginUtils.mpBoundSql(sql)
                .sql(
                        parser.parser(
                                        sql.getSql(),
                                        index -> {
                                            var mapping = sql.getParameterMappings().get(index);
                                            String property = mapping.getProperty();
                                            if (sql.hasAdditionalParameter(property))
                                                return sql.getAdditionalParameter(property);
                                            Object object = sql.getParameterObject();
                                            if (object == null) return null;
                                            if (statement
                                                    .getConfiguration()
                                                    .getTypeHandlerRegistry()
                                                    .hasTypeHandler(object.getClass()))
                                                return object;
                                            var meta =
                                                    statement
                                                            .getConfiguration()
                                                            .newMetaObject(object);
                                            return meta.hasGetter(property)
                                                    ? meta.getValue(property)
                                                    : null;
                                        })
                                .getSql());
    }
}
