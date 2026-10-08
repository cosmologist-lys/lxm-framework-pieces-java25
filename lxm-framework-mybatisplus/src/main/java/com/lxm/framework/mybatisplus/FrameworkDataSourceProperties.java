package com.lxm.framework.mybatisplus;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("spring.datasource")
public class FrameworkDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String name;
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
}
