package com.lxm.framework.email;

import com.lxm.framework.email.enums.EmailCategory;
import com.lxm.framework.email.util.EmailStreamUtil;
import org.apache.commons.lang3.StringUtils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * @Author: Lys
 * @Date 2022/9/16
 * @Describe
 **/
public class EasyEmailRecipient implements AutoCloseable {

    private EasyEmailRecipient(Store store) {
        this.store = store;
        this.folder = null;
    }

    private final Store store;
    private Folder folder;

    public static EasyEmailRecipient config(EmailCategory category, String emailAddress, String password) {
        var props = (java.util.Properties) category.props().clone();
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailAddress, password);
            }
        });
        Store store = null;
        try {
            store = session.getStore();
            if (Objects.isNull(store)) {
                throw new EmailException("email-receiver initialized failed ");
            }
        } catch (MessagingException e) {
            throw new EmailException(e);
        }
        return new EasyEmailRecipient(store);
    }

    public EmailSubReader connect() {
        try {
            this.store.connect();
            if (!this.store.isConnected()) {
                throw new EmailException("store连接失败");
            }
            this.folder = this.store.getFolder("INBOX");
            if (!this.folder.isOpen()) {
                this.folder.open(Folder.READ_ONLY);
            }
            Message[] messages = this.folder.getMessages();
            if (Objects.isNull(messages)) {
                throw new EmailException("");
            }
            return new EmailSubReader(messages, this);
        } catch (Exception e) {
            throw new EmailException("folder连接失败");
        }
    }

    public void close() {
        try {
            if (this.folder!=null && this.folder.isOpen()) {
                this.folder.close(false);
            }
            if (this.store.isConnected()) {
                this.store.close();
            }
        } catch (Exception e) {
            throw new EmailException("关闭链接失败");
        }
    }

    public static class EmailSubReader {
        private final Message[] messages;
        private final EasyEmailRecipient core;
        // 是否已连接
        private boolean greenLight;
        // 主题
        private String subject;
        // 发送人
        private String fromEmail;
        // 发送人名字
        private String fromName;
        // 接受时间
        private LocalDateTime receivedAt;

        private final StringBuilder content;

        private EmailSubReader(Message[] messages, EasyEmailRecipient core) {
            this.messages = messages;
            this.core = core;
            this.content = new StringBuilder();
            this.greenLight = true;
        }

        // read latest one of fromAddress & subject name contains
        public EmailSubReader read(String fromAddress, String subjectCharacterContains) {
            if (!greenLight) {
                throw new EmailException("连接已关闭");
            }
            if (messages.length==0) return this;
            content.setLength(0);
            final int len = messages.length - 1;
            try {
                if (StringUtils.isAllBlank(fromAddress, subjectCharacterContains)) {
                    var msg = (MimeMessage) messages[len];
                    readSingleMessage(msg);
                } else {
                    for (int i = len; i >= 0; i--) {
                        var msg = (MimeMessage) messages[i];
                        if (StringUtils.isNotBlank(fromAddress)) {
                            readFromAddress(msg);
                            if (StringUtils.equalsAnyIgnoreCase(fromAddress, this.fromEmail)) {
                                if (StringUtils.isNotBlank(subjectCharacterContains)) {
                                    readSubject(msg);
                                    if (StringUtils.contains(this.subject, subjectCharacterContains)) {
                                        readSingleMessage(msg);
                                        break;
                                    }
                                    continue;
                                }
                                readSingleMessage(msg);
                                break;
                            }
                        } else {
                            readSubject(msg);
                            if (StringUtils.contains(this.subject, subjectCharacterContains)) {
                                readSingleMessage(msg);
                                break;
                            }
                        }
                    }
                }
            } catch (MessagingException | IOException e) {
                throw new EmailException("email解析出错");
            }
            return this;
        }

        // read latest one of fromAddress
        public EmailSubReader read(String fromAddress) {
            return read(fromAddress, null);
        }

        // read latest one
        public EmailSubReader read() {
            return read(null, null);
        }

        public void close() {
            if (Objects.nonNull(this.core)) {
                this.core.close();
            }
            greenLight = false;
        }

        public EmailSubReader readThenClose(String fromAddress, String subjectCharacterContains) {
            var e = read(fromAddress, subjectCharacterContains);
            e.close();
            greenLight = true;
            return e;
        }

        public EmailSubReader readThenClose(String fromAddress) {
            var e = read(fromAddress, null);
            e.close();
            greenLight = true;
            return e;
        }

        public EmailSubReader readThenClose() {
            var e = read(null, null);
            e.close();
            greenLight = true;
            return e;
        }

        public String getContent() {
            if (greenLight) {
                return content.toString();
            }
            throw new EmailException("尚未指定要读取的邮件或连接已关闭");
        }

        public String getSubject() {
            if (greenLight) {
                return this.subject;
            }
            throw new EmailException("尚未指定要读取的邮件或连接已关闭");
        }

        public String getFromEmail() {
            if (greenLight) {
                return this.fromEmail;
            }
            throw new EmailException("尚未指定要读取的邮件或连接已关闭");
        }

        public String getFromName() {
            if (greenLight) {
                return this.fromName;
            }
            throw new EmailException("尚未指定要读取的邮件或连接已关闭");
        }

        public LocalDateTime getReceivedAt() {
            if (greenLight) {
                return this.receivedAt;
            }
            throw new EmailException("尚未指定要读取的邮件或连接已关闭");
        }


        // -------------------------- below are private ---------------------------------//
        private void readSingleMessage(MimeMessage msg) throws MessagingException, IOException {
            greenLight = true;
            readSubject(msg);
            readFromAddress(msg);
            readReceiveTime(msg);
            readContent(msg);
        }

        private void readSubject(Message msg) throws MessagingException, UnsupportedEncodingException {
            this.subject = MimeUtility.decodeText(msg.getSubject());
        }

        private void readFromAddress(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
            Address[] froms = msg.getFrom();
            if (Objects.isNull(froms) || froms.length == 0) {
                throw new MessagingException("no send address found");
            }
            InternetAddress address = (InternetAddress) froms[0];
            String personal = address.getPersonal();
            if (StringUtils.isNotBlank(personal)) {
                this.fromName = MimeUtility.decodeText(personal);
            }
            this.fromEmail = address.getAddress();
        }

        private void readReceiveTime(MimeMessage msg) throws MessagingException {
            var date = msg.getReceivedDate();
            if (Objects.isNull(date)) {
                date = msg.getSentDate();
                if (Objects.isNull(date)) {
                    return;
                }
            }
            this.receivedAt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        private void readContent(Part part) throws MessagingException, IOException {
            String contentType = part.getContentType();
            int nameIndex = contentType.indexOf("name");
            boolean conName = nameIndex != -1;
            if (part.isMimeType("text/plain") && !conName) {
                if (Objects.nonNull(part.getContent())) {
                    content.append(readByContent(part.getContent()));
                } else if (Objects.nonNull(part.getInputStream())) {
                    content.append(readByStream(part.getInputStream()));
                }
            } else if (part.isMimeType("text/html") && !conName) {
                if (Objects.nonNull(part.getContent())) {
                    content.append(readByContent(part.getContent()));
                } else if (Objects.nonNull(part.getInputStream())) {
                    content.append(readByStream(part.getInputStream()));
                }
            } else if (part.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) part.getContent();
                int counts = multipart.getCount();
                for (int i = 0; i < counts; i++) {
                    readContent(multipart.getBodyPart(i));
                }
            } else if (part.isMimeType("message/rfc822")) {
                readContent((Part) part.getContent());
            }
        }

        private String readByContent(Object content) {
            return (String) content;
        }

        private String readByStream(InputStream inputStream) throws IOException {
            try (inputStream) {
                return EmailStreamUtil.copyStreamToString(inputStream, StandardCharsets.UTF_8);
            }
        }


    }
}
