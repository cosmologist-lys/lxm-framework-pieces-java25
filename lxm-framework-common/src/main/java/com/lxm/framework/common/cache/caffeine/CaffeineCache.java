package com.lxm.framework.common.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/1/9
 * @Describe
 **/
public class CaffeineCache {

    private static final Cache<String, Object> cache;

    static {
        cache =
                Caffeine.newBuilder()
                        // 默认缓存1小时最多
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .maximumSize(50000L)
                        .initialCapacity(1000)
                        .build();
    }

    public static boolean hasKey(String key) {
        return Objects.nonNull(cache.getIfPresent(key));
    }

    public static <T> T read(String key, Class<T> tClass) {
        var val = cache.getIfPresent(key);
        if (Objects.isNull(val)) {
            return null;
        }
        if (val.getClass().equals(tClass)) {
            return (T) val;
        }
        return null;
    }

    public static Object read(String key) {
        return cache.getIfPresent(key);
    }

    public static void write(String key, Object value) {
        cache.put(key, value);
    }
}
