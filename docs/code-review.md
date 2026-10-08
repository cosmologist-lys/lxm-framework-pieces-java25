# Java 25 独立审查

日期：2026-10-08。对已验收 Java 21 基线的 Maven 坐标、release、处理器、CI、文档与 JDK 25 运行差异独立复核。所有 Java 源码逐文件相同，协议/浏览器源码与固定向量逐字节一致。实际 JDK 25 clean verify -Pintegration 通过 31 Test + 6 IT，0 失败/错误/跳过；JDK 25 启动的服务通过 Chromium Web Crypto 测试。未借用 Java 21 的测试结果作 Java 25 运行结论。

jdeps 未发现项目 JAR 使用 JDK internal API，jdeprscan --for-removal 未列出项目待移除 API。依赖层 Lombok Permit、BeeCP 原子字段更新器仍调用 sun.misc.Unsafe::objectFieldOffset，JDK 25 打印废弃警告；所选为当前已核实稳定版本，当前构建/真实连接池测试成功，未用 JVM 选项隐藏警告。它们的未来 JDK 兼容性需跟随上游。

以下为共享逻辑审查记录，已从 Java 21 同步，并在 Java 25 独立执行对应测试。

## 共享逻辑审查与修复

日期：2026-10-08。范围为完整模块依赖与自动配置、公开入口，以及缓存/MVC/Jackson/HTTP、auth、Redis 锁与订阅、MyBatis 租户/分页/事务、Mongo CRUD、Excel 资源与多 sheet、邮件配置与 MIME、Enigma 协议/存储/MVC/浏览器。以下为有代码证据且已落实的修复，不表示第三方库或任意应用集成都不存在其他问题。

| 问题 | 影响与修复 | 回归证据 |
| --- | --- | --- |
| parent 未纳入 reactor，版本跨代覆盖 | 根与子相对父引用，统一 Boot BOM，显式处理器 | 三个 Boot 检查点、effective POM、依赖树 |
| 自动配置依赖扫描/隐式服务 starter | AutoConfiguration.imports、显式资源开关、用户 Bean 退让 | all 外包名应用真实启动，无服务 |
| 缓存过期被读到、扫描删除替换值 | 同步实例操作，原子 get/remove，扫描复核，close | CacheExpiryTest |
| JsonResult 修改调用方 Map、过滤污染 mapper、写出异常吞掉 | 输入复制，context 范围过滤，finally 恢复，异常传播 | 八组旧样本、不可变 Map、失败后正常序列化 |
| HTTP 带凭据共享缓存，响应不关闭/参数不编码 | 限制无身份 GET 缓存，关闭响应，UTF-8 编码/状态抛错 | 真正本地 HTTP 服务 |
| 通用异常泄漏内部 message | 固定客户端消息，业务码保留，服务端日志 | 实际 HTTP 500/400 验证 |
| Auth Filter 输出无效 JSON，缺少策略仍注册 | 策略 Bean 条件、明确 JSON/status；资源由 Spring 关闭 | AuthRegressionTest |
| Auth 删除后更新可能复活、TTL 单位不统一 | SET XX/expire/persist，秒单位；不再创建泄漏的子 context | 两实例 AuthRedisIT、旧 Session fixture |
| Redis 任意类型反序列化 | 去 default typing、新 key namespace、明确 Map/List | 原生 Redis 检查与旧 key 隔离 |
| 锁释放未校验所有者、任务抛错不释放 | UUID SET NX TTL + Lua compare-delete + finally | 真实锁租期/旧所有者/失败任务测试 |
| once 订阅并发重入、回调失败未移除 | 原子一次标记与 finally 移除，重复 subscribe 幂等 | 容器生命周期 IT；并发语义为静态审查 |
| MyBatis 固定身份、OR 绕过、旧解析/删除 API | 可信 PrincipleProvider、维护中的 AST、先隔离再 count、标准逻辑删除 | H2 OR/逻辑删除、真实 MySQL count/填充/回滚 |
| 自定义租户 INSERT/UPDATE 绕过 | 显式 VALUES/租户绑定校验；禁止改归属/INSERT SELECT/upsert | AST 负向场景与 MySQL insert |
| deletedTime 错误写入 String | 改 LocalDateTime sentinel | 真实 MySQL INSERT/事务测试 |
| Mongo 错误配置条件、ObjectId 标记 INT32、分页 Query 被修改 | 显式开关、正确 ObjectId、独立页查询/总数、字面 like/单范围 | 真实 Mongo CRUD/批量/排序/重复分页 |
| Excel 漏末行/空格 NPE/资源泄漏/并发 Workbook | 检测格式，末行与空值处理，关闭资源，顺序多 sheet；移除已废弃 AbstractXlsxView/SecurityManager | 真实 xlsx 重读、20 sheet、公式文本 |
| 邮件共享 Properties 凭据/静态收件 Store/多个收件地址错误 | 复制配置、Authenticator、实例资源、READ_ONLY、UTF-8/地址连接、TLS 主机检查 | 本地 SMTP multipart/附件/中文及配置测试 |
| Enigma nonce/IV 生命周期、刷新/撤销竞争 | 原子准入/CAS、旧 key snapshot、IV 整窗保留、身份撤销 marker、wrap AEAD | 六组生命周期与两存储实例竞争/篡改 |
| Enigma MVC 顺序/错误和浏览器数值差异 | 启动契约检查、TLS/Origin/CSRF、先解密/准入再 DTO、最后保护响应、严格 JCS | 真实 HTTP、RFC/HKDF 固定向量、Chromium Web Crypto |

## 仍需使用方遵守的边界

- AuthManager、RedisClient、MongoHelper 为兼容旧 API 保留静态入口，一个 JVM 多独立上下文需使用实例 API/自定义组件；不承诺跨 context 隔离。
- 默认 Auth 登录会话操作为多个 Redis 命令，同账号并发操作由应用串行化或替换实现。更新不复活已删除状态，但不承诺多命令事务。
- Redis 租约锁没有 fencing/自动续约，过期仍运行的业务必须另行控制。Enigma 的准入只防重放，不提供事务幂等。
- Enigma Redis 支持 Standalone；Cluster、异步复制回滚/故障切换强一致性未经验收。丢失状态必须切换 epoch 并重新握手。
- 邮件测试验证 SMTP/MIME，不验证供应商 OAuth、实际公网 TLS/IMAP；Excel 是有界内存处理，不提供大文件流式导出。
- 输入到自定义 SQL/公式/URL 附件仍需应用自己的授权与参数限制。框架不会替代业务访问规则。

构建与实际测试结果见 [verification](verification.md)；上述已确认缺陷已修复，列明的功能边界不是已实现的能力。
