package com.lxm.framework.enigma;

import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import com.lxm.framework.enigma.mvc.*;
import com.lxm.framework.enigma.protocol.*;
import com.lxm.framework.enigma.spi.*;
import com.lxm.framework.enigma.store.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.time.Clock;

@AutoConfiguration(afterName="org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration")
@ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix="lfp.enigma",name="enabled",havingValue="true")
@EnableConfigurationProperties(EnigmaProperties.class)
public class EnigmaAutoConfiguration {
    @Bean @ConditionalOnMissingBean(EnigmaCrypto.class)
    public EnigmaCrypto enigmaCrypto() { return new EnigmaCrypto(); }
    @Bean @ConditionalOnMissingBean(Clock.class)
    public Clock enigmaClock() { return Clock.systemUTC(); }
    @Bean @ConditionalOnMissingBean(SessionStore.class) @ConditionalOnProperty(prefix="lfp.enigma",name="store",havingValue="memory")
    public MemorySessionStore memorySessionStore(EnigmaProperties properties,Clock clock) { properties.validate();return new MemorySessionStore(properties,clock); }
    @Bean @ConditionalOnMissingBean(SessionStore.class) @ConditionalOnProperty(prefix="lfp.enigma",name="store",havingValue="redis") @ConditionalOnClass(name="redis.clients.jedis.JedisPooled")
    public RedisSessionStore redisSessionStore(EnigmaProperties properties,EnigmaCrypto crypto,Clock clock) { properties.validate();return new RedisSessionStore(properties,crypto,clock); }
    @Bean @ConditionalOnMissingBean(EnigmaSessionService.class)
    public EnigmaSessionService enigmaSessionService(SessionStore store,EnigmaProperties properties,EnigmaCrypto crypto,Clock clock) {
        return new EnigmaSessionService(store,properties,crypto,clock);
    }
    @Bean @ConditionalOnMissingBean(EnigmaProtocol.class)
    public EnigmaProtocol enigmaProtocol(EnigmaCrypto crypto,EnigmaSessionService sessions,EnigmaProperties properties,Clock clock) { return new EnigmaProtocol(crypto,sessions,properties,clock); }
    @Bean
    public EnigmaSessionController enigmaSessionController(EnigmaSessionService sessions,EnigmaIdentityResolver identities,EnigmaProperties properties) { return new EnigmaSessionController(sessions,identities,properties); }
    @Bean
    public EnigmaExceptionAdvice enigmaExceptionAdvice() { return new EnigmaExceptionAdvice(); }
    @Bean
    public EnigmaBodyAdvice enigmaBodyAdvice(EnigmaProtocol protocol,EnigmaProperties properties) { return new EnigmaBodyAdvice(protocol,properties); }
    @Bean
    public EnigmaInterceptor enigmaInterceptor(EnigmaProperties properties,EnigmaIdentityResolver identities,EnigmaSessionService sessions,EnigmaProtocol protocol) { return new EnigmaInterceptor(properties,identities,sessions,protocol); }
    @Bean
    public FilterRegistrationBean<EnigmaResponseFilter> enigmaResponseFilter(EnigmaProtocol protocol,EnigmaProperties properties) {
        var bean=new FilterRegistrationBean<>(new EnigmaResponseFilter(protocol,properties));bean.setOrder(-10000);bean.setAsyncSupported(false);return bean;
    }
    @Bean
    public WebMvcConfigurer enigmaMvcConfigurer(EnigmaInterceptor interceptor,EnigmaProperties properties) {
        return new WebMvcConfigurer() {
            public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(interceptor).order(-10000); }
            public void addCorsMappings(CorsRegistry registry) {
                if (!properties.getAllowedOrigins().isEmpty()) registry.addMapping("/**").allowedOrigins(properties.getAllowedOrigins().toArray(String[]::new))
                    .allowCredentials(true).allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
                    .allowedHeaders("Authorization","Content-Type","X-CSRF-Token","X-Enigma-Version","X-Enigma-Mode","X-Enigma-Session","X-Enigma-Kid","X-Enigma-Timestamp","X-Enigma-Nonce","X-Enigma-Sign")
                    .exposedHeaders("X-Enigma-Version","X-Enigma-Mode","X-Enigma-Session","X-Enigma-Kid","X-Enigma-Timestamp","X-Enigma-Nonce","X-Enigma-Unexecuted");
            }
        };
    }
    @Bean
    public SmartInitializingSingleton enigmaEndpointValidator(RequestMappingHandlerMapping mappings) {
        return ()->mappings.getHandlerMethods().forEach((mapping,handler)->EnigmaInterceptor.validate(handler,mapping));
    }
}
