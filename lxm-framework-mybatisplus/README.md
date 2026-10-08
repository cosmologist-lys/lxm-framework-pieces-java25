# lxm-framework-mybatisplus

MySQL、租户与分页。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-mybatisplus:2.0.0-java25-SNAPSHOT`。

## 功能与接入

显式 `lfp.mybatis.enabled=true`，`spring.datasource.url/username/password/driver-class-name/name`；默认驱动 com.mysql.cj.jdbc.Driver。默认 BeeCP，用户可提供名为 beeDataSource 的 DataSource；sqlSessionFactory/globalConfig/transactionManager/拦截器按名称覆盖。`spring.mybatis.mapper-packages` 默认 com.lxm，只扫描 @Mapper。

## 使用约定与验证

应用提供 `PrincipleProvider` Bean，返回当前可信 StandardPrinciple。含 tenant_id 的表缺失身份即拒绝；查询/写入 WHERE 追加租户和 deleted=0 条件，分页 count 先隔离再统计。INSERT 要求明确 columns + VALUES，绑定 tenant_id 必须等于当前身份；INSERT SELECT/upsert 和 UPDATE tenant_id 不受支持，TenantEntity 自动禁止更新该列。Mapper 的既有 escape 标记仅供受审查的服务端特权 SQL，不能让客户端选择。逻辑删除沿用 MyBatis-Plus 维护中的实现；不再手工改写过时 DELETE SQL。分页默认最大 1000。推荐 @Transactional，旧方法名事务切面需要 `lfp.mybatis.transactions.enabled=true`，表达式由 spring.mybatis.expression 配置。真实 MySQL IT 覆盖 count、隔离、身份填充、回滚与逻辑删除；H2 只作为 AST 回归，不替代 MySQL。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
