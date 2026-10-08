# Enigma v1 协议

适用 Java 21/25 与浏览器 Web Crypto。算法为 AES-256-GCM（12 字节 IV，16 字节 tag）和 HMAC-SHA-256（32 字节 key/tag）。四把独立随机密钥分别用于 requestEncryption/responseEncryption/requestSigning/responseSigning。密码功能不替代 HTTPS 与业务鉴权；密钥端点和业务保护接口都要求 TLS，只有显式 allow-insecure-localhost 的回环开发环境例外。

## 接入与支持边界

```yaml
lfp:
  enigma:
    enabled: true
    store: redis
    redis-uri: ${ENIGMA_REDIS_URI}
    redis-namespace: lfp:enigma:v1:epoch-1:
    active-wrapping-kid: wrap-1
    wrapping-keys:
      wrap-1: ${ENIGMA_WRAPPING_KEY}
```

单独使用 enigma 的 Redis 适配需引入 Jedis，版本跟随 Boot BOM；all 已包含该依赖。每个 wrapping key 为不带 padding 的 Base64URL 32 字节随机值，由部署环境提供，不能使用测试 vectors 中的 key。

提供 `EnigmaIdentityResolver` Bean，从已验证且仍有效的登录状态返回 `EnigmaIdentity(userId,tenantId,loginSession,cookieAuthentication,csrfToken)`。user/tenant/loginSession 必须来自服务端可信上下文，每次请求重新检查鉴权。loginSession 每次重新登录生成新的随机标识；不要将请求头中的 user/tenant 直接当作身份。与 lfp auth 的集成由应用实现，框架不假设特定登录协议。

```java
@EnigmaProtected(EnigmaProtected.Mode.ENCRYPT)
@PostMapping(path="/orders", consumes="application/json", produces="application/json")
JsonResult<?> create(@Valid @RequestBody CreateOrder body) { /* 业务实现 */ }
```

SIGN 无 body，可用于 GET/DELETE 或无 body 的 POST/PUT/PATCH；ENCRYPT 必须一个 @RequestBody。服务端注解固定策略，客户端不能通过删除 body 切换。返回类型只支持同步 JsonResult<?> / ResponseEntity<JsonResult<?>>，输出必须 JSON。启动时检查声明；HEAD、204/205/304、multipart、form body、文件、SSE、异步/流式响应不支持。动态返回无 body status 会被替换为协议失败；业务已经准入，不能据此重试写入。应用应避免在保护接口使用这些返回形式。

使用 lfp.enigma.store=memory 时限定开发单实例，启用必须明确选择，不会在 Redis 故障时退回内存。缺少身份 SPI/存储或不支持的接口契约，启动失败。普通未注解接口无需 Enigma，也不会被加密。

## 密钥端点与生命周期

| 方法与路径 | 请求 | 响应 |
| --- | --- | --- |
| POST /enigma/session | 已登录凭证，无业务 body | Grant：v/sid/kid/serverTime/refreshAt/expiresAt/keys |
| POST /enigma/session/refresh | JSON `{"sid":"...","currentKid":"..."}` | 新 Grant；同一旧 kid 宽限期内重试返回同一新 kid/keys |
| POST /enigma/session/revoke | JSON `{"sid":"..."}` | 普通 JsonResult 成功；该 SID 停止新准入 |

Grant 时间为十进制 Unix 毫秒字符串。sid/kid 为随机 16 字节 Base64URL，长度 22。keys 是四个上述名称的 Base64URL 原始 key，只有在已认证 HTTPS 下交付，不做 localStorage/cookie 持久化、不进入日志。服务端响应 Cache-Control=no-store。

默认 key TTL 15 分钟、建议刷新时间 12 分钟、旧 key 新准入重叠期最多 2 分钟、响应保留期 5 分钟。客户端使用 serverTime 计算时钟偏差，refreshAt 前主动刷新；4096 请求额度耗尽也需要刷新。SDK 提供显式 refresh，不自动重试业务请求。同一 SID 最多保存 16 个有效/响应保留版本，达到容量拒绝，不能淘汰有效重放记录。

已准入请求携带不可变 key snapshot，刷新或撤销后可以在原 key 响应保留窗口内完成响应保护。重新 lookup/admit 禁止使用已撤销 key。登录注销先使 token/session 无效，再调用 `EnigmaSessionService.revokeIdentity(identity)`；该登录身份所有 SID 及新建 SID 都拒绝，重新登录必须用新的 loginSession。

Cookie 登录在三个密钥端点都要求可信 X-CSRF-Token，常数时间比较；Origin 必须同源或在精确 allowed-origins 中。业务请求还需要密码认证及 nonce 校验。代理终止 TLS 时使用容器可信代理配置提供 isSecure/Host，不能信任任意客户端 Forwarded/X-Forwarded 头。

