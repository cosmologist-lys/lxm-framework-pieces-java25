package com.lxm.framework.redis.inf;

import java.util.function.BiConsumer;

/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 **/
public interface BusSubscriberSession extends BusTopic<BusSubscriberSession.BusConsumerInf> {


    interface BusConsumerInf {
        BusSubInf consumer(BiConsumer<String,Object> consumer);
    }

    interface BusSubInf {
        void subscribe();
    }
}
