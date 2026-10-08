# lxm-framework-parent

`parent` 是 lfp 的 Maven 父 POM，统一模块版本、依赖管理、Java 编译、注解处理和测试插件。它继承 Spring Boot 4.1.1，并覆盖经过验证的独立依赖版本；不包含业务类。

## 在应用中使用

先按[根说明](../README.md)安装框架制品，再让应用继承：

```xml
<parent>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-parent</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
    <relativePath/>
</parent>
```

应用可以使用自己的 `groupId/artifactId/version`，内部模块版本由 `lfp.version` 单独管理。使用这个 parent 后，引入框架模块不必再次写版本：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-web</artifactId>
</dependency>
```

已有公司 parent 的应用可以保留它，显式引入所需模块版本，并对齐[依赖版本表](../docs/dependency-matrix.md)中的配套 BOM/属性。不要只升级一个 Jackson 或 MongoDB 驱动 JAR 而保留同组其他旧版本。

## 构建约定

| 配置 | 行为 |
| --- | --- |
| Java release | `25`，生产/测试按同一目标编译 |
| 编码 | UTF-8 |
| 方法参数 | `-parameters`，保留参数名 |
| Lombok | provided，显式声明 annotation processor |
| 库制品 | 普通 JAR；根与 parent 为 POM |
| Maven Wrapper | 3.3.4，运行 Maven 3.9.16 并检查发行包校验值 |
| 测试插件 | Surefire/Failsafe 3.6.0 |

根 reactor 包含 parent 与全部功能模块，可以直接从源码构建，无需预先单独安装 parent。使用 `./mvnw`，Windows 使用 `mvnw.cmd`。

## 测试与安装

```bash
./mvnw clean verify
./mvnw install
./mvnw clean verify -Pintegration
```

普通测试 `*Test` 由 Surefire 执行，服务测试 `*IT` 由 `integration` profile 的 Failsafe 在 verify 阶段执行。服务缺失时集成测试应失败；专用 Redis/MySQL/MongoDB 端口见[测试说明](../docs/verification.md)。

## 注意事项

- 选择与应用运行 JDK 对应的制品版本；JDK 25 是本仓库的最低目标版本。
- `lfp.version` 是整套框架模块版本，覆盖它时必须确保对应的全部制品可用。
- 应用若重新声明同名版本属性或自己的 dependencyManagement，会改变最终依赖；用 `help:effective-pom`、`dependency:tree` 检查解析结果。
- 只继承 parent 不会生成应用入口或打包可执行 Boot JAR；应用需要自己的启动类和 `spring-boot-maven-plugin`。
- parent 不保存制品仓库凭据；向团队仓库发布时使用自己的 Maven settings 与发布配置。

格式约定见[格式说明](../docs/formatting.md)。回到[项目接入说明](../README.md)。
