package com.lxm.framework.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import com.lxm.framework.web.jackon.JacksonConfig;
import com.lxm.framework.web.jsonresult.advice.JsonResultAdvice;
import com.lxm.framework.web.jsonresult.handler.GlobalExceptionHandler;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix="lfp.web", name="enabled", matchIfMissing=true)
public class WebAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name="lxmJsonModule")
    public tools.jackson.databind.module.SimpleModule lxmJsonModule() { return JacksonConfig.module(); }
    @Bean
    @ConditionalOnMissingBean(JsonResultAdvice.class)
    public JsonResultAdvice jsonResultAdvice() { return new JsonResultAdvice(); }
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() { return new GlobalExceptionHandler(); }
}
