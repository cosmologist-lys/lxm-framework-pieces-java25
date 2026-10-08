# 依赖与构建矩阵

锁定日期：2026-10-08。以 effective POM 为准。Boot 管理的 Spring/Jackson/Jedis 等跟随 4.1.1 BOM，不独立跨代覆盖；非管理库版本经 Maven Central metadata 核对。

| 坐标 | 最终版本 | 管理依据 |
| --- | --- | --- |
| com.alibaba:druid | 1.2.28 | Boot BOM 或 parent 明确锁定 |
| com.baomidou:mybatis-plus-spring | 3.5.17 | Boot BOM 或 parent 明确锁定 |
| com.fasterxml.jackson.core:jackson-annotations | 2.21 | Boot BOM 或 parent 明确锁定 |
| com.github.ben-manes.caffeine:caffeine | 3.2.4 | Boot BOM 或 parent 明确锁定 |
| com.github.chris2018998:beecp | 5.2.3 | Boot BOM 或 parent 明确锁定 |
| com.mysql:mysql-connector-j | 9.7.0 | Boot BOM 或 parent 明确锁定 |
| org.apache.commons:commons-lang3 | 3.20.0 | Boot BOM 或 parent 明确锁定 |
| org.apache.poi:poi | 5.5.1 | Boot BOM 或 parent 明确锁定 |
| org.eclipse.angus:angus-mail | 2.0.5 | Boot BOM 或 parent 明确锁定 |
| org.jsoup:jsoup | 1.23.2 | Boot BOM 或 parent 明确锁定 |
| org.projectlombok:lombok | 1.18.48 | Boot BOM 或 parent 明确锁定 |
| org.springframework.data:spring-data-mongodb | 5.1.1 | Boot BOM 或 parent 明确锁定 |
| org.springframework.data:spring-data-redis | 4.1.1 | Boot BOM 或 parent 明确锁定 |
| org.springframework:spring-core | 7.0.9 | Boot BOM 或 parent 明确锁定 |
| redis.clients:jedis | 7.4.1 | Boot BOM 或 parent 明确锁定 |
| tools.jackson.core:jackson-databind | 3.1.5 | Boot BOM 或 parent 明确锁定 |

平台：Java 25，Maven 3.9.16，Boot 4.1.1；MyBatis-Plus BOM 3.5.17、mybatis-spring 4.1.0、java-json-canonicalization 1.1、angus-activation 2.0.3。Compiler 3.16.0、Surefire/Failsafe 3.6.0、Wrapper 3.3.4，Lombok 1.18.48 显式处理器。浏览器 SDK 无运行时依赖，Playwright 1.64.0 为测试开发依赖，package-lock.json 固定解析结果。

旧 parent 使用 Boot 2.7.12，同时覆盖 Spring 5.3.16 / Context 5.0.4、Redis 3.0.5、Mongo starter 3.0.1 和 Jackson 2.13.1，已移除混合组合。JSON 注解包仍在 Jackson annotations 2.x，由 Jackson 3 BOM 正常管理。

检查点 Boot 3.5.16、4.0.8 在 Java 21 迁移过程中完成 package（跳过测试），Java 25 最终 4.1.1 独立完成测试。选择稳定、实际可解析版本，未显式选用 milestone/RC 或商业仓库；Jedis 7.4.1 传递依赖 redis-authx-core 0.1.1-beta2，该版本由稳定 Jedis 的 POM/BOM 带入，保留其认证 API 依赖并记录，不宣称所有传递依赖都为正式版。直接 MyBatis/Spring Data 集成保留资源显式开关；相关源码 API 变化见迁移说明。

[Boot 系统要求](https://docs.spring.io/spring-boot/system-requirements.html)、[Central metadata](https://repo.maven.apache.org/maven2/)、[MyBatis-Plus](https://baomidou.com/getting-started/install/)、[Maven Wrapper](https://maven.apache.org/tools/wrapper/)、[JCS Java 实现](https://github.com/erdtman/java-json-canonicalization)。
