package com.lxm.framework.common.cache.customized.impl;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/1/18
 * @Describe
 **/
public class CacheBuilder {

    /**
     * 创建一个具有定时清除功能的缓存
     *
     * @param initialCapacity  初始大小
     * @param period  定时扫描过期key的时间间隔
     * @param timeUnit  定时扫描过期key的时间单位
     * @param <K>  泛型
     * @param <V>  泛型
     * @return
     */
    public static <K, V> TimeCache<K, V> newTimeCache(
            int initialCapacity, int period, TimeUnit timeUnit) {
        return new TimeCache<K, V>(initialCapacity, period, timeUnit);
    }

    /**
     * 创建一个简单缓存，即是永久存储的缓存器
     *
     * @param initialCapacity
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> PermanentCache<K, V> newPermanentCache(int initialCapacity) {
        return new PermanentCache<K, V>(initialCapacity);
    }
}