## 请求认证数据

每个保护请求提供以下唯一 header，重复或缺失拒绝。所有值最大 256 字符：

| Header | 值 |
| --- | --- |
| X-Enigma-Version | 1 |
| X-Enigma-Mode | SIGN 或 ENCRYPT，与服务端注解一致 |
| X-Enigma-Session | sid |
| X-Enigma-Kid | kid |
| X-Enigma-Timestamp | 正十进制 Unix 毫秒，无前导零 |
| X-Enigma-Nonce | 新的随机 16 字节 Base64URL |
| X-Enigma-Sign | 仅 SIGN 请求，32 字节 HMAC Base64URL |

认证对象 R 字段如下，使用 RFC 8785 JCS 编码为 UTF-8，无 BOM、无空白：

```json
{"v":1,"mode":"SIGN","direction":"request","sid":"...","kid":"...","method":"PUT","path":"/demo/params","params":{"q":["中文","+",""]},"contentType":"","timestamp":"...","nonce":"..."}
```

JSON 示例字段显示顺序不等于最终 JCS 字节顺序，实际按 UTF-16 key 排序；不能依赖业务 Map 的遍历顺序。method 为大写，path 使用未经百分号解码的 requestURI，包含 context path；可信配置 public-path-prefix 可补代理移除的路径前缀，无自动转发头推断。SDK 签名外部 URL.pathname。

params 包含全部 query 名和值，重复值顺序保留，空值和 bare 参数均为空字符串；+ 解码为空格，%2B 为加号；百分号与 UTF-8 严格校验。不同名字按 JCS 排序，单个名字的值顺序不能改变。query 最大 8192 字符、128 名、256 值。协议元数据在 header，不从 params 移除任何名字。

contentType 没有 header 时为空字符串；JSON/UTF-8 统一为 application/json。ENCRYPT 要求 JSON UTF-8；Content-Encoding 仅接受缺省或 identity。SIGN 无实际 body，包含 chunked body 也拒绝。

### SIGN

`X-Enigma-Sign = Base64URL(HMAC(requestSigning, JCS(R)))`。参数保留明文。后端认证成功后原子准入，再执行 Controller。

### ENCRYPT

请求 body 直接是以下四字段信封（没有额外 data 外壳）：

```json
{"v":1,"kid":"...","iv":"...","ciphertext":"..."}
```

IV 为新的随机 12 字节，ciphertext 为 AES-GCM 密文与 tag 拼接，AAD=JCS(R)，plaintext 为业务 JSON 的 UTF-8 字节。v/kid 必须与 header 匹配，不接受未知字段、重复 JSON key、尾随文本、非法 UTF-8/Unicode。GCM 成功后先原子登记 nonce/IV 与额度，再检查明文 JSON 和 DTO；无论随后 DTO/JSON 校验结果如何，nonce 都已消耗。

业务 JSON 和协议使用有限双精度数字，数学上的整数必须在浏览器安全整数 ±(2^53−1) 范围内。更大 ID/金额用字符串；包括 `1e30` 这样的整数型数值也禁止出现在 Enigma 业务数据中。普通非 Enigma JSON 不受此约束。JCS 算法固定向量另覆盖 RFC 的 IEEE-754 表示案例，不将它们误当成可接受的业务 ID。

## 响应保护

先由应用 mapper/JsonResultAdvice/字段过滤完成业务序列化，最外层 Filter 缓存有界 JSON，保护一次。外层 code/message/data 保留，业务 sign 改为协议保留字段。code=0 成功，非零业务码；客户端完成密码验证后才读取 message/data。

返回 header：X-Enigma-Version/Session/Kid/Timestamp/Nonce/Mode/Unexecuted。其中 timestamp/nonce 为新的响应值，Session/Kid 必须对应原请求；Mode 为 SIGN 或 ENCRYPT。

认证对象 A：

```json
{"v":1,"request":R,"direction":"response","timestamp":"...","nonce":"...","status":201,"code":0,"message":"","protection":"ENCRYPT","unexecuted":false}
```

A 中 request 是完整原请求 R；status 为实际 HTTP status。`unexecuted=true` 只表示尚未通过准入，false 表示已准入，**不承诺业务执行成功、事务提交或 Controller 已调用**；DTO 失败也可能为 false。客户端不得据此推断写入可重试。

ENCRYPT 请求且最终 data 非 null 时：data 变为四字段信封，AAD=JCS(A)，plaintext=JCS(已过滤 data)，响应使用 responseEncryption。ENCRYPT 请求的 null data 和所有 SIGN 响应：A 添加最终 data 字段，`sign=Base64URL(HMAC(responseSigning,JCS(A)))`；protection/Mode 为 SIGN。非零业务错误的非空 data 同样加密。

