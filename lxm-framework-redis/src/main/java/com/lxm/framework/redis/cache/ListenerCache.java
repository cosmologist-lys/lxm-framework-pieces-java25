package com.lxm.framework.redis.cache;

import com.lxm.framework.common.cache.customized.impl.PermanentCache;
import com.lxm.framework.redis.consts.RedisDefaultConstant;
import com.lxm.framework.redis.inf.RedisOnRemoveListener;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: Lys
 * @Date 2023/2/7
 * @Describe
 **/
public class ListenerCache {

    private static final PermanentCache<String, RedisOnRemoveListener> cache;

    static {
        cache = new PermanentCache<String, RedisOnRemoveListener>(128);
    }

    public static void putListener(String prefix, @NonNull String key, @NonNull RedisOnRemoveListener listener) {
        String tag = formatKey(prefix, key);
        cache.put(tag, listener);
    }

    public static RedisOnRemoveListener getListenerThenRemove(String prefix, @NonNull String key) {
        String tag = formatKey(prefix, key);
        RedisOnRemoveListener listener = cache.get(tag);
        cache.remove(tag);
        return listener;
    }

    public static RedisOnRemoveListener getListenerThenRemove(@NonNull String formatKey){
        final RedisOnRemoveListener redisOnRemoveListener = cache.get(formatKey);
        if (null != redisOnRemoveListener){
            cache.remove(formatKey);
        }
        return redisOnRemoveListener;
    }

    private static String formatKey(String prefix, String key) {
        String tag = StringUtils.isNotBlank(prefix) ? prefix.concat(key) : key;
        return RedisDefaultConstant.LISTENER_PREFIX.concat(tag);
    }
}
