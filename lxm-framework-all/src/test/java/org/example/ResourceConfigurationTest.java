package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import com.lxm.framework.redis.RedisDbConfig;
import com.lxm.framework.mongo.MongoConfig;
import com.lxm.framework.mybatisplus.MybatisPlusConfig;
import static org.junit.jupiter.api.Assertions.*;

class ResourceConfigurationTest {
    @org.springframework.context.annotation.Configuration(proxyBeanMethods=false)
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration
    static class Consumer { }
    @Test void actualConsumerOutsideFrameworkPackageStartsWithAllAndNoServices() {
        var app=new org.springframework.boot.SpringApplication(Consumer.class);
        app.setDefaultProperties(java.util.Map.of("server.port","0","spring.main.banner-mode","off","logging.level.root","ERROR"));
        try(var context=app.run()) {
            assertFalse(context.containsBean("lxmRedisConnectionFactory"));
            assertFalse(context.containsBean("lxmMongoClient"));
            assertFalse(context.containsBean("beeDataSource"));
            assertFalse(context.containsBean("enigmaSessionService"));
            assertTrue(context.containsBean("jsonResultAdvice"));
        }
    }
    @Test void aggregateDependencyDoesNotCreateResourceClientsByDefault() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(RedisDbConfig.class,MongoConfig.class,MybatisPlusConfig.class))
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertFalse(context.containsBean("lxmRedisConnectionFactory"));
                assertFalse(context.containsBean("lxmMongoClient"));
                assertFalse(context.containsBean("beeDataSource"));
            });
    }
}
