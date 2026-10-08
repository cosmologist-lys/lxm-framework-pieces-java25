package org.example;

import com.lxm.framework.mybatisplus.MybatisPlusConfig;
import com.lxm.framework.mybatisplus.model.TenantEntity;
import com.lxm.framework.common.principle.*;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.mapper.RecordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.Getter;
import lombok.Setter;
import java.sql.DriverManager;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class MySqlIT {
    @Getter
    @Setter
    @TableName("lfp_records")
    public static class TenantRecord extends TenantEntity {
        @TableId private Long id;
        private String name;
    }

    @Test
    void realMySqlPaginationCountIsolationIdentityAndRollback() throws Exception {
        String server = System.getProperty("lfp.mysql.server", "jdbc:mysql://127.0.0.1:13306/");
        String user = System.getProperty("lfp.mysql.user", "root"),
                password = System.getProperty("lfp.mysql.password", "");
        String database = "lfp_test_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection =
                        DriverManager.getConnection(
                                server + "?allowPublicKeyRetrieval=true&useSSL=false",
                                user,
                                password);
                var statement = connection.createStatement()) {
            statement.execute("create database " + database);
            try (var context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment()
                        .getPropertySources()
                        .addFirst(
                                new MapPropertySource(
                                        "test",
                                        Map.of(
                                                "lfp.mybatis.enabled",
                                                "true",
                                                "spring.datasource.url",
                                                server
                                                        + database
                                                        + "?allowPublicKeyRetrieval=true&useSSL=false",
                                                "spring.datasource.username",
                                                user,
                                                "spring.datasource.password",
                                                password,
                                                "spring.datasource.driver-class-name",
                                                "com.mysql.cj.jdbc.Driver",
                                                "spring.mybatis.mapper-packages",
                                                "org.example.mapper")));
                context.registerBean(
                        PrincipleProvider.class,
                        () -> () -> new StandardPrinciple(1, "account", 42, "user", 7));
                context.register(MybatisPlusConfig.class);
                context.refresh();
                var jdbc =
                        new JdbcTemplate(
                                context.getBean("beeDataSource", javax.sql.DataSource.class));
                jdbc.execute(
                        "create table lfp_records(id bigint primary key,name varchar(80),tenant_id int,deleted tinyint default 0,created_at datetime,updated_at datetime,deleted_time datetime,created_by_id int,updated_by_id int)");
                jdbc.execute(
                        "insert into lfp_records(id,name,tenant_id,deleted) values(1,'a',7,0),(2,'b',7,0),(3,'foreign',8,0),(4,'deleted',7,1)");
                var mapper = context.getBean(RecordMapper.class);
                var page =
                        mapper.selectPage(
                                new Page<TenantRecord>(1, 1),
                                new QueryWrapper<TenantRecord>()
                                        .eq("tenant_id", 8)
                                        .or()
                                        .eq("id", 1)
                                        .or()
                                        .eq("id", 2));
                assertEquals(2, page.getTotal());
                assertEquals(1, page.getRecords().size());
                assertEquals(7, page.getRecords().getFirst().getTenantId());
                var entity = new TenantRecord();
                entity.setId(5L);
                entity.setName("inserted");
                mapper.insert(entity);
                assertEquals(7, entity.getTenantId());
                assertEquals(42, entity.getCreatedById());
                var transactions =
                        new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
                transactions.execute(
                        status -> {
                            var rollback = new TenantRecord();
                            rollback.setId(6L);
                            rollback.setName("rollback");
                            mapper.insert(rollback);
                            status.setRollbackOnly();
                            return null;
                        });
                assertNull(mapper.selectById(6));
                assertEquals(3, mapper.selectCount(new QueryWrapper<>()));
                mapper.deleteById(5);
                assertNull(mapper.selectById(5));
                assertEquals(
                        1,
                        jdbc.queryForObject(
                                "select deleted from lfp_records where id=5", Integer.class));
            } finally {
                statement.execute("drop database " + database);
            }
        }
    }
}
