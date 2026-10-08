# lxm-framework-email

Jakarta Mail 邮件。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-email:2.0.0-java25-SNAPSHOT`。

## 功能与接入

EasyEmailSender 使用 Jakarta Mail API 与 Angus 运行时。`config(category,address,password).from("姓名").to("a@example.test","b@example.test").subject("主题").text("正文").send()`；附件支持 File、ByteArrayOutputStream 和 URL。自定义 EmailCategory 返回 Properties 可配置 SMTP/STARTTLS。

## 使用约定与验证

预置 SMTP 使用 TLS、主机名校验与超时，Gmail SMTP 端口 465。每次会话复制配置，凭据只传入 Authenticator，不写共享 Properties。发送 builder 不跨线程共享；多个收件人正确合并，正文为 UTF-8。EasyEmailRecipient 为实例 Store/Folder，可 try-with-resources；不再共享静态连接，INBOX 使用 READ_ONLY，空邮箱可读取，最新邮件索引修复。URL 附件只接受应用认可的目标；邮箱地址、授权码由使用方环境提供。测试使用本地 SMTP 协议端点验证中文 MIME、两个收件人和附件；未在真实公网邮箱执行发送，也未验收供应商的 OAuth/IMAP/TLS 登录。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
