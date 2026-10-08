# lxm-framework-pieces Java 25

面向 Servlet MVC 应用的基础组件库，提供统一返回值、鉴权、Redis、MyBatis-Plus/MySQL、MongoDB、Excel、邮件，以及 Enigma 请求保护。最低运行环境为 Java 25，统一采用 Spring Boot 4.1.1 / Jackson 3。Java 17 的 `1.0` 制品和原仓库保持独立。

## 模块选择

| 模块 | 功能与依赖边界 |
| --- | --- |
| [parent](lxm-framework-parent/README.md) | Maven BOM、内部版本、编译和测试插件 |
| [common](lxm-framework-common/README.md) | 无 Spring/Servlet 的返回接口、身份、缓存及工具 |
| [web](lxm-framework-web/README.md) | MVC、JsonResult、Jackson 3、HTTP 与文件工具 |
| [auth](lxm-framework-auth/README.md) | token/session、角色权限、Servlet Filter、注解鉴权 |
| [redis](lxm-framework-redis/README.md) | 存储、所有者锁、消息总线；需要明确启用 |
| [mybatisplus](lxm-framework-mybatisplus/README.md) | MySQL、事务、分页、租户和逻辑删除 |
| [mongo](lxm-framework-mongo/README.md) | MongoDB CRUD、条件、分页 |
| [excel](lxm-framework-excel/README.md) | POI 导入导出、多 sheet、HTML 转换、MVC 下载 |
| [email](lxm-framework-email/README.md) | Jakarta Mail / Angus 邮件发送和接收 |
| [enigma](lxm-framework-enigma/README.md) | AES-GCM、HMAC、密钥轮换、原子重放准入 |
| [all](lxm-framework-all/README.md) | 上述功能的依赖集合；外部服务和 Enigma 默认关闭 |

初版保留模块划分。`auth/redis` 移除无实际代码用途的 `web` 依赖；`excel` 保留 MVC 下载接口与 `web` 依赖。持久层直接组合 Spring Data / MyBatis 基础依赖，避免第三方 starter 在缺少服务配置时自动创建客户端。`enigma → web → common`，通过身份 SPI 和可选 Jedis 接入鉴权与共享状态，不形成反向依赖。

## 构建和引入

```bash
./mvnw clean verify
./mvnw install
```

开发坐标为 `com.lxm:<artifactId>:2.0.0-java25-SNAPSHOT`。尚未发布到 Maven Central；先本地 `install`，或由使用方按自身制品流程发布。源码推送不等于 Maven 制品部署。应用使用 Boot 4.1.1 BOM，并按需引入：

```xml
<dependency>
  <groupId>com.lxm</groupId>
  <artifactId>lxm-framework-web</artifactId>
  <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

普通 `@SpringBootApplication` 无需扫描框架包。模块的 `AutoConfiguration.imports` 自动注册配置，用户提供对应 Bean 时按说明退让。

```java
@RestController
class GreetingController {
    @GetMapping("/greeting")
    JsonResult<?> greeting() { return JsonResult.json(0, "", Map.of("name", "中文")); }
}
```

返回结构为 `code/message/data/sign`，`code=0` 表示成功。`Result` 保留原有 `returnValue`，不会自动转为 `JsonResult`。内部异常返回固定消息；业务 `AppException` 保留业务码和消息。模块外 Controller 已有真实 HTTP 与全依赖启动测试。

## 功能开关

| 配置 | 默认行为 |
| --- | --- |
| `lfp.web.enabled` | `true`，MVC 返回集成；可关闭或覆盖 Bean |
| `lfp.auth.enabled` | 关闭；启用后由使用方提供鉴权策略 |
| `lfp.redis.enabled` | 关闭；启用时读取 `spring.data.redis.*` |
| `lfp.mybatis.enabled` | 关闭；启用时读取 `spring.datasource.*` |
| `lfp.mybatis.transactions.enabled` | 关闭；建议应用使用 `@Transactional` |
| `lfp.mongo.enabled` | 关闭；启用时要求数据库名 |
| `lfp.enigma.enabled` | 关闭；启用时必须指定存储和身份解析器 |

详见模块 README。不要在新版本直接复用旧 Redis 对象命名空间；默认前缀为 `lfp:v2:`，JSON 对象读取为 Map/List，不启用任意类型反序列化。Auth session JSON 保留明确的模型，旧格式样本另有回归测试。

## Enigma 与验证

[协议与生命周期](docs/enigma-protocol.md)、[浏览器示例](examples/enigma-browser/README.md)、[Java 17→21 迁移](docs/migration-java17-to-java21.md)、[Java 21→25 迁移](docs/migration-java21-to-java25.md)、[依赖矩阵](docs/dependency-matrix.md)、[审查记录](docs/code-review.md)、[测试与服务启动](docs/verification.md)。CI 执行完整 reactor、Redis/MySQL/MongoDB 集成以及 Chromium Web Crypto 互操作。

Java 21 基线仓库：[lxm-framework-pieces-java21](https://github.com/cosmologist-lys/lxm-framework-pieces-java21)。两个版本使用不同 Maven 版本，相同 Enigma v1 协议和向量。
