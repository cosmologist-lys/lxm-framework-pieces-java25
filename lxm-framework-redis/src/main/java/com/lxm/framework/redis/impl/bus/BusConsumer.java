package com.lxm.framework.redis.impl.bus;

import com.lxm.framework.redis.inf.BusSubscriberSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 **/
public class BusConsumer implements BusSubscriberSession.BusConsumerInf {

    private final RedisTemplate<String, Object> session;
    private final RedisMessageListenerContainer container;
    private final Set<String> topics;
    private final Boolean isOnce;


    public BusConsumer(RedisTemplate<String, Object> session, RedisMessageListenerContainer container, Set<String> topics, Boolean isOnce) {
        this.session = session;
        this.container = container;
        this.topics = topics;
        this.isOnce = isOnce;
    }

    @Override
    public BusSubscriberSession.BusSubInf consumer(BiConsumer<String, Object> consumer) {
        var list = new ArrayList<BusSub>();
        for (String topic : topics) {
            list.add(new BusSub(session, container, consumer, topic, isOnce));
        }
        return new BusMultiSub(list);
    }
}
