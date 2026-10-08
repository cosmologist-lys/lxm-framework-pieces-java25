package com.lxm.framework.redis.inf;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/2/6
 * @Describe
 **/
public interface GenericSession {

    GenericSession key(String key);

    GenericSession expire(long time, TimeUnit timeUnit);

    default GenericSession persist() {
        return expire(0, null);
    }

    GenericSession prefix(String prefix);

    default GenericSession prefix(KeyPrefixRule prefixRule) {
        return prefix(prefixRule.getPrefix());
    }

    void delete();

    void deleteBatch();

    StoreSession use();

    /**
     * 设置监听，仅在redis主动移除某个value时触发
     * 但：对于因为时间到期的移除无效，因为这是两个不同场景。如果需要这个功能，考虑使用redis自带的expire-listener
     * @param redisOnRemoveListener
     * @return
     */
    GenericSession listener(RedisOnRemoveListener redisOnRemoveListener);

}
