package com.lxm.framework.common.cache.customized;

import com.lxm.framework.common.cache.customized.impl.PermanentCache;
import com.lxm.framework.common.cache.customized.impl.TimeCache;
import com.lxm.framework.common.cache.customized.inf.CacheListener;
import lombok.NonNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/2/2
 * @Describe 封装好的静态工具类，使用者不用在意使用的具体内存是哪个。由工具自己适配。但是方法受到限制。
 **/
public class SimpleCache {

    private final static TimeCache timeCache;

    private final static PermanentCache permanentCache;

    static {
        timeCache = (TimeCache) CacheSingleton.Timed.getInstance();
        permanentCache = (PermanentCache) CacheSingleton.Permanent.getInstance();
    }

    private static <T> T translate(Object value, Class<T> tClass) {
        return Objects.isNull(value) ? null : (tClass.isInstance(value) ? (T) value : null);
    }

    /**
     * 永久保存
     *
     * @param key
     * @param value
     */
    public static void put(@NonNull Object key, @NonNull Object value) {
        permanentCache.put(key, value);
    }

    /**
     * 定时保存
     *
     * @param key
     * @param value
     * @param expire
     * @param expireUnit
     */
    public static void put(@NonNull Object key, @NonNull Object value, long expire, @NonNull TimeUnit expireUnit) {
        timeCache.put(key, value, expire, expireUnit);
    }

    /**
     * 根据key判断是否存在
     *
     * @param key
     * @return
     */
    public static boolean exist(@NonNull Object key) {
        return permanentCache.exist(key) || timeCache.exist(key);
    }

    /**
     * 获取。由于不确定是永久还是定时，因此可能两个都要试一下
     *
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T get(@NonNull Object key, @NonNull Class<T> tClass) {
        Object value = permanentCache.get(key);
        if (Objects.isNull(value)) {
            return get(key, tClass, false);
        }
        return translate(value, tClass);
    }

    /**
     * 获取，存在refresh字段，必定是定时缓存
     *
     * @param key
     * @param tClass
     * @param refresh
     * @param <T>
     * @return
     */
    public static <T> T get(@NonNull Object key, @NonNull Class<T> tClass, boolean refresh) {
        Object value = timeCache.get(key, refresh);
        return translate(value, tClass);
    }

    /**
     * 获取并销毁。必定是定时缓存
     *
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T getThenRemove(@NonNull Object key, @NonNull Class<T> tClass) {
        Object value = timeCache.getThenRemove(key);
        return translate(value, tClass);
    }

    /**
     * 对定时缓存设置全局通用的过期监听
     *
     * @param cacheListener
     */
    public static void setGlobalExpireListener(@NonNull CacheListener cacheListener) {
        timeCache.expireListener(cacheListener);
    }

    /**
     * 移除缓存。由于不确定是哪种类型，因此都要尝试一下
     *
     * @param key
     */
    public static void remove(@NonNull Object key) {
        boolean exist = permanentCache.exist(key);
        if (exist) {
            permanentCache.remove(key);
            return;
        }
        exist = timeCache.exist(key);
        if (exist) {
            timeCache.remove(key);
        }
    }

    /**
     * 当前总size
     *
     * @return
     */
    public static int holderSize() {
        return permanentCache.size() + timeCache.size();
    }


}
