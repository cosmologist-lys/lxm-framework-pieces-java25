package com.lxm.framework.email.enums;

import java.util.Properties;

/**
 * @Author: Lys
 * @Date 2022/6/23
 * @Describe
 **/
public enum EmailCategorySender implements EmailCategory{

    // qq企业版
    QQ_ENT("smtp.exmail.qq.com", 465, true, 10000),
    // qq
    QQ("smtp.qq.com", 465, true, 10000),
    // 网易
    NET163("smtp.163.com", 465, true, 10000),
    // gmail
    GMAIL("smtp.gmail.com", 465, true, 10000);

    private final Properties properties;

    EmailCategorySender(String server, int port, boolean useSSl, int timeout) {
        this.properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", server);
        properties.put("mail.debug", "false");
        properties.put("mail.smtp.ssl.enable", String.valueOf(useSSl));
        properties.put("mail.smtp.timeout", String.valueOf(timeout));
        properties.put("mail.smtp.connectiontimeout", String.valueOf(timeout));
        properties.put("mail.smtp.writetimeout", String.valueOf(timeout));
        properties.put("mail.smtp.ssl.checkserveridentity", "true");
        properties.put("mail.smtp.port", String.valueOf(port));
    }

    @Override
    public Properties props() {
        return (Properties) this.properties.clone();
    }
}
