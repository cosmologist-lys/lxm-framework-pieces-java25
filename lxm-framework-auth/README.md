# lxm-framework-auth

token/session 与权限。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-auth:2.0.0-java25-SNAPSHOT`。

## 功能与接入

启用 `lfp.auth.enabled=true`。保留 `Xmauth`、`LxmAuthLogic`、`LxmTokenDao`、角色权限注解和 Servlet 过滤器。未配置 auth Redis 时使用内存 DAO；配置 `lxm-auth.redis.port` 后使用受 Spring 管理的 `TokenDaoRedisDefault`，host 默认 127.0.0.1，database 默认 2。

## 使用约定与验证

使用方提供 `RoleFilter`、`PermissionFilter`、`AuthFilterRegister` 等业务策略，框架不会自动生成允许所有请求的规则。Filter 失败返回有效 JSON，与 MVC advice 独立。DAO timeout 单位秒：-1 缺失，0 永久；删除后的更新使用 Redis SET XX，禁止复活。session 内的属性为 JSON 值，复杂 Java 类型读取为 Map/List。AuthManager 保留旧静态入口，因此一个 JVM 中同时存在多个独立鉴权上下文不受支持。默认登录过程由多个 Redis 命令组成，不是事务；同账号并发登录需要应用串行化或自定义 DAO/logic。Enigma 必须每次请求查询当前有效身份，注销先失效 token/session，再调用 revokeIdentity；每次登录使用新的 loginSession 标识。测试：Filter、内存 TTL、Redis session/删除竞态和旧样本。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
