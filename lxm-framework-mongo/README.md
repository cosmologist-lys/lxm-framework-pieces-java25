# lxm-framework-mongo

`mongo` 为 MongoDB 提供基础实体、条件构造、CRUD、批量插入和分页。它基于 Spring Data `MongoTemplate` 与同步 MongoDB 驱动，适合不希望为每个集合编写 Repository 的应用。

## 引入依赖

要求 JDK 25。先按[项目说明](../README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-mongo</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 启用连接

```yaml
lfp:
  mongo:
    enabled: true
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://127.0.0.1:27017}
      database: app
```

模块默认关闭，启用时必须配置数据库名。连接 URI 可以包含应用需要的认证和连接参数；真实凭据从环境注入。自动配置创建 `MongoClient`、`MongoTemplate` 和 `MongoService`，应用可提供同类型 Bean。

## 定义实体

```java
import com.lxm.framework.mongo.entity.MongoBaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("users")
class UserDocument extends MongoBaseEntity {
    private String name;
    private int rank;
}
```

`MongoBaseEntity` 使用 `ObjectId` 主键，并提供 `created_at/updated_at` 时间字段；只需要基础插入能力时可继承 `MongoRootEntity` 并定义自己的主键。集合名称可以使用 `@Document`，也可以通过显式 `col` 参数指定。

## 插入与查询

```java
import com.lxm.framework.mongo.core.MongoQuery;
import com.lxm.framework.mongo.core.MongoService;
import java.util.List;

MongoService service = applicationMongoService; // 应用注入的 Bean。
var user = new UserDocument();
user.setName("中文");
user.setRank(1);
service.save(user);
UserDocument stored = service.getById(user.getId(), UserDocument.class);

List<UserDocument> users =
        service.getList(
                new MongoQuery().between("rank", 1, 10).orderByAsc("rank"),
                UserDocument.class);
```

`save` 是 insert，重复主键报错，不是 upsert。多条插入使用 `saveBatch(items, UserDocument.class)`。

`MongoQuery` 支持 `eq/ne/in/notin/like/between/betweenDateTime/orderByAsc/orderByDesc`。`like` 将输入作为字面字符串、忽略大小写，不把用户输入当作正则表达式。多个约束针对同一字段时优先使用一个范围条件，避免重复 Criteria 的冲突。

## 更新与删除

```java
import org.springframework.data.mongodb.core.query.Update;

service.edit(
        new MongoQuery().eq("rank", 1),
        new Update().set("name", "updated"),
        UserDocument.class);
service.editMulti(
        new MongoQuery().between("rank", 1, 10),
        new Update().set("rank", 20),
        UserDocument.class);
service.remove(new MongoQuery().eq("name", "updated"), UserDocument.class);
```

`edit` 更新首条匹配记录，`editMulti` 更新全部匹配记录。方法返回驱动的 `UpdateResult/DeleteResult`，调用方可以检查实际修改/删除数量。空条件会匹配整个集合，应用必须明确授权和限制操作范围。

## 分页

调用 `service.getPage(page, query, UserDocument.class)`；`page` 是应用实现的 `StandardPage<UserDocument>`。接口要求当前页、每页数量、升降序字段，以及回写总数/记录的方法。

当前页从 1 开始，每页数量为 1..1000。总数不带 skip/limit；页查询复制输入 Query，因此可以复用同一个条件查询下一页。分页对象保存返回结果，不应在并发请求中共享。

## 注意事项

- `MongoHelper` 使用静态模板入口，按一个 JVM 单数据库上下文使用；多个独立数据库上下文建议注入原生 `MongoTemplate`。
- 查询字段、排序列和集合名由应用的允许列表决定，不能作为任意客户端输入直接执行。
- 本模块不自动加入业务权限、租户规则或事务；需要时在应用服务层实现。
- 数据库索引和大集合分页性能由使用方设计；offset 分页不等于游标分页。
- 复用一个实体对象更新前应明确字段与审计时间的处理规则；直接 `Update` 操作按指定内容写入。

回到[项目接入说明](../README.md)。
