package com.lxm.framework.redis.inf;

/**
 * @Author: Lys
 * @Date 2023/5/19
 * @Describe
 **/
public interface BusSubscriberTagSession {

    BusSubscriberSession persist();

    BusSubscriberSession once();
}
