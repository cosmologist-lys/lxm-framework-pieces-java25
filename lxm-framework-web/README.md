# lxm-framework-web

MVC、统一返回与 Jackson 3。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-web:2.0.0-java25-SNAPSHOT`。

## 功能与接入

`JsonResult.json(0,"",data)` 返回业务数据，`include/exclude` 与 `@JsonResultFilter` 在当前结果范围内生效。Map 工厂复制输入，不修改调用方。`FrameworkJackson.mapper()` 提供不可变默认 mapper，日期维持旧格式；应用可以注册自定义 mapper/module。普通 `Result` 不转换。

## 使用约定与验证

`lfp.web.enabled=true` 默认生效。可提供 `JsonResultAdvice`、`GlobalExceptionHandler` 或名为 `lxmJsonModule` 的 Bean 覆盖默认实现。默认全局异常处理覆盖 MVC 应用；需要限定包/业务规则时提供自己的 Handler Bean。内部错误日志保留栈，客户端只收到固定消息，业务 AppException 消息保留。`HttpClient.useCache(true)` 仅缓存无 body、无 header/cookie 的 GET；带身份请求不缓存，非 2xx 和解析失败抛出异常，响应资源自动关闭。`EntityUtils` 转换失败抛错，不返回部分结果。测试：旧八组 JSON、过滤范围、序列化异常和真实 HTTP 缓存/编码。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
