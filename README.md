# lxm-framework-pieces Java 25

lxm-framework-pieces（lfp）是一组面向 Java Servlet 应用的基础组件。你可以按需使用统一 JSON 返回、鉴权、Redis、MySQL/MyBatis-Plus、MongoDB、Excel、邮件，也可以通过 Enigma 为前后端请求增加加密和消息认证。

项目要求 JDK 25 或更高版本，使用 Spring Boot 4.1.1、Spring Framework 7 和 Jackson 3。服务端组件面向 Spring MVC；`common` 和 `email` 也可以在普通 Java 程序中使用。

## 选择模块

| 模块 | 适合的需求 |
| --- | --- |
| [common](lxm-framework-common/README.md) | 返回类型、身份接口、分页接口、内存缓存和通用工具 |
| [web](lxm-framework-web/README.md) | REST 接口返回 `JsonResult`、字段过滤、异常响应、HTTP 工具 |
| [auth](lxm-framework-auth/README.md) | 登录 token、会话、角色与权限、URL 过滤 |
| [redis](lxm-framework-redis/README.md) | Redis 对象存储、租约锁、发布订阅 |
| [mybatisplus](lxm-framework-mybatisplus/README.md) | MySQL Mapper、分页、租户隔离、逻辑删除、事务 |
| [mongo](lxm-framework-mongo/README.md) | MongoDB 实体、条件查询、CRUD 和分页 |
| [excel](lxm-framework-excel/README.md) | 注解式 Excel 导入导出、多 sheet、MVC 下载 |
| [email](lxm-framework-email/README.md) | 文本/HTML 邮件、附件、读取 INBOX |
| [enigma](lxm-framework-enigma/README.md) | JSON 加解密、无 body 参数验签、密钥会话和防重放 |
| [all](lxm-framework-all/README.md) | 在一个应用中引入全部功能模块 |
| [parent](lxm-framework-parent/README.md) | 统一 Maven 依赖版本、编译和测试配置 |

只需要 REST 工具时引入 `web` 即可。`all` 提供全部依赖，但 Redis、MySQL、MongoDB、Auth 和 Enigma 仍需要分别启用和配置。

## 快速开始

### 1. 构建制品

当前制品版本为 `2.0.0-java25-SNAPSHOT`，尚未发布到 Maven Central。先下载本仓库，在 JDK 25 环境下安装到本地 Maven 仓库；团队也可以部署到自己的制品仓库。

```bash
./mvnw clean install
```

Maven Wrapper 固定 Maven 3.9.16。Windows 使用 `mvnw.cmd`。

### 2. 创建应用

应用可以继承 lfp parent，以使用框架配套的依赖版本。下面的 POM 创建一个独立版本为 `1.0.0-SNAPSHOT` 的 Web 应用：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.lxm</groupId>
        <artifactId>lxm-framework-parent</artifactId>
        <version>2.0.0-java25-SNAPSHOT</version>
        <relativePath/>
    </parent>
    <groupId>example</groupId>
    <artifactId>lfp-demo</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>com.lxm</groupId>
            <artifactId>lxm-framework-web</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

在应用自己的包中声明启动类和 Controller，无需扫描 `com.lxm`：

```java
package example;

import com.lxm.framework.web.jsonresult.JsonResult;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/hello")
    public JsonResult<?> hello() {
        return JsonResult.json(0, "", Map.of("name", "中文"));
    }
}
```

运行 `./mvnw spring-boot:run`，请求 `/hello` 得到：

```json
{"code":0,"message":"","data":{"name":"中文"}}
```

`code=0` 表示成功；非零值表示业务失败，`message` 为可展示的消息，`data` 是数据体。`sign` 用于消息认证，启用 Enigma 后由框架生成；普通未签名响应省略该字段。

### 3. 启用需要的资源

| 配置项 | 默认值 | 启用后需要提供 |
| --- | --- | --- |
| `lfp.web.enabled` | `true` | 通常无需额外配置 |
| `lfp.auth.enabled` | `false` | 应用的 URL、角色和权限策略 |
| `lfp.redis.enabled` | `false` | `spring.data.redis.*` |
| `lfp.mybatis.enabled` | `false` | `spring.datasource.*`、Mapper 路径、可信身份提供器 |
| `lfp.mybatis.transactions.enabled` | `false` | 使用方法名事务切面时配置表达式；普通事务推荐 `@Transactional` |
| `lfp.mongo.enabled` | `false` | MongoDB URI 和数据库名 |
| `lfp.enigma.enabled` | `false` | 身份解析器和会话存储；多实例还需要共享存储 |

具体 YAML、Bean 和调用示例见对应模块 README。自动配置通过 `AutoConfiguration.imports` 加载；提供自定义 Bean 时，按模块说明的类型或名称替换默认组件。

## 前后端加密

Enigma 提供 AES-256-GCM JSON 加解密和 HMAC-SHA-256 参数/响应认证。接口通过 `@EnigmaProtected` 声明模式，浏览器先获取密钥会话，再使用 Web Crypto 客户端发起请求。密钥具备刷新、在途响应保留、使用量限制和撤销机制。

从 [Enigma 接入说明](lxm-framework-enigma/README.md)开始；[浏览器示例](examples/enigma-browser/README.md)可以直接运行，完整消息格式见[协议文档](docs/enigma-protocol.md)。

## 注意事项

- 使用 parent 或相同的依赖管理版本，避免应用自己的 BOM 将 Jackson、Jedis 等降为不同版本。[依赖版本表](docs/dependency-matrix.md)列出了本项目的版本与核查来源。
- 框架提供技术组件，登录校验、数据访问授权、业务幂等和第三方服务凭据由应用负责。
- `AuthManager`、`Xmauth`、`RedisClient`、`MongoHelper` 的静态入口按一个 JVM 单应用上下文使用；多个独立上下文请使用实例组件或自定义实现。
- 租约锁不提供 fencing 或自动续约；Enigma 防重放不等于业务事务幂等。详见对应模块的限制。
- 数据库、Redis、邮箱及 Enigma wrapping key 从环境或密钥管理系统注入；不要提交真实凭据。

## 开发与测试

```bash
./mvnw clean verify
# 专用 Redis/MySQL/MongoDB 服务可用时执行集成测试
./mvnw clean verify -Pintegration
```

普通测试由 Surefire 执行，`*IT` 由集成 profile 的 Failsafe 执行。专用服务端口和测试命令见[验证说明](docs/verification.md)。GitHub Actions 同时执行服务集成和 Chromium 前后端互通检查。格式约定见[格式说明](docs/formatting.md)。

另一个 JDK 版本见 [Java 21 仓库](https://github.com/cosmologist-lys/lxm-framework-pieces-java21)。两者使用不同 Maven 制品版本，遵守相同 Enigma v1 协议。
