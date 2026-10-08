# lxm-framework-redis

`redis` 为 Spring 应用提供 JSON 对象存储、带租期的互斥锁和 Redis 发布订阅。底层由 Spring Data Redis 与 Jedis 驱动管理连接；Spring 关闭应用时会关闭连接工厂和监听容器。

## 引入依赖

要求 JDK 25。先按[项目说明](../README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-redis</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 配置连接

```yaml
lfp:
  redis:
    enabled: true
    namespace: "lfp:v2:"
spring:
  data:
    redis:
      host: ${REDIS_HOST:127.0.0.1}
      port: 6379
      database: 0
      username: ${REDIS_USERNAME:}
      password: ${REDIS_PASSWORD:}
      timeout: 10s
```

模块默认关闭。启用后的默认连接为 localhost:6379、database=0、timeout=10s。当前自动配置连接单个 Redis 实例；其他拓扑由应用提供连接工厂并独立验证。

## 存储 JSON 数据

```java
import com.lxm.framework.redis.impl.RedisClient;
import java.util.Map;
import java.util.concurrent.TimeUnit;

var item =
        RedisClient.storage()
                .prefix("profile:")
                .key("42")
                .expire(10, TimeUnit.MINUTES)
                .use();
item.store(Map.of("name", "中文", "enabled", true));
Map<?, ?> value = item.get(Map.class);
```

namespace 是整个模块的物理键前缀，`prefix` 是业务键前缀。每次操作创建新的 builder；不要在多个线程间共享可变 builder。

`persist()` 设置永久存储；`StoreSession` 提供 `exist()`、`expire()`、`expireAt()`、`getList()` 等入口。`getThenDelete` 是读取后删除入口，不作为跨实例的一次性原子准入；需要防重放使用 Enigma 或自己实现原子操作。`listener` 仅处理本框架主动删除通知，不代表 Redis 自然过期通知。

值为不带任意类型信息的 JSON。读取为 `Object` 时得到 Map/List；已知类型用 `get(MyType.class)`。字符串 hash field 按普通字符串编码。

## 执行带锁任务

```java
var lock =
        RedisClient.locker()
                .prefix("order:")
                .key("42")
                .expire(30, TimeUnit.SECONDS)
                .duration(50, TimeUnit.MILLISECONDS)
                .use();

lock.accessResourceThenRelease(
        () -> {
            // 更新订单；业务自身仍需幂等控制。
        });
```

`expire` 是锁租期，`duration` 是等待时的重试间隔。锁以随机所有权值获取，释放时校验所有者；任务正常完成或抛出异常均会释放当前租约。

如果需要立即返回，使用 `tryLock()`，并只在成功取得锁后在 `finally` 中调用 `releaseLock()`。等待被中断时会保留中断状态并中止获取。

租期到期后另一个调用者可以获得锁，即使前一个业务仍在运行。本模块不提供 fencing token 或自动续约；任务执行时间、数据库写入一致性和重复请求必须由业务控制。

## 发布与订阅

```java
RedisClient.messageBus()
        .subscriber()
        .once()
        .topic("order-events")
        .consumer(
                (topic, payload) -> {
                    // 处理一次消息；payload 使用 JSON 数据模型。
                })
        .subscribe();

RedisClient.messageBus()
        .publisher()
        .topic("order-events")
        .payload(Map.of("orderId", "42"))
        .publish();
```

`once()` 在首次回调后移除监听，回调抛错也会移除；长期订阅使用 `persist()`，随 Spring 监听容器关闭。Redis Pub/Sub 不持久化消息，订阅尚未建立或断线时可能漏收；需要可靠投递时使用具有相应保证的消息系统。

## 自定义组件与注意事项

自动配置 Bean 名称为 `lxmRedisConnectionFactory`、`lxmRedisTemplate`、`lxmRedisLocker` 和 `lxmRedisContainer`，应用可提供同名组件。模板分别负责对象存储和字符串锁键。

- `RedisClient` 是静态便利入口，按一个 JVM 单套配置使用；多独立上下文请注入模板并创建自己的实例组件。
- namespace 应按应用和用途区分，避免业务键碰撞；对象和锁均使用配置前缀。
- Redis 锁、缓存和 Pub/Sub 的行为随连接、租期和服务故障而变化，不替代业务事务。
- `deleteBatch()` 涉及前缀匹配批量删除，先确认业务键范围，避免对共享 Redis 做过宽的匹配。
- 通用 Redis、Auth Redis、Enigma Redis 各有独立配置，启用一个模块不会自动配置另外两个。

回到[项目接入说明](../README.md)。
