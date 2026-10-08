package com.lxm.framework.redis.impl.storage;

import com.lxm.framework.redis.cache.ListenerCache;
import com.lxm.framework.redis.inf.RedisOnRemoveListener;
import com.lxm.framework.redis.parts.StoreCore;

/**
 * @Author: Lys
 * @Date 2023/5/18
 * @Describe
 **/
public class StorageCleanser {

    private final StoreCore core;

    public StorageCleanser(StoreCore core) {
        this.core = core;
    }

    public void onRemove(String key, Object value) {
        RedisOnRemoveListener listener = core.getListener();
        if (null == listener) {
            listener = ListenerCache.getListenerThenRemove(core.getPrefix(), core.getKey());
        }
        if (null != listener) {
            listener.follow(key, value);
        }
    }
}
