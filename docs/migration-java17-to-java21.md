# Java 17 → Java 21

原 Java 17 仓库 SHA `8d8ca6bd6d6f5aa30a5255f65c819251a532ec6d` 保持只读。新项目坐标由 1.0 改为 2.0.0-java25-SNAPSHOT；发布最终版需另行版本化，不能覆盖 1.0。

## 平台和 API 变化

- Boot 2.7.12 → 3.5.16 → 4.0.8 → 4.1.1，在 Java 21 工作区分别完成前两检查点的全 reactor package。最终 Jackson 3；注解仍为 com.fasterxml.jackson.annotation，其余 mapper/serializer 包 tools.jackson。
- Servlet、Validation、Mail、Activation、Annotation 改为 jakarta；Java SE javax.sql/javax.crypto 保留。应用不应混用旧 Servlet 类型。
- 自动配置由 AutoConfiguration.imports 加载。外部服务显式开关，配置细节见各模块 README。MyBatis-Plus 不使用 Boot starter，而组合 mybatis-plus-spring/jsqlparser/mybatis-spring；这是为了避免引入 starter 自动创建未配置数据源，已验证真实 MySQL 与 all 无服务启动。
- JsonResult filter 不再继承旧 SimpleFilterProvider；obsolete SimpleMixInResolver API 移除。JsonResult Serializer 用当前调用的 context，不修改共享 mapper。需要扩展 Jackson 的应用迁移到 Jackson 3 API。
- FrameworkDataSourceProperties 替代旧 Boot DataSourceProperties 参数；SqlInfo 替代已移除的第三方 SqlInfo 返回类型。BeeCP 包名 org.stone.beecp。ExcelView 继承 AbstractView，render 后关闭 Workbook。
- EntityUtils/HTTP/XML 转换失败抛异常，不返回部分对象或吞掉解析错误。Mongo like 改为字面匹配，save 仍为 insert；非法分页显式拒绝。
- 租户从 PrincipleProvider 解析，缺失即拒绝。tenant_id 不允许 UPDATE；受保护表 INSERT SELECT/upsert 被拒绝，自定义 SQL 应采用显式参数化 VALUES。不要从请求直接构造 SQL 或选择 escape mapper。

## 数据与返回格式

八组 JSON 样本来自未修改的旧源码与 Jackson 2.13.1，样本生成程序在 JDK 21 运行，未声称验收 JDK 17 运行环境。成功、失败、null、sign、日期、过滤、嵌套与 Result 结构均结构化回放。普通 Result 保留 returnValue；JsonResult 成功码仍为 0，非零业务码语义保持。

Redis 对象默认新命名空间 lfp:v2:；无任意类型反序列化，新 JSON 对象读为 Map/List。旧带类型元数据的缓存值不直接兼容；使用方选择过期自然淘汰或显式业务迁移，框架不清空旧数据。Auth session 使用明确 LxmSession 模型，独立旧 session 样本回归；动态属性仅承诺 JSON 值，不承诺恢复任意 Java 类型。Auth Redis 的 key 保持原约定，部署前隔离环境。

默认 MVC 内部异常 message 固定，服务端记录栈；AppException 保留业务消息。使用方可覆盖 GlobalExceptionHandler Bean 定制处理范围与追踪标识。

## Enigma 接入变化

普通接口关闭 Enigma 时保留原协议。受保护接口 sign 为框架保留字段；data 信封不再是原 DTO 的 wire 形状。字段过滤在加密之前执行。SDK 必须在认证成功后读取 code/message/data；NONE 响应仅按 HTTPS 协议错误处理，不能当作已认证业务结果。详见 [协议](enigma-protocol.md)。

## 官方依据

[Boot 3 迁移](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)、[Boot 4 迁移](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)、[Boot 4.1](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes)、[MyBatis-Plus](https://baomidou.com/getting-started/install/)、[Lombok](https://projectlombok.org/changelog)。
