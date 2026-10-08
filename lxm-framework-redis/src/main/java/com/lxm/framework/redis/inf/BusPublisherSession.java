package com.lxm.framework.redis.inf;

import lombok.NonNull;

/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 **/
public interface BusPublisherSession extends BusTopic<BusPublisherSession.BusPayloadInf> {

    interface BusPayloadInf {
        BusPubInf payload(@NonNull Object payload);
    }

    interface BusPubInf {
        void publish();
    }
}
