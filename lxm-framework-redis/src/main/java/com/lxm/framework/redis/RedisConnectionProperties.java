package com.lxm.framework.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties("spring.data.redis")
public class RedisConnectionProperties {
    private String host = "localhost";
    private int port = 6379;
    private int database;
    private String username;
    private String password;
    private java.time.Duration timeout = java.time.Duration.ofSeconds(10);
}
