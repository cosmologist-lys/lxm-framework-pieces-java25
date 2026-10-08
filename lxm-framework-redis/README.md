# lxm-framework-redis

Redis 存储、锁与消息。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-redis:2.0.0-java25-SNAPSHOT`。

## 功能与接入

显式 `lfp.redis.enabled=true`；读取 `spring.data.redis.host/port/database/username/password/timeout`。默认 host=localhost、port=6379、database=0、timeout=10s。Bean 名为 lxmRedisConnectionFactory/lxmRedisTemplate/lxmRedisLocker/lxmRedisContainer；同名用户 Bean 优先。

## 使用约定与验证

对象键及锁键使用 `lfp.redis.namespace`，默认 `lfp:v2:`，hash field 使用普通字符串；值为无 default typing 的 JSON，读取 Object 返回 Map/List。`RedisClient.storage()/locker()/messageBus()` 保留静态入口，一个 JVM 只支持一组配置。锁用随机所有权值与 Lua 比较删除，任务异常 finally 释放，等待中断传播；锁租约不是业务 fencing，超期任务仍可能运行，需要业务幂等或使用方 fencing。消息订阅由 Spring 容器统一停止；once 使用原子去重并在回调异常后移除监听器。永久订阅随容器关闭；发布与 topics builder 不应跨线程共享。集成测试需 Redis，详见验证文档。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
