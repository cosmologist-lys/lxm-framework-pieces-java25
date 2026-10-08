# lxm-framework-auth

`auth` 提供登录 token、账号会话、禁用/踢出、URL 过滤和角色权限注解。应用负责验证账号凭据，以及实现角色、权限和受保护路径规则；框架负责这些规则的执行与 token/session 存储。

## 引入依赖

要求 JDK 25。先按[项目说明](README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-auth</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 启用和选择存储

```yaml
lfp:
  auth:
    enabled: true
lxm-auth:
  token:
    token-name: lxmtoken
    timeout: 86400
    token-style: random-64
    is-read-head: true
    is-read-cookie: false
    is-read-parameter: false
```

未配置 Auth Redis 端口时使用内存 DAO，适合单实例。多实例部署配置独立的 Auth Redis：

```yaml
lxm-auth:
  redis:
    host: ${AUTH_REDIS_HOST:127.0.0.1}
    port: 6379
    database: 2
    password: ${AUTH_REDIS_PASSWORD}
```

Auth 的配置前缀为 `lxm-auth.redis`，与通用 Redis 模块的 `spring.data.redis` 相互独立。默认 host 为 `127.0.0.1`、database 为 `2`；password 从环境注入。

## 登录与注销

在应用完成账号密码、授权码等凭据验证后，使用业务确定的 loginId：

```java
import com.lxm.framework.auth.model.Xmauth;

String token = Xmauth.signIn("account:42", "browser");
Xmauth.checkSignIn();
String loginId = Xmauth.getLoginId();
Xmauth.signOut();
```

这些调用在已初始化的 Servlet 请求上下文内使用。登录接口将 token 按应用约定返回或写入 Cookie；后续请求携带配置名称对应的 token。`signIn` 不验证密码，不能直接信任客户端提供的 loginId。

其他入口：`isSignIn()` 判断登录状态；`kick(loginId)`/`kickByToken(token)` 踢出；`forbid(loginId, seconds)` 禁用；`permit(loginId)` 解除禁用。

## URL 过滤

应用提供 `AuthFilterRegister` Bean。下面的示例保护 `/api/**`，允许登录接口通过，错误结果通过 Jackson 构造：

```java
import com.lxm.framework.auth.filter.ServletErrorStrategy;
import com.lxm.framework.auth.filter.ServletFilterStrategy;
import com.lxm.framework.auth.model.AuthFilterRegister;
import com.lxm.framework.auth.model.Xmauth;
import org.springframework.context.annotation.Bean;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

@Bean
AuthFilterRegister authFilterRegister() {
    var mapper = JsonMapper.builder().build();
    return new AuthFilterRegister() {
        public List<String> includes() {
            return List.of("/api/**");
        }

        public List<String> excludes() {
            return List.of("/api/sign-in");
        }

        public ServletFilterStrategy whenBlocked() {
            return Xmauth::checkSignIn;
        }

        public ServletErrorStrategy whenError() {
            return error -> mapper.writeValueAsString(Map.of("code", 1001, "message", "请先登录"));
        }
    };
}
```

配置方法放在应用的 `@Configuration` 类中。`includes/excludes` 使用路径规则；`whenBlocked` 执行检查并在失败时抛出异常；`whenError` 返回有效 JSON 字符串。过滤器写出的错误不会再次经过 MVC advice。

## 角色与权限

应用分别实现并注册 `RoleFilter` 和 `PermissionFilter`：

| 接口 | 必须实现的方法 |
| --- | --- |
| `RoleFilter` | `getRoleList(loginId)`、`hasRole(loginId, role)` |
| `PermissionFilter` | `getPermissionList(loginId)`、`hasPermission(loginId, permission)` |

实现应从已认证用户的授权数据查询结果，不能读取客户端自报权限。提供两类 Bean 后，框架创建默认 `PrincipleFilter`，供注解鉴权使用：

```java
import com.lxm.framework.auth.aop.LxmCheckPermission;
import com.lxm.framework.auth.aop.LxmCheckRole;

@LxmCheckRole("admin")
@LxmCheckPermission("orders:read")
public void readOrders() {
    // 读取已授权的业务数据。
}
```

多个值默认要求全部满足；可以用 `mode = LxmMode.OR` 要求任一满足。注解通过 Spring AOP 执行，应用内部自调用不会经过代理，应按 Spring AOP 的调用约定组织服务。

## 扩展与注意事项

- 可提供 `LxmTokenDao`、`AuthAction`、`LxmAuthLogic`、`PrincipleFilter` 等 Bean 替换默认实现；不提供策略时不会自动获得“所有请求都允许”的规则。
- DAO 有效期参数使用秒。查询 TTL：缺失为 `-1`，永久为 `0`；正值为剩余秒数。DAO 更新不会恢复已删除的 token/session。
- Session 动态属性使用 JSON 数据模型，复杂对象按 Map/List 读取；需要明确 Java 类型时由应用转换。
- 默认同账号登录过程包含多个 Redis 命令，不提供事务性并发登录保证；需要严格原子规则时串行化同账号操作或实现自定义 logic/DAO。
- `Xmauth` 和 `AuthManager` 是一个 JVM 单应用上下文入口，内存存储也不能用于跨实例会话共享。
- 使用 Cookie 时配置合适的域、HTTPS 和应用 CSRF 规则；参数携带 token 会进入 URL/日志，示例因此关闭参数读取。
- 与 Enigma 联用时，先使登录凭据失效，再调用 `EnigmaSessionService.revokeIdentity`；每次登录生成新的 loginSession 标识。

回到[项目接入说明](README.md)。
