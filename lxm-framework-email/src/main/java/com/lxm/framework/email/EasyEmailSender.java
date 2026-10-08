package com.lxm.framework.email;

import com.lxm.framework.email.enums.EmailCategory;
import org.apache.commons.lang3.StringUtils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: Lys
 * @Date 2022/6/23
 * @Describe
 **/
public class EasyEmailSender {

    private EasyEmailSender() {}

    public static EmailSender config(EmailCategory category, String emailAddress, String password) {
        var props = (java.util.Properties) category.props().clone();
        props.put("username", emailAddress);
        Session session =
                Session.getInstance(
                        props,
                        new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(emailAddress, password);
                            }
                        });
        var message = new MimeMessage(session);
        return new EmailSender(message, emailAddress);
    }

    public static class EmailSender {

        private EmailSender(MimeMessage message, String username) {
            this.message = message;
            this.username = username;
        }

        final MimeMessage message;
        final String username;
        String text;
        String html;
        final List<MimeBodyPart> attachments = new ArrayList<MimeBodyPart>();

        /**
         * 主题
         *
         * @param subject
         * @return
         * @throws EmailException
         */
        public EmailSender subject(String subject) throws EmailException {
            try {
                this.message.setSubject(subject, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                throw new EmailException(e);
            }
            return this;
        }

        /**
         * 发送自
         *
         * @param fromDescription
         * @return
         * @throws EmailException
         */
        public EmailSender from(String fromDescription) throws EmailException {
            try {
                String encodeNickName = MimeUtility.encodeText(fromDescription);
                this.message.setFrom(
                        new InternetAddress(encodeNickName + " <" + this.username + ">"));
            } catch (Exception e) {
                throw new EmailException(e);
            }
            return this;
        }

        /**
         * 发送至
         *
         * @param to
         * @return
         * @throws EmailException
         */
        public EmailSender to(String... to) throws EmailException {
            try {
                return addRecipient(to, Message.RecipientType.TO);
            } catch (Exception e) {
                throw new EmailException(e);
            }
        }

        /**
         * 抄送至
         *
         * @param cc
         * @return
         * @throws EmailException
         */
        public EmailSender cc(String... cc) throws EmailException {
            try {
                return addRecipient(cc, Message.RecipientType.CC);
            } catch (Exception e) {
                throw new EmailException(e);
            }
        }

        /**
         * 秘密抄送至
         *
         * @param bcc
         * @return
         * @throws EmailException
         */
        public EmailSender bcc(String... bcc) throws EmailException {
            try {
                return addRecipient(bcc, Message.RecipientType.BCC);
            } catch (Exception e) {
                throw new EmailException(e);
            }
        }

        public EmailSender text(String content) {
            this.text = content;
            return this;
        }

        public EmailSender html(String html) {
            this.html = html;
            return this;
        }

        public EmailSender attach(File file) throws EmailException {
            this.attachments.add(createAttachment(file, null));
            return this;
        }

        public EmailSender attach(File file, String fileName) throws EmailException {
            this.attachments.add(createAttachment(file, fileName));
            return this;
        }

        /**
         * 以流的形式发送附件
         *
         * @param outputStream 输出流
         * @param filename     文件名
         * @param extension    后缀扩展名
         * @return
         * @throws EmailException
         */
        public EmailSender attach(
                ByteArrayOutputStream outputStream, String filename, String extension)
                throws EmailException {
            this.attachments.add(createAttachment(outputStream, filename, extension));
            return this;
        }

        /**
         * 以网络资源的形式发送附件
         *
         * @param url      网络资源url
         * @param fileName 文件名
         * @return
         * @throws EmailException
         */
        public EmailSender attach(URL url, String fileName) throws EmailException {
            this.attachments.add(createURLAttachment(url, fileName));
            return this;
        }

        private EmailSender addRecipient(String[] recipient, Message.RecipientType type)
                throws MessagingException {
            if (recipient == null || recipient.length == 0)
                throw new MessagingException("At least one recipient is required");
            String addresses = String.join(",", recipient).replace(";", ",");
            this.message.setRecipients(type, InternetAddress.parse(addresses, true));
            return this;
        }

        private MimeBodyPart createAttachment(
                ByteArrayOutputStream outputStream, String filename, String extension)
                throws EmailException {
            DataSource aAttachment =
                    new ByteArrayDataSource(outputStream.toByteArray(), "application/octet-stream");
            MimeBodyPart attachmentPart = new MimeBodyPart();
            try {
                if (StringUtils.isNotBlank(extension) && !extension.startsWith(".")) {
                    extension = "." + extension;
                }
                String file =
                        StringUtils.isBlank(extension) ? filename : filename.concat(extension);
                attachmentPart.setDataHandler(new DataHandler(aAttachment));
                attachmentPart.setFileName(file);
            } catch (Exception e) {
                throw new EmailException(e);
            }
            return attachmentPart;
        }

        private MimeBodyPart createAttachment(File file, String fileName) throws EmailException {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(file);
            try {
                attachmentPart.setDataHandler(new DataHandler(fds));
                attachmentPart.setFileName(
                        null == fileName
                                ? MimeUtility.encodeText(fds.getName())
                                : MimeUtility.encodeText(fileName));
            } catch (Exception e) {
                throw new EmailException(e);
            }
            return attachmentPart;
        }

        private MimeBodyPart createURLAttachment(URL url, String fileName) throws EmailException {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataHandler dataHandler = new DataHandler(url);
            try {
                attachmentPart.setDataHandler(dataHandler);
                attachmentPart.setFileName(
                        null == fileName ? "untitled-file" : MimeUtility.encodeText(fileName));
            } catch (Exception e) {
                throw new EmailException(e);
            }
            return attachmentPart;
        }

        private MimeBodyPart textPart() throws MessagingException {
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(text, StandardCharsets.UTF_8.name());
            return bodyPart;
        }

        private MimeBodyPart htmlPart() throws MessagingException {
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(html, "text/html; charset=utf-8");
            return bodyPart;
        }

        private MimeBodyPart toBodyPart(MimeMultipart cover) throws MessagingException {
            MimeBodyPart wrap = new MimeBodyPart();
            wrap.setContent(cover);
            return wrap;
        }

        public void send() {
            if (text == null && html == null) {
                throw new IllegalArgumentException(
                        "At least one context has to be provided: Text or Html");
            }

            MimeMultipart cover;
            boolean usingAlternative = false;
            boolean hasAttachments = attachments.size() > 0;

            try {
                if (StringUtils.isNotBlank(text) && StringUtils.isBlank(html)) {
                    // TEXT ONLY
                    cover = new MimeMultipart("mixed");
                    cover.addBodyPart(textPart());
                } else if (StringUtils.isBlank(text) && StringUtils.isNotBlank(html)) {
                    // HTML ONLY
                    cover = new MimeMultipart("mixed");
                    cover.addBodyPart(htmlPart());
                } else {
                    // HTML + TEXT
                    cover = new MimeMultipart("alternative");
                    cover.addBodyPart(textPart());
                    cover.addBodyPart(htmlPart());
                    usingAlternative = true;
                }

                MimeMultipart content = cover;
                if (usingAlternative && hasAttachments) {
                    content = new MimeMultipart("mixed");
                    content.addBodyPart(toBodyPart(cover));
                }

                for (MimeBodyPart attachment : attachments) {
                    content.addBodyPart(attachment);
                }

                this.message.setContent(content);
                this.message.setSentDate(new Date());
                Transport.send(this.message);
            } catch (Exception e) {
                throw new EmailException(e);
            }
        }
    }
}
