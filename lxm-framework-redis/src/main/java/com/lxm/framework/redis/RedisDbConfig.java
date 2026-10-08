package com.lxm.framework.redis;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.json.JsonMapper;

@AutoConfiguration
@ConditionalOnProperty(prefix = "lfp.redis", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RedisConnectionProperties.class)
public class RedisDbConfig {
    @Bean("lxmRedisConnectionFactory")
    @ConditionalOnMissingBean(name = "lxmRedisConnectionFactory")
    public JedisConnectionFactory lxmRedisConnectionFactory(RedisConnectionProperties properties) {
        var config = new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
        config.setDatabase(properties.getDatabase());
        config.setUsername(properties.getUsername());
        if (properties.getPassword() != null)
            config.setPassword(RedisPassword.of(properties.getPassword()));
        var client =
                JedisClientConfiguration.builder()
                        .connectTimeout(properties.getTimeout())
                        .readTimeout(properties.getTimeout());
        client.usePooling();
        return new JedisConnectionFactory(config, client.build());
    }

    @Bean("lxmRedisContainer")
    @ConditionalOnMissingBean(name = "lxmRedisContainer")
    public RedisMessageListenerContainer lxmRedisContainer(
            @Qualifier("lxmRedisConnectionFactory") RedisConnectionFactory factory) {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        return container;
    }

    @Bean("lxmRedisTemplate")
    @ConditionalOnMissingBean(name = "lxmRedisTemplate")
    public RedisTemplate<String, Object> lxmRedisTemplate(
            @Qualifier("lxmRedisConnectionFactory") RedisConnectionFactory factory,
            @Value("${lfp.redis.namespace:lfp:v2:}") String namespace) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        var key = new NamespacedKeySerializer(namespace);
        var value = new JacksonJsonRedisSerializer<>(JsonMapper.builder().build(), Object.class);
        template.setKeySerializer(key);
        template.setHashKeySerializer(StringRedisSerializer.UTF_8);
        template.setValueSerializer(value);
        template.setHashValueSerializer(value);
        return template;
    }

    @Bean("lxmRedisLocker")
    @ConditionalOnMissingBean(name = "lxmRedisLocker")
    public StringRedisTemplate lxmRedisLocker(
            @Qualifier("lxmRedisConnectionFactory") RedisConnectionFactory factory,
            @Value("${lfp.redis.namespace:lfp:v2:}") String namespace) {
        var template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new NamespacedKeySerializer(namespace));
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(com.lxm.framework.redis.impl.RedisClient.class)
    public com.lxm.framework.redis.impl.RedisClient redisClient(
            @Qualifier("lxmRedisTemplate") RedisTemplate<String, Object> template,
            @Qualifier("lxmRedisLocker") StringRedisTemplate locker,
            @Qualifier("lxmRedisContainer") RedisMessageListenerContainer container) {
        return new com.lxm.framework.redis.impl.RedisClient(template, locker, container);
    }
}
