package com.lxm.framework.auth.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: Lys
 * @Date 2023/6/5
 * @Describe
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = "lxm-auth.redis")
public class AuthRedisProperties {

    private String host = "127.0.0.1";
    private Integer port;
    private Integer database = 2;
    private String password;

    public boolean prepared() {
        return port != null && port > 0;
    }
}
