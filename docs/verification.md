# 验证记录与复现

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
