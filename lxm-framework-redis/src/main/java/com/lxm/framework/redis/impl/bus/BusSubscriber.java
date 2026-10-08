package com.lxm.framework.redis.impl.bus;

import com.lxm.framework.redis.inf.BusSubscriberSession;
import lombok.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 **/
public class BusSubscriber implements BusSubscriberSession {

    private final RedisTemplate<String, Object> session;
    private final RedisMessageListenerContainer container;
    private final Boolean isOnce;
    private final Set<String> topics;

    public BusSubscriber(
            RedisTemplate<String, Object> session,
            RedisMessageListenerContainer container,
            Boolean isOnce) {
        this.session = session;
        this.container = container;
        this.isOnce = isOnce;
        topics = new HashSet<>();
    }

    @Override
    public BusConsumerInf topic(@NonNull String topic) {
        // 因为是set，无需担心topic()和topics()来回调用
        topics.add(topic.trim());
        return new BusConsumer(session, container, this.topics, isOnce);
    }

    @Override
    public BusConsumerInf topics(@NonNull String @NonNull ... topics) {
        this.topics.addAll(Arrays.asList(topics));
        return new BusConsumer(session, container, this.topics, isOnce);
    }
}
