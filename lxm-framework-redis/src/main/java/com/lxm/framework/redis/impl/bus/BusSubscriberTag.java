package com.lxm.framework.redis.impl.bus;

import com.lxm.framework.redis.inf.BusSubscriberSession;
import com.lxm.framework.redis.inf.BusSubscriberTagSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @Author: Lys
 * @Date 2023/5/19
 * @Describe
 **/
public class BusSubscriberTag implements BusSubscriberTagSession {

    private final RedisTemplate<String, Object> session;
    private final RedisMessageListenerContainer container;


    public BusSubscriberTag(RedisTemplate<String, Object> session, RedisMessageListenerContainer container) {
        this.session = session;
        this.container = container;
    }

    @Override
    public BusSubscriberSession persist() {
        return new BusSubscriber(session,container,false);
    }

    @Override
    public BusSubscriberSession once() {
        return new BusSubscriber(session,container,true);
    }
}
