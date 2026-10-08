package com.lxm.framework.redis.inf;

/**
 * @Author: Lys
 * @Date 2023/5/9
 * @Describe
 **/
public interface MessageBusSession {

    BusSubscriberTagSession subscriber();

    BusPublisherSession publisher();
}
