# lxm-framework-all

完整依赖集合。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-all:2.0.0-java25-SNAPSHOT`。

## 功能与接入

无业务源码，仅聚合所有功能模块和 Enigma。适合需要多种能力的 Servlet 应用；可以改为只引入需要的模块降低依赖数量。引入 all 并不启用 Redis/MySQL/Mongo/Auth/Enigma。

## 使用约定与验证

普通 @SpringBootApplication 在无外部服务配置时应启动成功，框架包之外的使用方已有真实嵌入式服务器测试。资源客户端需要对应明确开关。可选 Jedis 因 redis/auth 已引用而在 all classpath 可用，单独使用 enigma+redis store 时需自行引入 Jedis。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
