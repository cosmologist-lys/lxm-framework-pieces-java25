package com.lxm.framework.common.cache.customized;

import com.lxm.framework.common.cache.customized.impl.CacheBuilder;
import com.lxm.framework.common.cache.customized.inf.Cache;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/2/1
 * @Describe  全局单例。注意，SimpleCache里使用的也是它。这里暴露出来的目的，是可以通过手动获取的方式使用到cache本身更多的功能。
 **/
public enum CacheSingleton {

    Permanent(CacheBuilder.newPermanentCache(128)),
    Timed(CacheBuilder.newTimeCache(128, 30, TimeUnit.SECONDS));

    CacheSingleton(Cache instance) {
        this.instance = instance;
    }

    private final Cache instance;

    public Cache getInstance() {
        return this.instance;
    }
}
