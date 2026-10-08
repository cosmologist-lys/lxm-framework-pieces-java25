package com.lxm.framework.auth.redis;

import com.lxm.framework.auth.model.AuthManager;
import com.lxm.framework.auth.model.LxmTokenDao;
import com.lxm.framework.auth.model.defaults.TokenDaoRedisDefault;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @Author: Lys
 * @Date 2023/6/9
 * @Describe   只要配置了 {@link com.lxm.framework.auth.redis.AuthRedisProperties}，就会有一个默认的基于jedis实现的redis-dao
 *              但是如果嫌弃这个默认的实现，非要自己写，也可以。框架会优先使用自己写的那一个
 *
 *              如果没有配置{@link com.lxm.framework.auth.redis.AuthRedisProperties}，
 *              也会有一个基于 {@link com.lxm.framework.common.cache.customized.impl.TimeCache} 的内存的dao实现
 **/
@Slf4j

@EnableConfigurationProperties(AuthRedisProperties.class)
public class AuthRedisDefaultPrepare {

    private final ApplicationContext applicationContext;

    @Autowired
    public AuthRedisDefaultPrepare(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 这里注册默认的redis-dao，2个前提
     * 1：没有自定义的tokenDao
     * 2：auth-redis-properties.prepare = true
     * @param properties
     */
    @Autowired
    public void registerDaoRedisDefault(AuthRedisProperties properties) {
        LxmTokenDao dao = null;
        try {
            dao = applicationContext.getBean(LxmTokenDao.class);
            log.debug("lxm-auth redis default prepare , customized dao has been set.");
        } catch (NoSuchBeanDefinitionException e) {
            if (!properties.prepared()) {
                log.debug("lxm-auth redis default prepare , properties is not set , ignore dao redis default");
                return;
            }
            throw new IllegalStateException("Enable lfp.auth.enabled or supply a managed LxmTokenDao bean");
        }
        AuthManager.setTokenDao(dao);
    }
}
