# lxm-framework-enigma

请求加密、参数验签与响应认证。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-enigma:2.0.0-java25-SNAPSHOT`。

## 功能与接入

通过 `@EnigmaProtected(Mode.ENCRYPT)` 或 `Mode.SIGN` 指定服务端策略。ENCRYPT 接口一个 @RequestBody，SIGN 无 body；返回 JsonResult<?> 或 ResponseEntity<JsonResult<?>>。显式 `lfp.enigma.enabled=true`，指定 store=memory/redis 并提供 EnigmaIdentityResolver，缺失时启动失败。

## 使用约定与验证

身份解析器必须从已校验的登录凭证获得 user/tenant/loginSession；不能读取客户端自报身份头作为可信身份。TLS、Origin、Cookie CSRF、时间偏差、nonce、IV、使用量及容量都有独立校验。memory 仅开发单实例；多实例使用 Redis Standalone 或自定义 SessionStore。完整 wire 格式、轮换、错误和支持限制见 [协议](../docs/enigma-protocol.md)。[浏览器示例](../examples/enigma-browser/README.md) 含 Web Crypto SDK 和固定向量；仅测试凭证，不提供生产鉴权实现。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
