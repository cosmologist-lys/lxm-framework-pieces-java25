package com.lxm.framework.auth;

import com.lxm.framework.auth.model.*;
import com.lxm.framework.auth.model.defaults.*;
import com.lxm.framework.auth.filter.*;
import com.lxm.framework.auth.redis.AuthRedisProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix="lfp.auth", name="enabled", havingValue="true")
@EnableConfigurationProperties({LxmTokenProperties.class, AuthRedisProperties.class})
public class AuthAutoConfiguration {
    @Bean @ConditionalOnMissingBean(LxmTokenDao.class)
    public LxmTokenDao tokenDao(AuthRedisProperties properties) {
        return properties.prepared() ? new TokenDaoRedisDefault(properties) : new TokenDaoCacheDefault();
    }
    @Bean @ConditionalOnMissingBean(AuthAction.class)
    public AuthAction authAction() { return new AuthActionDefault(); }
    @Bean @ConditionalOnMissingBean(LxmAuthLogic.class)
    public LxmAuthLogic authLogic(AuthAction action) { return new AuthLogic(action); }
    @Bean @ConditionalOnMissingBean(Xmauth.class)
    public Xmauth xmauth(LxmAuthLogic logic) { return new Xmauth(logic); }
    @Bean @ConditionalOnMissingBean(LxmAuthSpringAutowired.class)
    public LxmAuthSpringAutowired authSpringAutowired() { return new LxmAuthSpringAutowired(); }
    @Bean @ConditionalOnMissingBean(com.lxm.framework.auth.aop.LxmAnnotationAspect.class)
    public com.lxm.framework.auth.aop.LxmAnnotationAspect authAspect() { return new com.lxm.framework.auth.aop.LxmAnnotationAspect(); }
    @Bean @ConditionalOnMissingBean(PrincipleFilter.class) @ConditionalOnBean({RoleFilter.class, PermissionFilter.class})
    public PrincipleFilter principleFilter(RoleFilter role, PermissionFilter permission) { return new PrincipleDefaultFilter(role, permission); }
    @Bean @ConditionalOnMissingBean(LxmServletFilter.class) @ConditionalOnBean(AuthFilterRegister.class)
    public LxmServletFilter authFilter(AuthFilterRegister registration) { return new LxmServletFilter(registration); }
}
