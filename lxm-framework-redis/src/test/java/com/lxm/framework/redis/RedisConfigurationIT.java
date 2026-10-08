package com.lxm.framework.redis;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import redis.clients.jedis.RedisClient;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RedisConfigurationIT {
    @Test
    void namespacesOldValuesAndClosesManagedSubscriptionContainer() {
        int port = Integer.getInteger("lfp.redis.port", 16379);
        String namespace = "lfp:test:" + UUID.randomUUID() + ":";
        RedisMessageListenerContainer container;
        try (var nativeClient = RedisClient.create("127.0.0.1", port);
                var context = new AnnotationConfigApplicationContext()) {
            nativeClient.set(namespace + "legacy", "[\"untrusted.Type\",{}]");
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    "test",
                                    Map.of(
                                            "lfp.redis.enabled",
                                            "true",
                                            "spring.data.redis.host",
                                            "127.0.0.1",
                                            "spring.data.redis.port",
                                            Integer.toString(port),
                                            "lfp.redis.namespace",
                                            namespace + "v2:")));
            context.register(RedisDbConfig.class);
            context.refresh();
            var template = context.getBean("lxmRedisTemplate", RedisTemplate.class);
            template.opsForValue().set("item", Map.of("name", "中文", "number", 7));
            assertEquals(Map.of("name", "中文", "number", 7), template.opsForValue().get("item"));
            assertNull(template.opsForValue().get("legacy"));
            assertTrue(nativeClient.exists(namespace + "v2:item"));
            container = context.getBean(RedisMessageListenerContainer.class);
            assertTrue(container.isRunning());
            template.delete("item");
            nativeClient.del(namespace + "legacy");
            context.close();
            assertFalse(container.isRunning());
        }
    }
}
