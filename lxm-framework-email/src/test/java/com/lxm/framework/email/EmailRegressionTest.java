package com.lxm.framework.email;

import com.lxm.framework.email.enums.*;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class EmailRegressionTest {
    @Test
    void presetsReturnIndependentPropertiesAndCorrectSmtpPort() {
        var properties = EmailCategorySender.GMAIL.props();
        assertEquals("465", properties.getProperty("mail.smtp.port"));
        properties.put("password", "test-secret");
        assertNull(EmailCategorySender.GMAIL.props().getProperty("password"));
        assertEquals("true", properties.getProperty("mail.smtp.ssl.checkserveridentity"));
    }

    @Test
    void sendsUtf8MultipleRecipientsAndAttachmentThroughSmtp() throws Exception {
        try (var server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
                var executor = Executors.newSingleThreadExecutor()) {
            var received =
                    executor.submit(
                            () -> {
                                try (var socket = server.accept()) {
                                    socket.setSoTimeout(10000);
                                    var reader =
                                            new BufferedReader(
                                                    new InputStreamReader(
                                                            socket.getInputStream(),
                                                            StandardCharsets.UTF_8));
                                    var writer =
                                            new PrintWriter(
                                                    socket.getOutputStream(),
                                                    true,
                                                    StandardCharsets.UTF_8);
                                    writer.print("220 local SMTP\r\n");
                                    writer.flush();
                                    StringBuilder body = new StringBuilder();
                                    int recipients = 0;
                                    for (String line; (line = reader.readLine()) != null; ) {
                                        if (line.startsWith("EHLO") || line.startsWith("HELO"))
                                            writer.print("250 local\r\n");
                                        else if (line.startsWith("RCPT")) {
                                            recipients++;
                                            writer.print("250 ok\r\n");
                                        } else if (line.equals("DATA")) {
                                            writer.print("354 data\r\n");
                                            writer.flush();
                                            while (!(line = reader.readLine()).equals("."))
                                                body.append(line).append("\r\n");
                                            writer.print("250 accepted\r\n");
                                        } else if (line.equals("QUIT")) {
                                            writer.print("221 bye\r\n");
                                            writer.flush();
                                            break;
                                        } else writer.print("250 ok\r\n");
                                        writer.flush();
                                    }
                                    assertEquals(2, recipients);
                                    return body.toString();
                                }
                            });
            EmailCategory category =
                    () -> {
                        var p = new Properties();
                        p.put("mail.transport.protocol", "smtp");
                        p.put("mail.smtp.host", "127.0.0.1");
                        p.put("mail.smtp.port", Integer.toString(server.getLocalPort()));
                        p.put("mail.smtp.auth", "false");
                        p.put("mail.smtp.connectiontimeout", "5000");
                        p.put("mail.smtp.timeout", "5000");
                        return p;
                    };
            var attachment = new ByteArrayOutputStream();
            attachment.write("附件内容".getBytes(StandardCharsets.UTF_8));
            EasyEmailSender.config(category, "sender@example.test", "fixture-only")
                    .from("发送者")
                    .to("first@example.test", "second@example.test")
                    .subject("中文主题")
                    .text("中文正文")
                    .attach(attachment, "sample", "txt")
                    .send();
            var message =
                    new MimeMessage(
                            Session.getInstance(new Properties()),
                            new ByteArrayInputStream(
                                    received.get(10, TimeUnit.SECONDS)
                                            .getBytes(StandardCharsets.UTF_8)));
            assertEquals("中文主题", message.getSubject());
            assertEquals(2, message.getAllRecipients().length);
            var multipart = (Multipart) message.getContent();
            assertEquals(2, multipart.getCount());
            assertTrue(multipart.getBodyPart(0).getContent().toString().contains("中文正文"));
            assertEquals("sample.txt", multipart.getBodyPart(1).getFileName());
        }
    }
}