wire 响应本身为 JCS JSON，SDK 要求 canonical(parsed)==raw，拒绝重复字段/尾随文本。HTTP status、code/message、保护模式、请求 path/params/nonce 或响应值被篡改均导致认证失败。SDK 每个 prepared 请求只消费一次响应，旧 kid 在途请求用旧 keys 校验。

无法取得可信 key context 或密码认证失败时返回 HTTPS 明文协议错误，Mode=NONE、Unexecuted=true；不可作为已认证业务数据。身份/存储错误可能发生在 Controller 前，也可能在响应预留时，业务调用应采用独立幂等机制。

## 原子准入与存储

准入在同一存储事务内重新检查身份、撤销、有效期、时钟、nonce、IV 和请求计数。仅完成 lookup 不构成授权。nonce TTL 默认 180 秒，至少覆盖两倍时钟偏差与处理余量；IV 登记保留至整把 key 生命周期及响应窗口结束，不能随 nonce 过期删除。

默认 max-uses-per-key=4096，每个 AES 方向登记 IV，硬上限 2^20；响应 IV 在加密前原子预留。随机碰撞会拒绝该次加密，使用方重建响应/刷新，绝不重复使用。nonce 容量默认 4096；活跃记录满时 429，不做驱逐。max-sessions=1000 是 memory store 全局界限；Redis 使用每身份 sessions-per-minute=5 控制签发，实例容量由 Redis 内存、限流与应用监控管理，不声称 max-sessions 是 Redis 全局 quota。

MemorySessionStore 同步事务并在失败时回滚副本。RedisSessionStore 使用带 revision 的加密状态与 Lua CAS，多个进程竞争同一 nonce 只有一方准入；最多 32 次冲突重试，失败返回 503。action 可能重试，不得在其中执行业务副作用。Redis 状态以每条记录随机 salt 经 HKDF-SHA-256 派生 AES wrapping 子密钥，随机 IV；AAD 绑定 purpose/sid/revision/wrappingKid。主 wrapping key 不存 Redis。

仅支持 **Redis Standalone**，部分 Lua 同时访问 SID 和身份撤销 marker，不能在 Redis Cluster 跨 slot 使用。Redis 不得驱逐会话/重放状态；建议独立实例、持久化和 noeviction。状态丢失/回滚后不能继续沿用旧 epoch：切换 redis-namespace 与 wrapping key 版本，要求客户端重新握手。未验收自动故障切换的强一致性，不提供“恰好执行一次”业务保证。

wrapping key 轮换：所有实例先具备新旧 key，切换 active-wrapping-kid 为新版本；新写记录使用新 key，旧版本保留至少最长 session/key/response 窗口后移除。移除仍在使用的 wrapping key 会返回 503，禁止明文读取或弱化认证。响应加密额度/存储错误回退为可认证 SIGN 错误，不返回未保护业务 data。

## 错误与部署

| code | HTTP | 含义 |
| --- | --- | --- |
| 6200 | 400 | 协议/格式/响应契约不合法 |
| 6201 | 401 | key/session 不存在，重新握手 |
| 6202 | 401 | 时间或 key 已过期 |
| 6203 | 400 | GCM/HMAC 认证失败 |
| 6204 | 409 | 重放 |
| 6205 | 429 | 生命周期容量或使用量拒绝 |
| 6206 | 403 | 身份、TLS、Origin 或 CSRF 不符 |
| 6207 | 503 | 共享存储不可用、记录篡改、CAS 重试耗尽 |

默认 max-body-bytes=1048576、max-envelope-bytes=1572864；可配置明文最大 10 MiB，信封最大 16 MiB，至少容纳 Base64 膨胀与外层字段。响应和请求均有界，禁止流式保护。

跨域时配置精确 allowed-origins，框架暴露所有 X-Enigma 响应头；不要让其他 CORS 配置遮盖它。部署代理禁止缓存密钥与保护响应，限制请求大小/速率/超时，禁止记录 keys、wrapping key 或解密 data。SDK 默认 15 秒超时、禁止跨源请求/重定向；本示例不包含重试写入策略或业务幂等存储。

[跨语言向量](../examples/enigma-browser/vectors.json)、[可执行示例](../examples/enigma-browser/README.md)、[验证](verification.md)。标准依据：[Web Crypto](https://www.w3.org/TR/WebCryptoAPI/)、[RFC 8785](https://www.rfc-editor.org/rfc/rfc8785)、[RFC 5869](https://www.rfc-editor.org/rfc/rfc5869)、[NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final)。
