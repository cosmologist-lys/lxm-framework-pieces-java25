# lxm-framework-common

基础类型、身份与缓存。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-common:2.0.0-java25-SNAPSHOT`。

## 功能与接入

提供 `Result<T>`、`JsonResultInterface<T>`、`StandardPage<T>`、`StandardPrinciple`、`PrincipleProvider`、通用工具及 Caffeine/TimeCache。无 Spring/Servlet 依赖。身份由应用提供，MyBatis 不再使用固定 tenant/user=1。

## 使用约定与验证

`TimeCache` 实现 AutoCloseable，应用负责 `close()`；扫描线程为 daemon。到期键首次访问即删除并通知一次，实例操作同步；`getThenRemove` 原子。TTL：缺失 -1，永久 0。通用 CryptoUtils 是既有工具入口；Enigma 使用自己的四方向密钥和协议，不能用 MD5 工具充当协议认证。测试：`CacheExpiryTest`。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
