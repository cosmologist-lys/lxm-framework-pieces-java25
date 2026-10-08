package com.lxm.framework.auth.model;

import com.lxm.framework.auth.filter.PrincipleFilter;
import com.lxm.framework.auth.router.LxmPathMatcherHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;

/**
 * @Author: Lys
 * @Date 2023/6/7
 * @Describe 当定义的各种bean被注册之后，会进入以下方法，把对应的bean塞入AuthManager里，方便后续统一管理和获取
 * 如果没有重写各种bean的实现，则AuthManager会用默认的实现
 * token-dao的redis注册，在{@link com.lxm.framework.auth.redis.AuthRedisDefaultPrepare}里
 **/
@Slf4j

@EnableConfigurationProperties(LxmTokenProperties.class)
public class LxmAuthSpringAutowired {

    @Autowired
    public void setTokenDao(LxmTokenDao dao) { AuthManager.setTokenDao(dao); }

    @Autowired
    public void setProperties(LxmTokenProperties properties) {
        log.debug("lxm-auth spring autowired,set properties success");
        AuthManager.setProperties(properties);
    }

    @Autowired(required = false)
    public void setListener(LxmTokenListener listener) {
        log.debug("lxm-auth spring autowired,set listener success");
        AuthManager.setListener(listener);
    }

    @Autowired(required = false)
    public void setPrincipleFilter(PrincipleFilter principleFilter){
        log.debug("lxm-auth spring autowired,set principle filter success");
        AuthManager.setPrincipleFilter(principleFilter);
    }

    @Autowired(required = false)
    public void setPathMatcher(PathMatcher pathMatcher) {
        LxmPathMatcherHolder.setPathMatcher(pathMatcher);
    }


}
