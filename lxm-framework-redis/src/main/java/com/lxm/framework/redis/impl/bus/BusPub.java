package com.lxm.framework.redis.impl.bus;

import com.lxm.framework.redis.inf.BusPublisherSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Set;

/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 **/
@Slf4j
public class BusPub implements BusPublisherSession.BusPubInf {

    private final RedisTemplate<String, Object> session;
    private final Set<String> topics;
    private final Object payload;

    public BusPub(RedisTemplate<String, Object> session, Set<String> topics, Object payload) {
        this.session = session;
        this.topics = topics;
        this.payload = payload;
    }

    @Override
    public void publish() {
        log.debug("lxm-redis-message-bus publisher , topics : {}", topics);
        for (String topic : topics) {
            session.convertAndSend(topic, payload);
        }
    }
}
