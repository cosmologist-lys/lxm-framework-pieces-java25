# lxm-framework-web

`web` 为 Spring MVC REST 应用提供 `JsonResult<T>`、结果字段过滤、统一异常响应、Jackson JSON/XML 转换，以及 HTTP 和文件工具。引入模块后自动配置默认启用，无需额外扫描框架包。

## 引入依赖

要求 JDK 25。先按[项目说明](../README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-web</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 返回 JsonResult

```java
import com.lxm.framework.web.jsonresult.JsonResult;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ProfileController {
    @GetMapping("/profile")
    public JsonResult<?> profile() {
        return JsonResult.json(0, "", Map.of("name", "中文", "id", 42));
    }
}
```

响应包含 `code/message/data`；只有 `sign` 有非空值时才写出该字段。`JsonResult.json()` 创建成功且数据为空的结果；`JsonResult.json(code, message, data)` 创建自定义结果。`JsonResult.of(Result<T>)` 把服务层结果显式转换为 HTTP 返回类型。

`JsonResult.json(Map<String, Object>)` 把 Map 中的 `code/message` 提取为状态，其余条目放入 `data`，并复制输入 Map。需要完整 Map 作为数据时，使用三参数工厂。

## 限制数据字段

```java
import com.lxm.framework.web.jsonresult.annotation.JsonResultFilter;

record Profile(String name, String secret) {}

// 放在 Controller 方法上；过滤 Profile 类型的业务数据。
@JsonResultFilter(
        type = Profile.class,
        include = {"name"})
@GetMapping("/public-profile")
public JsonResult<?> publicProfile() {
    return JsonResult.json(0, "", new Profile("中文", "internal"));
}
```

也可以对单个结果调用 `include(type, fields)` 或 `exclude(type, fields)`。过滤只在当前结果的序列化范围内生效，不修改共享 mapper，也不修改调用方对象。

启用 Enigma 时先完成业务数据过滤，再加密或签名；外层协议字段由 Enigma 管理。

## Jackson 与数据转换

`com.lxm.framework.web.jackon.FrameworkJackson.mapper()` 提供框架的不可变默认 Jackson 3 mapper，可读取/写出 JSON，支持 `java.time` 日期类型。`LocalDateTime` 使用 `yyyy-MM-dd HH:mm:ss` 格式。`EntityUtils` 提供 JSON/XML 及对象转换入口；转换失败会抛出异常，调用方需明确处理。

```java
import com.lxm.framework.web.jackon.FrameworkJackson;
import java.util.Map;

String json = FrameworkJackson.mapper().writeValueAsString(Map.of("name", "中文"));
```

Jackson 3 的核心 API 位于 `tools.jackson.*`，共享注解仍位于 `com.fasterxml.jackson.annotation.*`。使用项目 parent 的 BOM 配套版本，避免缺失注解类等运行时冲突。

## 调用 HTTP 服务

```java
import com.lxm.framework.web.http.HttpClient;
import java.util.Map;

Map<?, ?> response =
        HttpClient.get(
                "https://api.example.com/search",
                Map.of("keyword", "中文", "page", 1),
                Map.class);
```

常用入口为 `get/post/put/execute`，支持 `Class<T>` 和 Jackson `TypeReference<T>`。参数按 UTF-8 编码；响应资源由工具关闭。非 2xx 响应和 JSON 解析失败会抛出异常，不返回部分成功结果。

`HttpClient.useCache(true)` 是进程范围的缓存开关，默认关闭。仅无 body、无 header、无 cookie 的 GET 请求可以缓存；带身份请求和写操作不会进入该缓存。缓存不能替代外部服务自身的访问规则。

## 配置与扩展

```yaml
lfp:
  web:
    enabled: true
```

| 可替换组件 | 替换方式 |
| --- | --- |
| `JsonResultAdvice` | 提供同类型 Bean |
| `GlobalExceptionHandler` | 提供同类型 Bean，定义自己的异常范围和状态规则 |
| `lxmJsonModule` | 提供同名 Jackson Module Bean |

默认异常处理：参数绑定/校验失败为 HTTP 400；未处理内部异常为 HTTP 500，客户端得到固定消息；`AppException` 保留业务码和消息，HTTP 状态为 500。鉴权异常按其类型返回对应状态。业务码与 HTTP 状态分别处理，不能仅依据 HTTP 200 判断成功。

## 注意事项

- 本模块面向 Servlet Spring MVC，不提供 WebFlux 接入。
- 普通 `Result<T>` 的字段是 `returnValue`，不会自动转成 `JsonResult.data`。
- 默认全局异常处理覆盖 MVC 应用；已有自己的异常体系时应提供替换组件或关闭相应集成。
- mapper 的共享配置不可在一次请求中修改；单次字段筛选使用 `JsonResult` 的过滤入口。
- 远程 URL 必须由应用决定和校验，HTTP 工具不替代目标授权或 SSRF 限制。

回到[项目接入说明](../README.md)。
