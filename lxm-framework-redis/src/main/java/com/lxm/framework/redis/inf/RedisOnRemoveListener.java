package com.lxm.framework.redis.inf;

/**
 * @Author: Lys
 * @Date 2023/2/7
 * @Describe
 *
 *      只在主动删除时触发。
 *      要想实现redis到期之后触发，需要继承KeyExpirationEventMessageListener，注入lxmRedisContainer，实现onMessage接口
 *      并且，这里也仅仅能拿到过期之后的key，拿不到value
 *      因为是过期在前，触发监听在后
 **/
@FunctionalInterface
public interface RedisOnRemoveListener<T> {

    void follow(String key, T value);
}
