package com.lxm.framework.common.cache.customized.inf;

/**
 * @Author: Lys
 * @Date 2023/1/13
 * @Describe
 **/
@FunctionalInterface
public interface CacheListener<K, V> {

    void onRemove(K k, V v);
}
