package com.lxm.framework.mongo;

import com.lxm.framework.mongo.core.MongoHelper;
import com.lxm.framework.mongo.core.MongoService;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@AutoConfiguration
@ConditionalOnProperty(prefix = "lfp.mongo", name = "enabled", havingValue = "true")
public class MongoConfig {
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(MongoClient.class)
    public MongoClient lxmMongoClient(
            @Value("${spring.data.mongodb.uri:mongodb://localhost:27017}") String uri) {
        return MongoClients.create(uri);
    }

    @Bean
    @ConditionalOnMissingBean(MongoTemplate.class)
    public MongoTemplate mongoTemplate(
            MongoClient client, @Value("${spring.data.mongodb.database}") String database) {
        if (database.isBlank()) throw new IllegalArgumentException("Mongo database is required");
        return new MongoTemplate(client, database);
    }

    @Bean
    @ConditionalOnMissingBean(MongoService.class)
    public MongoService mongoService(MongoTemplate template) {
        MongoHelper.mgt = template;
        return new MongoService();
    }
}
