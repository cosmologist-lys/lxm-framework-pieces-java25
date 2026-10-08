# lxm-framework-mybatisplus

`mybatisplus` 将 MyBatis-Plus、MySQL、连接池和 Spring 事务整合到应用，提供 Mapper 扫描、分页、身份填充、租户隔离和逻辑删除。默认使用 BeeCP；实体和 Mapper 仍使用 MyBatis-Plus 的标准 API。

## 引入依赖

要求 JDK 25。先按[项目说明](../README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-mybatisplus</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 配置数据源与 Mapper

```yaml
lfp:
  mybatis:
    enabled: true
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/app
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    name: app
  mybatis:
    package-name: example
    mapper-packages: example.persistence.mapper
    type-aliases-package: example.domain
    resource-path-pattern: classpath*:mapper/*.xml
```

模块默认关闭。`mapper-packages` 指定扫描包，只注册带 `@Mapper` 的接口；XML Mapper 放在匹配 `resource-path-pattern` 的资源路径内。`name` 是连接池名称，连接参数来自 `spring.datasource`。

## 定义实体和 Mapper

```java
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxm.framework.mybatisplus.model.TenantEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotations.Mapper;

@Getter
@Setter
@TableName("orders")
class Order extends TenantEntity {
    @TableId private Long id;
    private String description;
}

@Mapper
interface OrderMapper extends BaseMapper<Order> {}
```

实体基类按需要选择：

| 基类 | 提供的列 |
| --- | --- |
| `SimpleEntity` | `created_at`、`updated_at`、`deleted`、`deleted_time` |
| `PlainEntity` | 上述列加 `created_by_id`、`updated_by_id` |
| `TenantEntity` | 上述列加 `tenant_id` |

数据库表必须包含实体实际映射的列。应用也可以定义自己的实体；是否执行租户/删除条件与表的元数据有关。

## 提供当前可信身份

应用注册 `PrincipleProvider` Bean，从认证层取出当前账号、用户和租户。`current()` 返回 `StandardPrinciple`。没有可信身份时，受租户约束的 SQL 会拒绝执行，不会选择一个默认租户。

```java
import com.lxm.framework.common.principle.PrincipleProvider;
import org.springframework.context.annotation.Bean;

// CurrentIdentityContext 是应用已有的认证上下文组件。
@Bean
PrincipleProvider principleProvider(CurrentIdentityContext identities) {
    return identities::currentPrinciple;
}
```

示例中的 `CurrentIdentityContext` 由应用实现，不能把未经认证的请求参数作为身份。后台任务需要显式建立自己的可信身份上下文。

## 查询、分页和事务

```java
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lxm.framework.mybatisplus.pagenation.Pagination;

Pagination<Order> page = new Pagination<>(1, 20);
OrderMapper mapper = applicationOrderMapper;
mapper.selectPage(page, new QueryWrapper<Order>().like("description", "中文"));
```

`applicationOrderMapper` 是应用注入的 Mapper。分页 count 与数据查询先应用租户/删除条件，再进行分页；每页上限默认 1000。排序列名来自应用允许的字段集合，不能直接放入任意客户端 SQL 片段。

写操作推荐在 Spring 服务方法上使用 `@Transactional`。模块提供 `transactionManager`；需要按方法名应用事务时，另外启用 `lfp.mybatis.transactions.enabled=true` 并设置 `spring.mybatis.expression`。方法名切面默认关闭。

## 自定义 SQL 与隔离规则

- 含 `tenant_id` 的表使用当前身份约束查询和写入；含 `deleted` 的表过滤已删除记录。
- INSERT 支持明确列清单的 `VALUES`；已提供的 `tenant_id` 必须与当前身份相同。
- `INSERT SELECT`、upsert 和修改 `tenant_id` 的 UPDATE 不在受保护 SQL 的支持范围内。
- `TenantEntity.tenantId` 不参与实体更新；逻辑删除使用 MyBatis-Plus 的标准机制。
- 项目内的 escape 标记仅用于应用明确审查的特权 SQL，不能由客户端选择是否绕过隔离。
- 复杂 SQL 应使用实际数据库验证其解析、租户约束与分页行为；不能仅凭语法编译判断隔离正确。

## 自定义组件与注意事项

应用可以提供同名的 `beeDataSource`、`sqlSessionFactory`、`globalConfig`、`transactionManager`、`parameterInterceptor` 和 `statementInterceptor`。替换数据源时提供 `DataSource`，保持框架约定的名称；替换拦截器时自行承担相应隔离和填充行为。

框架自动配置以 MySQL 为默认数据库，不提供任意数据库方言的通用保证。连接池属于应用资源，不要在业务调用中反复初始化。数据库账号权限、索引、事务隔离级别和容量由使用方配置。

回到[项目接入说明](../README.md)。
