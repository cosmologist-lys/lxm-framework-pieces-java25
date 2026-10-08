# lxm-framework-enigma

Enigma 为前后端分离的 Servlet MVC 应用提供 JSON 加解密、无 body 参数验签和响应认证。它使用 AES-256-GCM、HMAC-SHA-256 与 JSON 规范化，对身份、路径、参数、时间和 nonce 进行绑定，并管理密钥刷新、在途响应和撤销。

它工作在 HTTPS 和已有登录系统之上。应用仍负责身份验证、授权、业务幂等和密钥存储运维。

## 引入依赖

要求 JDK 25。先按[项目说明](../README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-enigma</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 选择存储并启用

单实例开发可使用内存存储：

```yaml
lfp:
  enigma:
    enabled: true
    store: memory
    allowed-origins:
      - https://app.example.com
```

默认要求 HTTPS。只有本机开发时可显式设置 `allow-insecure-localhost: true`；该设置不用于对外部署。内存存储不在多个实例之间共享，也不在进程重启后保留会话。

多实例使用 Redis Standalone：

```yaml
lfp:
  enigma:
    enabled: true
    store: redis
    redis-uri: ${ENIGMA_REDIS_URI}
    redis-namespace: "app:enigma:v1:"
    active-wrapping-kid: deployment-v1
    wrapping-keys:
      deployment-v1: ${ENIGMA_WRAPPING_KEY}
    allowed-origins:
      - https://app.example.com
```

`ENIGMA_WRAPPING_KEY` 是独立随机 32 字节密钥的无 padding Base64URL 表示，从密钥管理系统提供。Redis 记录经认证加密保存，wrapping key 不放入 Redis。所有实例必须使用相同的 namespace 和配套 wrapping key；URI 凭据也从环境提供。

Jedis 在 Enigma POM 中是可选依赖，单独使用 Enigma + Redis 存储时，应用还需引入 `redis.clients:jedis`，版本由 parent 管理；使用 `all/redis/auth` 时已包含驱动。

## 接入已认证身份

必须提供 `EnigmaIdentityResolver` Bean。下面示例使用应用认证过滤器已经设置的 `Principal`，不读取客户端自报的用户或租户：

```java
import com.lxm.framework.enigma.spi.EnigmaIdentity;
import com.lxm.framework.enigma.spi.EnigmaIdentityResolver;
import java.security.Principal;
import org.springframework.context.annotation.Bean;

record LoginPrincipal(
        String userId,
        String tenantId,
        String loginSession,
        boolean cookieAuthentication,
        String csrfToken)
        implements Principal {
    public String getName() {
        return userId;
    }
}

@Bean
EnigmaIdentityResolver enigmaIdentityResolver() {
    return request -> {
        if (!(request.getUserPrincipal() instanceof LoginPrincipal principal)) {
            return null;
        }
        return new EnigmaIdentity(
                principal.userId(),
                principal.tenantId(),
                principal.loginSession(),
                principal.cookieAuthentication(),
                principal.csrfToken());
    };
}
```

将 Bean 方法放在应用配置类中。`LoginPrincipal` 是示例的应用身份模型，认证层必须校验当前凭据、登录撤销状态并建立它；仅有该 record 不会自动完成登录。

每个受保护请求都会重新解析身份。`loginSession` 必须在每次重新登录时改变，user/tenant 取自可信认证层。Cookie 认证传入服务器会话中的可信 CSRF token，浏览器须携带匹配的 `X-CSRF-Token`。缺少身份解析器或有效存储时，启用配置会启动失败。

## 声明接口模式

```java
import com.lxm.framework.enigma.mvc.EnigmaProtected;
import com.lxm.framework.web.jsonresult.JsonResult;
import org.springframework.web.bind.annotation.*;

@RestController
class ProtectedController {
    record Payload(String name) {}

    @PostMapping(
            value = "/protected/body",
            consumes = "application/json",
            produces = "application/json")
    @EnigmaProtected(EnigmaProtected.Mode.ENCRYPT)
    public JsonResult<?> body(@RequestBody Payload payload) {
        return JsonResult.json(0, "", payload);
    }

    @GetMapping(value = "/protected/params", produces = "application/json")
    @EnigmaProtected(EnigmaProtected.Mode.SIGN)
    public JsonResult<?> params(@RequestParam String name) {
        return JsonResult.json(0, "", name);
    }
}
```

ENCRYPT 接口只声明一个 JSON `@RequestBody`，框架在 DTO 绑定前解密；SIGN 接口没有 body，通过协议 header 验签完整路径和 query。模式由服务器注解决定，不能由客户端省略 body 来降级。

响应支持同步 `JsonResult<?>` 或 `ResponseEntity<JsonResult<?>>`。先完成业务字段过滤及序列化，再加密或签名。业务使用的 `sign` 字段交由 Enigma 管理。

## 浏览器调用

把示例的 `enigma.js` 接入前端模块：

```javascript
import { EnigmaClient, BusinessError, ProtocolError } from "./enigma.js";

const client = new EnigmaClient({
  origin: location.origin,
  authorization: `Bearer ${currentAccessToken}`,
});
await client.session();
await client.call("POST", "/protected/body", {
  mode: "ENCRYPT",
  body: { name: "中文" },
});
await client.call("GET", "/protected/params?name=中文", { mode: "SIGN" });
await client.refresh();
await client.revoke();
```

`currentAccessToken` 来自应用已有登录流程。authorization/csrfToken 接受字符串；凭据变化后更新客户端属性并重新建立会话。Cookie 认证额外传入 `csrfToken`。客户端先验证 HTTP status、消息、数据和请求绑定，再处理业务错误；`BusinessError` 的消息可以展示，`ProtocolError` 表示响应不能作为可信业务结果。

可运行演示与客户端方法说明见[浏览器 README](../examples/enigma-browser/README.md)。

## 会话与配置

| 配置 | 默认值 | 用途 |
| --- | --- | --- |
| `key-ttl-millis` | 900000 | 密钥使用期限，15 分钟 |
| `refresh-after-millis` | 720000 | 建议刷新时间，12 分钟 |
| `overlap-millis` | 120000 | 旧 key 的新请求接受宽限 |
| `response-grace-millis` | 300000 | 旧请求响应保留窗口 |
| `clock-skew-millis` | 60000 | 请求时钟偏差 |
| `nonce-ttl-millis` | 180000 | nonce 防重放窗口 |
| `max-uses-per-key` | 4096 | 每个方向的请求/响应使用上限 |
| `max-body-bytes` | 1048576 | JSON 明文大小上限 |
| `max-envelope-bytes` | 1572864 | 加密信封大小上限 |
| `sessions-per-minute` | 5 | 每个身份的会话签发速率 |
| `max-sessions` | 1000 | 内存存储的会话容量 |

配置必须满足生命周期与容量关系，非法组合会拒绝启动。`POST /enigma/session` 创建标签页会话；`POST /enigma/session/refresh` 刷新；`POST /enigma/session/revoke` 撤销指定 sid。控制接口本身通过 HTTPS、当前身份、Origin 和必要的 CSRF 保护。

应用注销时先撤销登录凭据，再调用 `EnigmaSessionService.revokeIdentity(identity)` 撤销该登录身份的全部 Enigma 会话。刷新同一旧 kid 在宽限期内幂等；客户端保留旧 kid 以验证已发送请求的在途响应。

## 注意事项

- 当前保护同步、有界 JSON。multipart、文件下载、SSE、受保护异步输出、HEAD/204/205/304 等无 JSON body 场景不受支持；普通未保护异步接口可以使用。
- JSON 整数必须在 JavaScript 安全整数范围内；大 ID、高精度金额使用字符串。拒绝重复字段、非法 Unicode 和非规范编码。
- nonce 一旦准入不会因 DTO/业务失败而释放。写操作不自动重试，业务结果恢复需要独立幂等键或查询。
- Redis 实现支持 Standalone，不提供 Cluster 或故障切换回滚后的强一致保证。状态丢失/回滚后切换 namespace epoch 或 wrapping key 并重新握手。
- 密钥只放入浏览器内存中的不可导出 CryptoKey；不要写入 localStorage、Cookie 或日志。页面刷新后重新建立会话。
- 公共代理路径需通过可信部署配置的 `public-path-prefix` 与浏览器实际路径对齐；不信任任意客户端转发头。

完整消息字段、认证上下文、错误码、存储与恢复规则见[Enigma v1 协议](../docs/enigma-protocol.md)。回到[项目接入说明](../README.md)。
