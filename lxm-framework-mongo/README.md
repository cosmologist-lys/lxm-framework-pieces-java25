# lxm-framework-mongo

MongoDB CRUD 与分页。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-mongo:2.0.0-java25-SNAPSHOT`。

## 功能与接入

显式 `lfp.mongo.enabled=true`；读取 `spring.data.mongodb.uri`（默认 mongodb://localhost:27017）与必填 database。MongoClient/MongoTemplate/MongoService 用户 Bean 优先。普通数据库名可以加载，无需特殊字面值。

## 使用约定与验证

实体继承 MongoRootEntity/MongoBaseEntity，后者使用 ObjectId 类型主键。MongoService.save 是 insert，重复主键报错，不是 upsert。`new MongoQuery().between("rank",1,2)` 使用单个范围条件；like 按字面字符串匹配并忽略大小写，不把输入当正则。分页 current>=1，size=1..1000，不修改传入 Query，总数不带页条件。MongoHelper 保留静态模板入口，因此一个 JVM 中多数据库上下文需要使用原生 MongoTemplate。测试 MongoIT 在唯一临时数据库中覆盖 CRUD、范围、like、排序与重复分页，finally 清理该测试数据库。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
