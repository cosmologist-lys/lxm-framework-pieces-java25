# lxm-framework-all

`all` 是完整依赖集合，把 common、web、auth、redis、mybatisplus、mongo、excel、email 和 enigma 放入一个应用 classpath。它不包含独立业务代码，适合同时使用多种框架能力的 Servlet MVC 应用。

## 引入依赖

要求 JDK 25。先按[项目说明](../README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-all</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 引入后的行为

使用普通 `@SpringBootApplication` 启动即可，无需扫描框架包。Web 返回与异常集成默认启用；外部资源和鉴权/加密功能分别启用：

| 功能 | 开关 | 具体接入 |
| --- | --- | --- |
| Redis | `lfp.redis.enabled` | [连接、存储、锁与订阅](../lxm-framework-redis/README.md) |
| MySQL | `lfp.mybatis.enabled` | [数据源、Mapper、可信身份与事务](../lxm-framework-mybatisplus/README.md) |
| MongoDB | `lfp.mongo.enabled` | [URI、数据库、CRUD](../lxm-framework-mongo/README.md) |
| Auth | `lfp.auth.enabled` | [token 和业务授权策略](../lxm-framework-auth/README.md) |
| Enigma | `lfp.enigma.enabled` | [身份 SPI 与会话存储](../lxm-framework-enigma/README.md) |

这些开关默认关闭，所以没有资源配置时不会要求连接 Redis/MySQL/MongoDB。只打开需要的功能，按对应 README 配置实际连接与业务规则。

## 组合示例

如果应用只需要统一返回和 Excel 下载，直接引入 `web/excel` 即可；如果同一个后台应用同时有鉴权、持久化、缓存和导出，可以使用 `all` 减少 POM 中的模块列表。

```yaml
lfp:
  auth:
    enabled: true
  redis:
    enabled: true
  mybatis:
    enabled: true
```

以上只启用模块，不是完整部署配置；继续提供连接参数、Mapper 和认证策略。Redis、Auth、Enigma 使用不同的配置前缀，不会自动共享配置。

## 注意事项

- `all` 引入全部依赖；只需要少量功能时使用单独模块更合适。
- 引入 Enigma 不会自动保护所有接口，必须提供身份解析器、存储配置并明确标记受保护接口。
- Jedis 因 Redis/Auth 模块而可用；单独使用 Enigma 时它是可选依赖。
- 项目面向 Servlet MVC，不把同时引入的功能变成 WebFlux 集成。
- 可按模块的 Bean 类型/名称替换默认实现，替换后仍需应用验证相关行为。

回到[项目接入说明](../README.md)。
