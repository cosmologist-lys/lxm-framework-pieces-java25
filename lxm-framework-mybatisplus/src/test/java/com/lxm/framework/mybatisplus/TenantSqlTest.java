package com.lxm.framework.mybatisplus;

import com.lxm.framework.mybatisplus.parser.CustomizedSqlParser;
import com.lxm.framework.mybatisplus.util.InterceptUtils;
import com.lxm.framework.common.principle.StandardPrinciple;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class TenantSqlTest {
    @Test
    void tenantAndDeletionCannotBeBypassedByOrOrExistingPredicate() throws Exception {
        try (var connection =
                DriverManager.getConnection(
                        "jdbc:h2:mem:tenant;MODE=MySQL;DATABASE_TO_LOWER=TRUE")) {
            try (var setup = connection.createStatement()) {
                setup.execute("create table records(id int, tenant_id int, deleted int)");
                setup.execute("insert into records values(1,7,0),(2,8,0),(3,7,1)");
            }
            var identity = new StandardPrinciple(1, "account", 42, "user", 7);
            String sql =
                    new CustomizedSqlParser(false, connection, "", identity)
                            .parser(
                                    "select id from records r where r.tenant_id=8 or 1=1 order by id")
                            .getSql();
            try (var statement = connection.createStatement();
                    var rows = statement.executeQuery(sql)) {
                assertTrue(rows.next());
                assertEquals(1, rows.getInt(1));
                assertFalse(rows.next());
            }
            assertThrows(
                    IllegalStateException.class,
                    () ->
                            new CustomizedSqlParser(false, connection, "", null)
                                    .parser("select * from records"));
            var parser = new CustomizedSqlParser(false, connection, "", identity);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> parser.parser("insert into records(id,tenant_id) values(9,8)"));
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            parser.parser(
                                    "insert into records(id,tenant_id) select id,7 from records"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> parser.parser("update records set tenant_id=8 where id=1"));
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            parser.parser(
                                    "insert into records(id,tenant_id) values(1,7) on duplicate key update deleted=0"));
            assertEquals(
                    "insert into records(id,tenant_id) values(9,7)",
                    parser.parser("insert into records(id,tenant_id) values(9,7)").getSql());
        }
    }

    @Test
    void identityFillFindsWrappedEntityAndRejectsAnotherTenant() {
        var identity = new StandardPrinciple(1, "account", 42, "user", 7);
        var entity = new com.lxm.framework.mybatisplus.model.TenantEntity();
        InterceptUtils.fillIdentity(Map.of("et", entity), identity, true);
        assertEquals(7, entity.getTenantId());
        assertEquals(42, entity.getCreatedById());
        assertEquals(42, entity.getUpdatedById());
        entity.setTenantId(8);
        assertThrows(
                IllegalArgumentException.class,
                () -> InterceptUtils.fillIdentity(Map.of("et", entity), identity, true));
    }
}
