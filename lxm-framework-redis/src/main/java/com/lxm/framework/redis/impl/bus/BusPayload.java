package com.lxm.framework.redis.impl.bus;

import com.lxm.framework.redis.inf.BusPublisherSession;
import lombok.NonNull;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 **/
public class BusPayload implements BusPublisherSession.BusPayloadInf {

    private final RedisTemplate<String, Object> session;
    private final Set<String> topics;

    public BusPayload(RedisTemplate<String, Object> session,  Set<String> topics) {
        this.session = session;
        this.topics = topics;
    }

    @Override
    public BusPublisherSession.BusPubInf payload(@NonNull Object payload) {
        return new BusPub(session,topics,payload);
    }
}
