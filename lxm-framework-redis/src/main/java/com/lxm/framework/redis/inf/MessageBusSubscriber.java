package com.lxm.framework.redis.inf;

/**
 * @Author: Lys
 * @Date 2023/5/10
 * @Describe
 **/
@FunctionalInterface
public interface MessageBusSubscriber {

    void onMessage(Object payload, String topic);
}
