# 验证记录与复现

## 2026-10-08：格式、依赖与接入示例

使用 OpenJDK 25.0.4.1、Maven 3.9.16 和隔离的临时 Maven settings/缓存完成以下检查。此记录对应本次格式、依赖与 README 调整后的本机结果；GitHub Actions 结果按具体提交的运行记录判断。

| 检查 | 实际结果 |
| --- | --- |
| 全 reactor `clean verify -Pintegration` | 32 个普通测试 + 6 个集成测试，0 失败、0 错误、0 跳过 |
| 真实服务 | Redis 8.2.2、MySQL 8.4.11、MongoDB 8.0.24；升级后的 Jedis、MySQL/MongoDB 驱动通过测试 |
| 浏览器 | `npm run check` 与 Chromium/Playwright 端到端用例通过，覆盖协议互通及负向断言 |
| 外部依赖 | 全部 12 个 POM 的 45 个外部坐标，逐项核对 Maven Repository、Central 元数据与两版 effective POM |
| Java 格式 | 273 个源文件通过 google-java-format 的 dry-run 检查；与格式化后的旧提交相比，仅 5 个文件含 Jedis 8 API 适配 |
| 浏览器/工作流格式 | JS/MJS、HTML、CSS、YAML 通过固定 Prettier 3.9.9 的 `--check` |
| README Java 示例 | 9 个功能模块共 30 段示例用实际框架 classpath 编译成功；片段补入说明中标明的应用上下文/注入对象，不执行远程或业务副作用 |
| README JavaScript 示例 | 两个客户端接入示例通过 Node 语法检查 |
| 独立应用快速开始 | 原文 POM/启动类构建成功，打包应用实际启动；`/hello` 返回与 README 一致 |
| parent 模块版本 | 独立应用 `version=1.0.0-SNAPSHOT`，未显式写模块版本，实际解析 `lxm-framework-web:2.0.0-java25-SNAPSHOT` |
| 文档与兼容性基线 | 17 份 README 已重写；相对链接与 diff 空白检查通过；Java 17 的 240 个已跟踪文件 SHA-256 保持一致 |

依赖更新的版本与来源见[依赖版本表](dependency-matrix.md)，格式工具及复现参数见[格式说明](formatting.md)。Jackson 的两组 BOM 与共享注解同时对齐，Jedis 8 使用官方 `RedisClient` API；普通未签名 `JsonResult` 响应省略空 `sign` 字段。

## 既有验证记录

本机环境：OpenJDK 25.0.4.1、Maven 3.9.16。使用隔离 settings/缓存，未改动全局 Maven 配置。Java 17 原始源码与 Jackson 2.13.1 在临时项目生成八组返回 JSON 与一组 auth Session 样本，实际运行在 JDK 21；不据此声称完成 JDK 17 运行验证。

| 验证 | 已执行结果 |
| --- | --- |
| Boot 3.5.16 / 4.0.8 检查点 | Java 21 迁移基线的全 reactor package 成功，跳过测试；未在 Java 25 重复执行这两个过渡版本 |
| Boot 4.1.1 本机 JDK 25 reactor 验证 | 初次 clean verify -Pintegration 为 31 个 Test + 6 个 IT；补充普通异步 HTTP 回归后 verify -Pintegration 为 32 个 Test + 6 个 IT，0 失败、0 错误、0 跳过 |
| GitHub Actions JDK 25 clean verify -Pintegration | Temurin 25.0.4-1，32 个 Test + 6 个 IT，0 失败、0 错误、0 跳过；Chromium 用例通过 |
| 真实服务 | Redis 8.2.2、MySQL 8.4.11、MongoDB 8.0.24，本地临时独立服务 |
| 邮件 | 本地 SMTP 协议/MIME 接收、双收件人、UTF-8、附件 |
| 使用方 | 框架包外应用、实际嵌入式 Servlet HTTP、all 无服务启动 |
| 浏览器 | Chromium / Playwright 1.64.0，1 个端到端用例含九组协议断言；JDK 25 服务完成独立 Chromium 验证；JDK 21 另有 Codex 浏览器实测通过 |
| 依赖 | Java 21 基线的 effective POM、全 reactor dependency:tree 完成；Java 25 使用独立坐标在实际 JDK 25 完整构建 |

## 命令

```bash
./mvnw -B -ntp clean verify
# 需要以下测试端口的专用服务，缺失服务应失败，不能将跳过视为通过
./mvnw -B -ntp clean verify -Pintegration
cd examples/enigma-browser
npm ci
npx playwright install chromium
npm run check
npm test
```

服务属性：Redis `-Dlfp.redis.port=16379`，Mongo `-Dlfp.mongo.uri=mongodb://127.0.0.1:17017`，MySQL `-Dlfp.mysql.server=jdbc:mysql://127.0.0.1:13306/ -Dlfp.mysql.user=root -Dlfp.mysql.password=...`。默认本机 MySQL 测试密码为空，仅用于回环临时实例；CI 用公开测试密码。MySQL 测试创建独立 lfp_test_UUID 数据库并 finally 删除；Mongo 同样使用唯一数据库，Redis 使用 lfp:test:UUID 前缀。不要把集成测试指向业务数据库。

CI 用 `.github/workflows/ci.yml` 的专用服务容器，执行完整测试和 Chromium。2026-10-08 已完成的[验收运行](https://github.com/cosmologist-lys/lxm-framework-pieces-java25/actions/runs/37733020529)通过，覆盖源码提交 `d2fd287ff041546f47d29db9842bf28e24aa0bb4`。之后的提交按各自运行结果判断。

未执行公网邮箱登录/发送、Redis Cluster/故障切换、一致性压力或无限规模文件测试。部署支持边界见协议与模块 README。

Java 21 基线的空 Maven 本地缓存验证：从 fresh-repository 解析全部依赖，执行 `verify -DskipTests` 成功，不依赖已安装的旧 parent；此项验证解析与构建，未重复执行测试。

JDK 25 本身使用隔离共享依赖缓存独立 clean verify，所有本项目 25 坐标从源码 reactor 构建。不能把 Java 21 的空缓存检查算作 Java 25 的空缓存检查。

jdeps / jdeprscan（release 25，for-removal）完成：项目 JAR 无 JDK 内部 API、无列出的待移除 API；这不代表第三方依赖没有相关调用。文档相对链接检查通过。
