package com.lxm.framework.common.cache.customized.impl;

import com.lxm.framework.common.AppException;
import com.lxm.framework.common.cache.customized.inf.AbstractCache;
import com.lxm.framework.common.cache.customized.inf.CacheListener;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Lys
 * @Date 2023/1/13
 * @Describe  永久缓存。即如果不清理，就永远存在。慎用
 **/
public class PermanentCache<K, V> extends AbstractCache<K, V> {

    public PermanentCache(int capacity) {
        if (capacity < 128) {
            capacity = 128;
        }
        this.cacheMap = new ConcurrentHashMap<>(capacity);
    }

    private static final long serialVersionUID = -338613525114348344L;

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public int size() {
        return this.cacheMap.size();
    }

    /**
     * 普通cache因为没有定时功能，所以不支持过时监听的设置
     * @param listener
     */
    @Override
    public void expireListener(CacheListener<K, V> listener) {
        throw new AppException(-1, "only unsupported for time-schedule cache");
    }
}
