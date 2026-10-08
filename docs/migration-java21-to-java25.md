# Java 21 → Java 25

由已验收 Java 21 源码复制，排除 .git/target/node_modules 与测试输出。内部 Maven 版本改为 2.0.0-java25-SNAPSHOT，java.version/release 改为 25；Boot 4.1.1 和协议保持一致，CI 使用 JDK 25。

显式 Lombok annotationProcessorPaths 避免 JDK 23 起默认关闭处理器导致模型方法缺失。两仓库不交叉覆盖 Maven 制品，JDK 25 制品不能在 JDK 21 运行。已在共同逻辑中移除 SecurityManager 检查，POI 反射使用 trySetAccessible，不添加宽泛 --add-opens/--enable-native-access JVM 参数。

JDK 25 的原生构建、回归、真实服务与浏览器验证记录见 verification.md；Java 21 结果不能替代 Java 25。向量文件内容完全相同，接口保护与身份生命周期相同。

[Oracle JDK 25 迁移指南](https://docs.oracle.com/en/java/javase/25/migrate/preparing-migration.html)。
