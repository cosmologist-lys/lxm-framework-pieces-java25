package com.lxm.framework.auth.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "lxm-auth.token")
public class LxmTokenProperties {

    /**
     * token名称 (同时也是cookie名称)
     */
    private String tokenName = "lxmtoken";

    /**
     * token的长久有效期(单位:秒) 默认1天,-1代表一次性
     */
    private long timeout = 24 * 60 * 60;

    /**
     * 一次性token，对于不登录，但又临时授予一个游客账号身份，有效期1小时
     */
    private long onceTimeout = 60 * 60;

    /**
     * 是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录)
     */
    private Boolean allowConcurrentLogin = true;

    /**
     * 在多人登录同一账号时，是否共用一个token (为true时所有登录共用一个token, 为false时每次登录新建一个token)
     */
    private Boolean isShare = true;

    /**
     * 是否尝试从请求体里读取token
     */
    private Boolean isReadParameter = true;

    /**
     * 是否尝试从header里读取token
     */
    private Boolean isReadHead = true;

    /**
     * 是否尝试从cookie里读取token
     */
    private Boolean isReadCookie = true;

    /**
     * token风格(默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik)
     */
    private String tokenStyle = "random-64";

    /**
     * 是否打开自动续签 (如果此值为true, 框架会在每次直接或间接调用getLoginId()时进行一次过期检查与续签操作)
     */
    private Boolean autoRenew = true;

    /**
     * 写入Cookie时显式指定的作用域, 常用于单点登录二级域名共享Cookie的场景
     */
    private String cookieDomain;

    /**
     * token前缀
     */
    private String tokenPrefix;
}
