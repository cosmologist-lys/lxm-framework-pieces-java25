package com.lxm.framework.redis.impl.bus;

import com.lxm.framework.redis.inf.BusPublisherSession;
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
public class BusPublisher implements BusPublisherSession {

    private final RedisTemplate<String, Object> session;
    private final Set<String> topics;

    public BusPublisher(RedisTemplate<String, Object> session) {
        this.session = session;
        this.topics = new HashSet<>();
    }

    @Override
    public BusPayloadInf topic(@NonNull String topic) {
        // 因为是set，无需担心topic()和topics()来回调用
        topics.add(topic.trim());
        return new BusPayload(session, topics);
    }

    @Override
    public BusPayloadInf topics(@NonNull String... topics) {
        this.topics.addAll(Arrays.asList(topics));
        return new BusPayload(session, this.topics);
    }
}
