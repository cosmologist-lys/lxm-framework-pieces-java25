package com.lxm.framework.redis.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * @Author: Lys
 * @Date 2023/2/7
 * @Describe 这里提供redis的实例
 **/

public class RedisClient {

    private static RedisTemplate redisSession;
    private static StringRedisTemplate lockSession;
    private static RedisMessageListenerContainer redisContainer;

    @Autowired
    public RedisClient(@Qualifier("lxmRedisTemplate") RedisTemplate lxmRedisTemplate,
                       @Qualifier("lxmRedisLocker") StringRedisTemplate lxmRedisLocker,
                       @Qualifier("lxmRedisContainer") RedisMessageListenerContainer lxmRedisContainer) {
        redisSession = lxmRedisTemplate;
        lockSession = lxmRedisLocker;
        redisContainer = lxmRedisContainer;
    }


    public static RedisStorage storage() {
        return RedisStorage.of(redisSession);
    }

    public static RedisLocker locker() {
        return RedisLocker.of(lockSession);
    }

    public static RedisBus messageBus(){
        return RedisBus.of(redisSession,redisContainer);
    }

}
