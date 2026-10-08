package com.lxm.framework.common.cache.customized.parts;

import com.lxm.framework.common.cache.customized.inf.Cache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Lys
 * @Date 2023/1/13
 * @Describe
 **/
@Slf4j
public class CacheScheduleScanner<K, V> {

    /**
     * 定时任务
     */
    private final ScheduledExecutorService scanner;
    /**
     * 扫描周期
     */
    private final int period;
    /**
     * 时间单位
     */
    private final TimeUnit timeUnit;

    private final Cache<K, V> cache;

    private final AtomicInteger count;

    private CacheScheduleScanner(Cache<K, V> cache, int period, TimeUnit timeUnit) {
        this.period = period;
        this.timeUnit = timeUnit;
        this.cache = cache;
        this.count = new AtomicInteger(0);
        if (period <= 0 || timeUnit == null) throw new IllegalArgumentException("Positive scan period required");
        this.scanner = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "lfp-cache-scanner");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static <K, V> CacheScheduleScanner<K, V> build(Cache<K, V> cache, int period, TimeUnit timeUnit) {
        return new CacheScheduleScanner<K, V>(cache, period, timeUnit);
    }

    public void scan() {
        scanner.scheduleAtFixedRate(() -> {
            try {
                int scanCount = this.count.getAndIncrement();
                if (scanCount == 0){
                    log.debug("schedule-scanner init");

                    return;
                }
                String threadTag = Thread.currentThread().getName();
                log.debug("thead : {} ,scan-count : {}, cache-size : {}", threadTag, scanCount, cache.size());
                for (CacheTarget<K, V> pair : cache) {
                    // 检查当前映射，而非删除扫描前已被替换的旧对象。
                    cache.exist(pair.getKey());
                }
            } catch (Exception e) {
                log.error("cache-schedule-scanner error", e);
            }
        }, 0, period, timeUnit);
    }

    public void close() {
        this.scanner.shutdown();
    }

    //=======================

    public int getPeriod() {
        return period;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public Cache<K, V> getCacheTarget() {
        return cache;
    }
}
