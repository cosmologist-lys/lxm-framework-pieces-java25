# lxm-framework-email

`email` 封装 Jakarta Mail 与 Angus，支持文本/HTML 邮件、多收件人、附件和 INBOX 读取。它不需要 Spring，可用于普通 Java 程序或应用的通知服务。

## 引入依赖

要求 JDK 25。先按[项目说明](../README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-email</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 发送邮件

```java
import com.lxm.framework.email.EasyEmailSender;
import com.lxm.framework.email.enums.EmailCategorySender;
import java.io.File;

EasyEmailSender.config(
                EmailCategorySender.QQ,
                System.getenv("MAIL_ADDRESS"),
                System.getenv("MAIL_PASSWORD"))
        .from("通知服务")
        .to("alice@example.com", "bob@example.com")
        .subject("报表已生成")
        .text("附件中是本次报表。")
        .attach(new File("report.xlsx"))
        .send();
```

`MAIL_PASSWORD` 使用服务商要求的授权码或应用密码。预置发件配置包括 QQ、QQ 企业邮箱、网易 163 和 Gmail，SMTP 端口为 465，启用 TLS、主机名校验和超时；实际可用性取决于邮箱服务设置。

`to/cc/bcc` 接受多个地址。`text` 设置纯文本正文，`html` 设置 HTML 正文；附件支持 `File`、`ByteArrayOutputStream` 和 `URL`，可显式指定文件名。每封邮件使用新的 builder，不在多个线程间共享。

## 自定义 SMTP

实现 `EmailCategory` 返回 Java Mail Properties，可配置自己的 SMTP/STARTTLS 服务：

```java
import com.lxm.framework.email.enums.EmailCategory;
import java.util.Properties;

EmailCategory smtp =
        () -> {
            var properties = new Properties();
            properties.setProperty("mail.transport.protocol", "smtp");
            properties.setProperty("mail.smtp.host", "smtp.example.com");
            properties.setProperty("mail.smtp.port", "587");
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.starttls.enable", "true");
            properties.setProperty("mail.smtp.starttls.required", "true");
            properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
            properties.setProperty("mail.smtp.connectiontimeout", "10000");
            properties.setProperty("mail.smtp.timeout", "10000");
            properties.setProperty("mail.smtp.writetimeout", "10000");
            return properties;
        };
```

再调用 `EasyEmailSender.config(smtp, address, password)`。框架复制配置，每个 Session 通过独立 Authenticator 使用凭据，不把密码写入共享 Properties。

## 读取邮箱

通过自定义收件配置使用服务商实际提供的 IMAP/POP3 连接参数。以下为 IMAP over TLS 的配置形态：

```java
import com.lxm.framework.email.EasyEmailRecipient;

EmailCategory imap =
        () -> {
            var properties = new Properties();
            properties.setProperty("mail.store.protocol", "imap");
            properties.setProperty("mail.imap.host", "imap.example.com");
            properties.setProperty("mail.imap.port", "993");
            properties.setProperty("mail.imap.user", System.getenv("MAIL_ADDRESS"));
            properties.setProperty("mail.imap.ssl.enable", "true");
            properties.setProperty("mail.imap.ssl.checkserveridentity", "true");
            properties.setProperty("mail.imap.connectiontimeout", "10000");
            properties.setProperty("mail.imap.timeout", "10000");
            return properties;
        };

try (var recipient =
        EasyEmailRecipient.config(
                imap, System.getenv("MAIL_ADDRESS"), System.getenv("MAIL_PASSWORD"))) {
    var mail = recipient.connect().read();
    String subject = mail.getSubject();
    String content = mail.getContent();
}
```

`connect()` 打开 INBOX，按 READ_ONLY 读取；`read(fromAddress, subjectContains)` 可筛选发件人和主题。空邮箱可能没有匹配结果，调用方应按返回 getter 的空值处理。每个 recipient 持有自己的 Store/Folder，使用 `try-with-resources` 关闭，不与其他调用共享。

## 注意事项

- 服务地址、TLS 模式、账号授权与限流以邮箱服务商规则为准；本模块不提供 OAuth 登录流程或持久化投递队列。
- 发送失败抛出 `EmailException`；邮件发送属于外部副作用，重试前考虑重复投递，重要通知可在应用中保存发送状态。
- 正文和附件来自可信业务数据；URL 附件只允许应用认可的目标，避免读取任意内部地址。
- 邮箱密码/授权码从环境或密钥服务读取，不放入源码或日志；不要在生产开启邮件协议 debug。
- 本项目自动化测试覆盖本地 SMTP 与 MIME，不替代实际供应商的账号、TLS 或 IMAP 接入验证。

回到[项目接入说明](../README.md)。
