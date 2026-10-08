package com.lxm.framework.mybatisplus.parser;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.lxm.framework.common.principle.StandardPrinciple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** 使用维护中的 AST 处理器改写 SQL。元数据或身份读取失败时禁止执行原 SQL。 */
public class CustomizedSqlParser {
    private final Connection connection;
    private final StandardPrinciple principle;
    private final boolean escape;
    private final Map<String, Integer> columns = new HashMap<>();

    public CustomizedSqlParser(boolean escape, Connection connection, String originalSql, StandardPrinciple principle) {
        this.escape = escape;
        this.connection = java.util.Objects.requireNonNull(connection, "connection");
        this.principle = principle;
    }

    public record SqlInfo(String sql) { public String getSql() { return sql; } }

    public SqlInfo parser(String sql) {
        return parser(sql, index -> { throw new IllegalStateException("Tenant INSERT requires bound parameters"); });
    }
    public SqlInfo parser(String sql, java.util.function.IntFunction<Object> parameter) {
        if (escape) return new SqlInfo(sql);
        var tenant = new TenantLineInnerInterceptor(new TenantLineHandler() {
            public Expression getTenantId() {
                if (principle == null || principle.getTenantId() < 0) {
                    throw new IllegalStateException("Tenant SQL requires a trusted PrincipleProvider");
                }
                return new LongValue(principle.getTenantId());
            }
            public boolean ignoreTable(String table) { return (columnFlags(table) & 1) == 0; }
        });
        var deleted = new TenantLineInnerInterceptor(new TenantLineHandler() {
            public Expression getTenantId() { return new LongValue(0); }
            public String getTenantIdColumn() { return "deleted"; }
            public boolean ignoreTable(String table) { return (columnFlags(table) & 2) == 0; }
        });
        var statement = parseStatement(sql);
        if (statement instanceof net.sf.jsqlparser.statement.insert.Insert insert) {
            if ((columnFlags(insert.getTable().getFullyQualifiedName()) & 1) != 0) validateInsert(insert,parameter);
            return new SqlInfo(sql);
        }
        if (statement instanceof net.sf.jsqlparser.statement.update.Update update) {
            if ((columnFlags(update.getTable().getFullyQualifiedName()) & 1) != 0)
                for (var set:update.getUpdateSets()) for (var column:set.getColumns())
                    if ("tenant_id".equalsIgnoreCase(column.getColumnName().replace("`",""))) throw new IllegalArgumentException("Tenant ownership cannot be changed by UPDATE");
        } else if (!(statement instanceof net.sf.jsqlparser.statement.select.Select) && !(statement instanceof net.sf.jsqlparser.statement.delete.Delete))
            throw new IllegalArgumentException("Unsupported protected SQL command");
        return new SqlInfo(deleted.parserSingle(tenant.parserSingle(sql, null), null));
    }

    private void validateInsert(net.sf.jsqlparser.statement.insert.Insert insert, java.util.function.IntFunction<Object> parameter) {
        if (principle==null || principle.getTenantId()<0) throw new IllegalStateException("Trusted tenant identity required");
        if (insert.getColumns()==null || !(insert.getSelect() instanceof net.sf.jsqlparser.statement.select.Values) || insert.getDuplicateUpdateSets()!=null || insert.getSetUpdateSets()!=null)
            throw new IllegalArgumentException("Tenant INSERT requires explicit columns and VALUES; upsert is unsupported");
        int tenantColumn=-1;
        for (int i=0;i<insert.getColumns().size();i++) if ("tenant_id".equalsIgnoreCase(insert.getColumns().get(i).getColumnName().replace("`",""))) tenantColumn=i;
        if (tenantColumn<0) throw new IllegalArgumentException("Tenant INSERT requires tenant_id");
        var values=insert.getValues().getExpressions();
        if (values.getFirst() instanceof net.sf.jsqlparser.expression.operators.relational.ExpressionList<?>) {
            for (var row:values) {
                if (!(row instanceof net.sf.jsqlparser.expression.operators.relational.ExpressionList<?> list)) throw new IllegalArgumentException("Unsupported INSERT row");
                validateTenantValue((Expression)list.get(tenantColumn),parameter);
            }
        } else validateTenantValue((Expression)values.get(tenantColumn),parameter);
    }
    private void validateTenantValue(Expression value, java.util.function.IntFunction<Object> parameter) {
        Object actual=value instanceof LongValue literal ? literal.getValue() : value instanceof net.sf.jsqlparser.expression.JdbcParameter jdbc && jdbc.getIndex()!=null ? parameter.apply(jdbc.getIndex()-1) : null;
        if (!(actual instanceof Number number) || number.longValue()!=principle.getTenantId()) throw new IllegalArgumentException("Inserted tenant does not match trusted identity");
    }

    private net.sf.jsqlparser.statement.Statement parseStatement(String sql) {
        try { return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(sql); }
        catch (net.sf.jsqlparser.JSQLParserException ex) { throw new IllegalArgumentException("Unsupported SQL", ex); }
    }

    private int columnFlags(String table) {
        return columns.computeIfAbsent(table, name -> {
            String clean = name.replace("`", "").replace("\"", "");
            String schema = null;
            int dot = clean.lastIndexOf('.');
            if (dot >= 0) { schema = clean.substring(0, dot); clean = clean.substring(dot + 1); }
            try {
                var metadata = connection.getMetaData();
                String pattern = clean.replace(metadata.getSearchStringEscape(), metadata.getSearchStringEscape() + metadata.getSearchStringEscape())
                    .replace("_", metadata.getSearchStringEscape() + "_").replace("%", metadata.getSearchStringEscape() + "%");
                int flags = 0;
                try (var result = metadata.getColumns(connection.getCatalog(), schema, pattern, null)) {
                    while (result.next()) {
                        String column = result.getString("COLUMN_NAME");
                        if ("tenant_id".equalsIgnoreCase(column)) flags |= 1;
                        if ("deleted".equalsIgnoreCase(column)) flags |= 2;
                    }
                }
                return flags;
            } catch (SQLException ex) { throw new IllegalStateException("Cannot inspect protected table metadata", ex); }
        });
    }
}
