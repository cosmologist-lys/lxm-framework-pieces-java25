# 依赖版本与稳定发布核查

核查日期：2026-10-08。覆盖两个仓库全部 12 个 POM 中声明的外部 parent、dependency 与 plugin，共 45 个不同坐标。先查看 Maven Repository，再使用 Maven Central 官方元数据和实际 effective POM 交叉核对；排除 SNAPSHOT、alpha、beta、milestone、RC 等预发布版本。内部 `com.lxm` 坐标是本项目制品，不作为第三方更新候选。

Java 21/25 使用相同的第三方稳定版本，仅 JDK release 和内部制品版本不同。表格按坐标展示 POM 实际使用的稳定版本；本表不声称所有未使用的 BOM 管理项或全部传递依赖都已独立升级。

| 外部坐标 / Maven Repository | 调整前 | 最新稳定 / 当前采用 | 官方元数据 |
| --- | --- | --- | --- |
| [cn.hutool:hutool-crypto](https://mvnrepository.com/artifact/cn.hutool/hutool-crypto) | 5.8.47 | 5.8.47 | [Central](https://repo.maven.apache.org/maven2/cn/hutool/hutool-crypto/maven-metadata.xml) |
| [cn.hutool:hutool-http](https://mvnrepository.com/artifact/cn.hutool/hutool-http) | 5.8.47 | 5.8.47 | [Central](https://repo.maven.apache.org/maven2/cn/hutool/hutool-http/maven-metadata.xml) |
| [com.alibaba:druid](https://mvnrepository.com/artifact/com.alibaba/druid) | 1.2.28 | 1.2.28 | [Central](https://repo.maven.apache.org/maven2/com/alibaba/druid/maven-metadata.xml) |
| [com.baomidou:mybatis-plus-bom](https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-bom) | 3.5.17 | 3.5.17 | [Central](https://repo.maven.apache.org/maven2/com/baomidou/mybatis-plus-bom/maven-metadata.xml) |
| [com.baomidou:mybatis-plus-jsqlparser](https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-jsqlparser) | 3.5.17 | 3.5.17 | [Central](https://repo.maven.apache.org/maven2/com/baomidou/mybatis-plus-jsqlparser/maven-metadata.xml) |
| [com.baomidou:mybatis-plus-spring](https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-spring) | 3.5.17 | 3.5.17 | [Central](https://repo.maven.apache.org/maven2/com/baomidou/mybatis-plus-spring/maven-metadata.xml) |
| [com.fasterxml.jackson.core:jackson-annotations](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations) | 2.21 | 2.22 | [Central](https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/maven-metadata.xml) |
| [com.github.ben-manes.caffeine:caffeine](https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine) | 3.2.4 | 3.3.0 | [Central](https://repo.maven.apache.org/maven2/com/github/ben-manes/caffeine/caffeine/maven-metadata.xml) |
| [com.github.chris2018998:beecp](https://mvnrepository.com/artifact/com.github.chris2018998/beecp) | 5.2.3 | 5.2.3 | [Central](https://repo.maven.apache.org/maven2/com/github/chris2018998/beecp/maven-metadata.xml) |
| [com.h2database:h2](https://mvnrepository.com/artifact/com.h2database/h2) | 2.4.240 | 2.5.252 | [Central](https://repo.maven.apache.org/maven2/com/h2database/h2/maven-metadata.xml) |
| [com.mysql:mysql-connector-j](https://mvnrepository.com/artifact/com.mysql/mysql-connector-j) | 9.7.0 | 26.7.0 | [Central](https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/maven-metadata.xml) |
| [io.github.erdtman:java-json-canonicalization](https://mvnrepository.com/artifact/io.github.erdtman/java-json-canonicalization) | 1.1 | 1.1 | [Central](https://repo.maven.apache.org/maven2/io/github/erdtman/java-json-canonicalization/maven-metadata.xml) |
| [jakarta.activation:jakarta.activation-api](https://mvnrepository.com/artifact/jakarta.activation/jakarta.activation-api) | 2.1.4 | 2.1.4 | [Central](https://repo.maven.apache.org/maven2/jakarta/activation/jakarta.activation-api/maven-metadata.xml) |
| [jakarta.annotation:jakarta.annotation-api](https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api) | 3.0.0 | 3.0.0 | [Central](https://repo.maven.apache.org/maven2/jakarta/annotation/jakarta.annotation-api/maven-metadata.xml) |
| [jakarta.mail:jakarta.mail-api](https://mvnrepository.com/artifact/jakarta.mail/jakarta.mail-api) | 2.1.5 | 2.1.5 | [Central](https://repo.maven.apache.org/maven2/jakarta/mail/jakarta.mail-api/maven-metadata.xml) |
| [jakarta.servlet:jakarta.servlet-api](https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api) | 6.1.0 | 6.1.0 | [Central](https://repo.maven.apache.org/maven2/jakarta/servlet/jakarta.servlet-api/maven-metadata.xml) |
| [jakarta.validation:jakarta.validation-api](https://mvnrepository.com/artifact/jakarta.validation/jakarta.validation-api) | 3.1.1 | 3.1.1 | [Central](https://repo.maven.apache.org/maven2/jakarta/validation/jakarta.validation-api/maven-metadata.xml) |
| [org.apache.commons:commons-lang3](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3) | 3.20.0 | 3.21.0 | [Central](https://repo.maven.apache.org/maven2/org/apache/commons/commons-lang3/maven-metadata.xml) |
| [org.apache.maven.plugins:maven-compiler-plugin](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-compiler-plugin) | 3.16.0 | 3.16.0 | [Central](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/maven-metadata.xml) |
| [org.apache.maven.plugins:maven-failsafe-plugin](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-failsafe-plugin) | 3.6.0 | 3.6.0 | [Central](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-failsafe-plugin/maven-metadata.xml) |
| [org.apache.poi:poi](https://mvnrepository.com/artifact/org.apache.poi/poi) | 5.5.1 | 5.5.1 | [Central](https://repo.maven.apache.org/maven2/org/apache/poi/poi/maven-metadata.xml) |
| [org.apache.poi:poi-ooxml](https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml) | 5.5.1 | 5.5.1 | [Central](https://repo.maven.apache.org/maven2/org/apache/poi/poi-ooxml/maven-metadata.xml) |
| [org.aspectj:aspectjweaver](https://mvnrepository.com/artifact/org.aspectj/aspectjweaver) | 1.9.25.1 | 1.9.25.1 | [Central](https://repo.maven.apache.org/maven2/org/aspectj/aspectjweaver/maven-metadata.xml) |
| [org.eclipse.angus:angus-activation](https://mvnrepository.com/artifact/org.eclipse.angus/angus-activation) | 2.0.3 | 2.0.3 | [Central](https://repo.maven.apache.org/maven2/org/eclipse/angus/angus-activation/maven-metadata.xml) |
| [org.eclipse.angus:angus-mail](https://mvnrepository.com/artifact/org.eclipse.angus/angus-mail) | 2.0.5 | 2.0.5 | [Central](https://repo.maven.apache.org/maven2/org/eclipse/angus/angus-mail/maven-metadata.xml) |
| [org.jsoup:jsoup](https://mvnrepository.com/artifact/org.jsoup/jsoup) | 1.23.2 | 1.23.2 | [Central](https://repo.maven.apache.org/maven2/org/jsoup/jsoup/maven-metadata.xml) |
| [org.mongodb:mongodb-driver-sync](https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync) | 5.8.1 | 5.13.0 | [Central](https://repo.maven.apache.org/maven2/org/mongodb/mongodb-driver-sync/maven-metadata.xml) |
| [org.mybatis:mybatis-spring](https://mvnrepository.com/artifact/org.mybatis/mybatis-spring) | 4.1.0 | 4.1.0 | [Central](https://repo.maven.apache.org/maven2/org/mybatis/mybatis-spring/maven-metadata.xml) |
| [org.projectlombok:lombok](https://mvnrepository.com/artifact/org.projectlombok/lombok) | 1.18.48 | 1.18.48 | [Central](https://repo.maven.apache.org/maven2/org/projectlombok/lombok/maven-metadata.xml) |
| [org.slf4j:slf4j-api](https://mvnrepository.com/artifact/org.slf4j/slf4j-api) | 2.0.18 | 2.0.20 | [Central](https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/maven-metadata.xml) |
| [org.springframework:spring-context](https://mvnrepository.com/artifact/org.springframework/spring-context) | 7.0.9 | 7.0.9 | [Central](https://repo.maven.apache.org/maven2/org/springframework/spring-context/maven-metadata.xml) |
| [org.springframework:spring-jdbc](https://mvnrepository.com/artifact/org.springframework/spring-jdbc) | 7.0.9 | 7.0.9 | [Central](https://repo.maven.apache.org/maven2/org/springframework/spring-jdbc/maven-metadata.xml) |
| [org.springframework:spring-web](https://mvnrepository.com/artifact/org.springframework/spring-web) | 7.0.9 | 7.0.9 | [Central](https://repo.maven.apache.org/maven2/org/springframework/spring-web/maven-metadata.xml) |
| [org.springframework:spring-webmvc](https://mvnrepository.com/artifact/org.springframework/spring-webmvc) | 7.0.9 | 7.0.9 | [Central](https://repo.maven.apache.org/maven2/org/springframework/spring-webmvc/maven-metadata.xml) |
| [org.springframework.boot:spring-boot-autoconfigure](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-autoconfigure) | 4.1.1 | 4.1.1 | [Central](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-autoconfigure/maven-metadata.xml) |
| [org.springframework.boot:spring-boot-starter-aspectj](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-aspectj) | 4.1.1 | 4.1.1 | [Central](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-aspectj/maven-metadata.xml) |
| [org.springframework.boot:spring-boot-starter-parent](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-parent) | 4.1.1 | 4.1.1 | [Central](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml) |
| [org.springframework.boot:spring-boot-starter-test](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test) | 4.1.1 | 4.1.1 | [Central](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-test/maven-metadata.xml) |
| [org.springframework.boot:spring-boot-starter-validation](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-validation) | 4.1.1 | 4.1.1 | [Central](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-validation/maven-metadata.xml) |
| [org.springframework.boot:spring-boot-starter-webmvc](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-webmvc) | 4.1.1 | 4.1.1 | [Central](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-webmvc/maven-metadata.xml) |
| [org.springframework.data:spring-data-mongodb](https://mvnrepository.com/artifact/org.springframework.data/spring-data-mongodb) | 5.1.1 | 5.1.1 | [Central](https://repo.maven.apache.org/maven2/org/springframework/data/spring-data-mongodb/maven-metadata.xml) |
| [org.springframework.data:spring-data-redis](https://mvnrepository.com/artifact/org.springframework.data/spring-data-redis) | 4.1.1 | 4.1.1 | [Central](https://repo.maven.apache.org/maven2/org/springframework/data/spring-data-redis/maven-metadata.xml) |
| [redis.clients:jedis](https://mvnrepository.com/artifact/redis.clients/jedis) | 7.4.1 | 8.0.2 | [Central](https://repo.maven.apache.org/maven2/redis/clients/jedis/maven-metadata.xml) |
| [tools.jackson.core:jackson-databind](https://mvnrepository.com/artifact/tools.jackson.core/jackson-databind) | 3.1.5 | 3.2.3 | [Central](https://repo.maven.apache.org/maven2/tools/jackson/core/jackson-databind/maven-metadata.xml) |
| [tools.jackson.dataformat:jackson-dataformat-xml](https://mvnrepository.com/artifact/tools.jackson.dataformat/jackson-dataformat-xml) | 3.1.5 | 3.2.3 | [Central](https://repo.maven.apache.org/maven2/tools/jackson/dataformat/jackson-dataformat-xml/maven-metadata.xml) |

## 统一版本与兼容处理

- Jackson 3 使用 `tools.jackson:jackson-bom:3.2.3`；Boot 同时管理的 Jackson 2 BOM 更新为 `com.fasterxml.jackson:jackson-bom:2.22.3`，两者共享 `jackson-annotations:2.22`。仅升级 Jackson 3 会被旧 Jackson 2 BOM 覆盖注解，导致 `JsonApplyView` 类缺失；最终通过现有 JSON/过滤/异常兼容测试。
- MongoDB 用 `mongodb.version=5.13.0` 统一同步驱动及同组核心/BSON 组件；不单独覆盖某个驱动 JAR。
- Jedis 使用 8.0.2。被移除的 `JedisPooled` 改用官方 `RedisClient.create()/builder()`，Spring Data 4.1.1 仍使用其连接工厂；Auth 和 Enigma 独立池保持由 Spring/存储组件关闭。
- Caffeine 3.3.0、Commons Lang 3.21.0、H2 2.5.252、MySQL Connector/J 26.7.0 和 SLF4J 2.0.20 在 parent 中统一覆盖 Boot 默认版本。MySQL 的 26.7 是正式版编号，不是预发布标记。
- Boot 4.1.1、Spring Framework 7.0.9、Spring Data MongoDB 5.1.1 / Redis 4.1.1 与其他未变化的坐标已是核查时最新稳定版；不采用 Boot 4.2 milestone、Servlet 6.2 milestone、Validation 4 milestone 或 Compiler 4 beta。
- Jedis 上游仍带有 `redis-authx-core:0.1.1-beta2` 传递依赖。Jedis 自身是稳定发布；本表的“稳定”指明确核查的 POM 坐标，不能扩展为所有传递依赖都没有预发布版本。该认证扩展不作为 lfp 的生产身份验证实现。
- `lfp.version` 独立固定内部模块版本，应用继承 parent 后可以使用自己的应用版本号。

补充 BOM 来源：[Jackson 3 BOM](https://repo.maven.apache.org/maven2/tools/jackson/jackson-bom/3.2.3/jackson-bom-3.2.3.pom)、[Jackson 2 BOM](https://mvnrepository.com/artifact/com.fasterxml.jackson/jackson-bom/2.22.3)、[Jedis 8.0.2 官方发布](https://github.com/redis/jedis/releases/tag/v8.0.2)。部分 Maven Repository 页面缓存晚于 Central，最终以已发布到 Central 的稳定版本和实际解析结果为准。

## 验证方式

分别执行实际 JDK 21/25 的 reactor 构建、普通测试、真实 Redis/MySQL/MongoDB 集成测试和 Chromium Web Crypto 检查；具体结果记录在 [verification](verification.md)。构建期间核对两份 effective POM，确认表格版本与解析结果一致。

```bash
./mvnw help:effective-pom
./mvnw dependency:tree
./mvnw clean verify -Pintegration
```

格式化工具作为临时开发工具使用，见[格式说明](formatting.md)。
