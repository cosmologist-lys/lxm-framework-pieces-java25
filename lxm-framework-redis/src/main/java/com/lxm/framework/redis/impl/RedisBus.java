package com.lxm.framework.redis.impl;

import com.lxm.framework.redis.impl.bus.BusPublisher;
import com.lxm.framework.redis.impl.bus.BusSubscriber;
import com.lxm.framework.redis.impl.bus.BusSubscriberTag;
import com.lxm.framework.redis.inf.BusPublisherSession;
import com.lxm.framework.redis.inf.BusSubscriberTagSession;
import com.lxm.framework.redis.inf.MessageBusSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @Author: Lys
 * @Date 2023/5/9
 * @Describe
 **/
@Slf4j
public class RedisBus implements MessageBusSession {

    private final RedisTemplate<String, Object> session;
    private final RedisMessageListenerContainer container;

    private RedisBus(
            RedisTemplate<String, Object> session, RedisMessageListenerContainer container) {
        this.session = session;
        this.container = container;
    }

    public static RedisBus of(
            RedisTemplate<String, Object> session, RedisMessageListenerContainer container) {
        return new RedisBus(session, container);
    }

    @Override
    public BusSubscriberTagSession subscriber() {
        return new BusSubscriberTag(session, container);
    }

    @Override
    public BusPublisherSession publisher() {
        return new BusPublisher(session);
    }
}
