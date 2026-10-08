package com.lxm.framework.common.cache.customized.impl;

import com.lxm.framework.common.AppException;
import com.lxm.framework.common.cache.customized.inf.AbstractCache;
import com.lxm.framework.common.cache.customized.parts.CacheScheduleScanner;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/1/17
 * @Describe 具有定时功能的缓存。存储的对象必须指定过期时间，否则抛错
 **/
@Slf4j
public class TimeCache<K, V> extends AbstractCache<K, V> implements AutoCloseable {

    private static final long serialVersionUID = 5532645361538653907L;
    private final CacheScheduleScanner<K, V> scanner;

    /**
     * 构造器
     *
     * @param initialCapacity 初始化大小
     * @param period          定时器检查过期key的扫描时间间隔
     * @param timeUnit        时间单位
     */
    protected TimeCache(int initialCapacity, int period, TimeUnit timeUnit) {
        if (initialCapacity <= 0) {
            initialCapacity = 1024;
        }
        this.cacheMap = new ConcurrentHashMap<>(initialCapacity);
        this.scanner = CacheScheduleScanner.build(this, period, timeUnit);
        this.scanner.scan();
    }

    @Override
    public boolean isFull() {
        return false;
    }

    public void closeScanner() {
        this.scanner.close();
    }

    @Override
    public void close() { closeScanner(); }

    @Override
    public int size() {
        return this.cacheMap.size();
    }


    /**
     * 对于定时的缓存，必须指定存放的时间。不然就应该使用permanentCache
     *
     * @param key
     * @param value
     */
    @Override
    public void put(K key, V value) {
        throw new AppException(-1, "requires expire time of time-cache");
    }

}
