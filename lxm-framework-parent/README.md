# lxm-framework-parent

统一构建基线。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-parent:2.0.0-java25-SNAPSHOT`。

## 功能与接入

继承 Spring Boot 4.1.1 的依赖管理，集中管理非 BOM 库和所有 `com.lxm` 模块版本。根 reactor 首先构建 parent，子模块以 `../lxm-framework-parent/pom.xml` 解析父 POM。Java release=21，UTF-8，`-parameters`，显式 Lombok annotation processor；库模块保持普通 JAR，不重打包为 Boot 应用。

## 使用约定与验证

执行根目录 `./mvnw clean verify`；`integration` profile 使用 Failsafe 执行 `*IT`。默认 Surefire 执行 `*Test`。Maven Wrapper 3.3.4 固定 Maven 3.9.16，附发行包校验值。移除原内网 distributionManagement；`deploy` 需应用自己的仓库和凭据，不在源码中保存凭据。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
