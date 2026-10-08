# lxm-framework-common

`common` 提供应用层基础类型与工具：业务返回 `Result<T>`、分页接口、可信身份接口、内存缓存，以及字符串、日期、集合和加密辅助工具。它不依赖 Spring 或 Servlet，适用于服务层、批处理和普通 Java 程序。

## 引入依赖

要求 JDK 25。先按[项目说明](README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-common</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 业务返回值

```java
import com.lxm.framework.common.web.Result;

Result<String> success = Result.of("已保存");
Result<String> failure = Result.of(10001, "订单不存在", null);
boolean passed = success.success();
String value = success.getReturnValue();
```

`Result` 使用 `code/message/returnValue`，成功码为 `0`。它适合内部服务返回；对外 REST 接口需要 `code/message/data/sign` 时使用 [web 的 JsonResult](lxm-framework-web/README.md)，或调用 `JsonResult.of(result)` 显式转换。

## 有过期时间的内存缓存

```java
import com.lxm.framework.common.cache.customized.impl.CacheBuilder;
import java.util.concurrent.TimeUnit;

try (var cache = CacheBuilder.<String, String>newTimeCache(128, 1, TimeUnit.SECONDS)) {
    cache.put("verification:42", "value", 5, TimeUnit.MINUTES);
    String value = cache.get("verification:42");
    String once = cache.getThenRemove("verification:42");
    long remainingSeconds = cache.getTimeout("verification:42");
}
```

构造参数依次为初始容量、扫描周期和扫描时间单位；每个 `put` 单独指定有效期。`getThenRemove` 原子地读取并删除，同一键只会被一个调用取得。过期值在读取时也会检查，不能通过读取刷新已经过期的条目。

长期使用时，将缓存声明为应用拥有的资源，在组件关闭时调用 `close()`；不要每次业务调用都创建扫描线程。扫描线程为 daemon。`expireListener((key, value) -> ...)` 可接收过期通知，回调应保持简短。

不需要过期的缓存可以使用：

```java
var cache = CacheBuilder.<String, String>newPermanentCache(128);
cache.put("configuration", "value");
```

TTL 约定：缺失返回 `-1`，永久条目返回 `0`，其余返回剩余秒数。初始容量不是最大条目数；应用仍需要控制内存占用。这是进程内缓存，多个应用实例之间不会同步。

## 可信身份

`StandardPrinciple` 描述账号、用户和租户等身份字段。`PrincipleProvider.current()` 用来获得当前已经认证的身份；MyBatis 模块通过该接口进行身份填充与租户隔离。

```java
import com.lxm.framework.common.principle.PrincipleProvider;
import com.lxm.framework.common.principle.StandardPrinciple;

// 在已有认证流程中构造并持有身份；不要用客户端自报的 userId/tenantId。
StandardPrinciple identity = new StandardPrinciple(42, "account", 7, "user", 3);
PrincipleProvider provider = () -> identity;
```

在 Web 应用中，提供器应读取应用的请求认证上下文；上面的固定对象只是 API 用法，不适合多用户请求。异步任务需要显式传递经过校验的身份。

## 分页与常用工具

`StandardPage<T>` 是分页适配接口，应用实现当前页、每页数量、排序字段、总数和记录列表等方法即可接入 Mongo 分页。MyBatis 的 `Pagination` 也实现该接口。

通用工具集中在 `com.lxm.framework.common.utils`：`DateTimeUtils`、`ListUtils`、`StringFormatUtils`、`ObjectMapUtils`、`CryptoUtils` 等。按具体方法的输入、单位与异常约定调用；涉及前后端请求认证时使用 Enigma，不能用 MD5 摘要替代消息认证。

## 注意事项

- `common` 不自动创建 Spring Bean；在需要的应用组件中明确管理资源。
- 内存缓存不提供分布式一致性，也不是持久化存储。
- `StandardPrinciple` 的用户、账号、租户标识为整数；需要其他身份模型时，由应用适配提供器或使用独立接口。
- `AppException` 的消息可能对客户端展示，业务异常中不要放入内部配置或敏感信息。

回到[项目接入说明](README.md)。
