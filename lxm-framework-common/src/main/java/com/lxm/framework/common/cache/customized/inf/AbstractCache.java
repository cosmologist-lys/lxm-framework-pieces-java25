package com.lxm.framework.common.cache.customized.inf;


import com.lxm.framework.common.cache.customized.impl.ImmutableKey;
import com.lxm.framework.common.cache.customized.parts.CacheTarget;
import com.lxm.framework.common.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @Author: Lys
 * @Date 2023/1/13
 * @Describe
 **/
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private static final long serialVersionUID = -7656172679665345803L;

    protected CacheListener<K, V> listener;

    protected Map<Immutable<K>, CacheTarget<K, V>> cacheMap;
    /**
     * 访问次数，get，set方法每调用一次，+1
     */
    protected LongAdder visitCount = new LongAdder();

    @Override
    public synchronized boolean exist(K key) {
        Immutable<K> mutableKey = ImmutableKey.of(key);
        return exist(mutableKey);
    }

    protected boolean exist(Immutable<K> key) {
        CacheTarget<K, V> v = cacheMap.get(key);
        return safe(v);
    }

    /**
     * 存储对象是否安全。不安全的定义：不存在，或过期
     * 如果过期，则删除
     *
     * @param target
     * @return
     */
    protected boolean safe(CacheTarget<K, V> target) {
        if (target == null) return false;
        if (target.isExpired()) {
            if (cacheMap.remove(ImmutableKey.of(target.getKey()), target)) onRemove(target.getKey(), target.getValue());
            return false;
        }
        return true;
    }

    @Override
    public synchronized void put(K key, V value, long timeout, TimeUnit timeUnit) {
        visitCount.increment();
        cacheMap.put(ImmutableKey.of(key), new CacheTarget<>(key, value, timeout,
            timeUnit == null ? TimeUnit.MINUTES : timeUnit));
    }

    @Override
    public synchronized V get(K key, boolean refresh) {
        visitCount.increment();
        var target = cacheMap.get(ImmutableKey.of(key));
        if (!safe(target)) return null;
        if (refresh) target.refresh();
        return target.getValue();
    }

    @Override
    public synchronized V getThenRemove(K key) {
        var target = cacheMap.remove(ImmutableKey.of(key));
        if (target == null) return null;
        onRemove(key, target.getValue());
        return target.isExpired() ? null : target.getValue();
    }

    /** 缺失或过期为 -1，永久为 0，其余为剩余毫秒。 */
    @Override
    public synchronized long getTimeout(K key) {
        var target = cacheMap.get(ImmutableKey.of(key));
        if (!safe(target)) return -1;
        return target.getExpiredTime() == null ? 0 : Math.max(0,
            DateTimeUtils.toMillis(target.getExpiredTime()) - System.currentTimeMillis());
    }

    /**
     * 删除并且通知监听
     *
     * @param key
     * @param value
     */
    public void onRemove(K key, V value) {
        if (Objects.nonNull(this.listener)) {
            this.listener.onRemove(key, value);
        }
    }

    /**
     * 删除，同时触发监听
     *
     * @param key
     */
    @Override
    public synchronized void remove(K key) {
        var removed = cacheMap.remove(ImmutableKey.of(key));
        if (removed != null) onRemove(key, removed.getValue());
    }

    public long visitCount() {
        return visitCount.sum();
    }

    @Override
    public synchronized void clear() {
        for (var target : java.util.List.copyOf(cacheMap.values())) remove(target.getKey());
    }

    @Override
    public Iterator<CacheTarget<K, V>> iterator() {
        return this.cacheMap.values().iterator();
    }

    public void expireListener(CacheListener<K, V> listener) {
        this.listener = listener;
    }
}
