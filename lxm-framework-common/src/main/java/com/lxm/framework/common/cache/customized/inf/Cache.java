package com.lxm.framework.common.cache.customized.inf;

import com.lxm.framework.common.cache.customized.parts.CacheTarget;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Author: Lys
 * @Date 2023/1/13
 * @Describe
 **/
public interface Cache<K, V> extends Serializable, Iterable<CacheTarget<K, V>> {

    /**
     * 不传入时间，默认永久存储
     *
     * @param key
     * @param value
     */
    default void put(K key, V value) {
        put(key, value, 0, null);
    }

    /**
     * 完整的存储方式
     *
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     */
    void put(K key, V value, long timeout, TimeUnit timeUnit);

    default V get(K key) {
        return get(key, false);
    }

    V get(K key, boolean refresh);

    /**
     * get不到的时候，做otherwise的事情
     *
     * @param key       key
     * @param otherwise 不然就做。。
     * @return
     */
    default V get(K key, Function<K, V> otherwise) {
        V value = get(key);
        if (null != value) {
            return value;
        }
        return otherwise.apply(key);
    }

    V getThenRemove(K key);

    boolean exist(K key);

    boolean isFull();

    long getTimeout(K key);

    void remove(K key);

    void clear();

    int size();

    void onRemove(K key, V value);

    void expireListener(CacheListener<K, V> cacheListener);
}
