package com.lxm.framework.email.enums;

import java.util.Properties;

/**
 * @Author: Lys
 * @Date 2022/9/15
 * @Describe
 **/
public enum EmailCategoryRecipient implements EmailCategory {
    QQ("pop.qq.com", 995, true, 10000),
    ;

    private final Properties properties;

    EmailCategoryRecipient(String server, int port, boolean useSSl, int timeout) {
        this.properties = new Properties();
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.pop3.host", server);
        properties.put("mail.smtp.port", String.valueOf(port));
        properties.put("mail.debug", "false");
        properties.put("mail.smtp.ssl.enable", String.valueOf(useSSl));
        properties.put("mail.smtp.timeout", String.valueOf(timeout));
        properties.put("mail.smtp.connectiontimeout", String.valueOf(timeout));
        properties.put("mail.smtp.writetimeout", String.valueOf(timeout));
        properties.put("mail.smtp.ssl.checkserveridentity", "true");
    }

    @Override
    public Properties props() {
        return (Properties) this.properties.clone();
    }
}
