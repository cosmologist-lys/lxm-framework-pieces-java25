package com.lxm.framework.auth.model.defaults;

import tools.jackson.databind.ObjectMapper;
import com.lxm.framework.auth.consts.AuthConstants;
import com.lxm.framework.auth.model.LxmSession;
import com.lxm.framework.auth.model.LxmTokenDao;
import com.lxm.framework.auth.redis.AuthRedisProperties;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;

import java.time.Duration;

/**
 * @Author: Lys
 * @Date 2023/6/5
 * @Describe
 **/
@Slf4j
public class TokenDaoRedisDefault implements LxmTokenDao, AutoCloseable {

    private final UnifiedJedis jedis;
    private final ObjectMapper om;

    public TokenDaoRedisDefault(AuthRedisProperties properties) {
        this.om = tools.jackson.databind.json.JsonMapper.builder().build();
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(600);
        poolConfig.setMaxIdle(0);
        poolConfig.setMinIdle(0);
        poolConfig.setMaxWait(Duration.ofSeconds(5));
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        DefaultJedisClientConfig.Builder clientConfigBuilder = DefaultJedisClientConfig.builder();
        clientConfigBuilder.password(properties.getPassword());
        clientConfigBuilder.database(properties.getDatabase());
        DefaultJedisClientConfig clientConfig = clientConfigBuilder.build();
        HostAndPort hostAndPort = new HostAndPort(properties.getHost(), properties.getPort());
        this.jedis =
                RedisClient.builder()
                        .hostAndPort(hostAndPort)
                        .clientConfig(clientConfig)
                        .poolConfig(poolConfig)
                        .build();
    }

    @Override
    public String get(@NonNull String key) {
        return jedis.get(key);
    }

    @Override
    public void set(@NonNull String key, @NonNull String value, long timeout) {
        jedis.set(key, value, timeout > 0 ? new SetParams().ex(timeout) : new SetParams());
    }

    @Override
    public void update(@NonNull String key, @NonNull String value, long timeout) {
        jedis.set(
                key, value, timeout > 0 ? new SetParams().xx().ex(timeout) : new SetParams().xx());
    }

    @Override
    public void updateTimeout(@NonNull String key, long timeout) {
        if (timeout > 0) jedis.expire(key, timeout);
        else jedis.persist(key);
    }

    @Override
    public void delete(@NonNull String key) {
        jedis.del(key);
    }

    @Override
    public long getTimeout(@NonNull String key) {
        final long ttl = jedis.ttl(key);
        return ttl == -1 ? 0 : (ttl == -2 ? -1 : ttl);
    }

    @Override
    public LxmSession getSession(@NonNull String key) {
        final String sessionString = jedis.get(key);
        if (StringUtils.isNotBlank(sessionString)) {
            try {
                return om.readValue(sessionString, LxmSession.class);
            } catch (Exception e) {
                throw new IllegalStateException("Invalid stored auth session", e);
            }
        }
        return null;
    }

    @Override
    public void setSession(@NonNull LxmSession session, long timeout) {
        String loginId = session.getLoginId();
        try {
            String sessionString = om.writeValueAsString(session);
            jedis.set(
                    loginId,
                    sessionString,
                    timeout > 0 ? SetParams.setParams().ex(timeout) : SetParams.setParams());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot persist auth session", e);
        }
    }

    @Override
    public void updateSession(@NonNull LxmSession session, long timeout) {
        jedis.set(
                session.getLoginId(),
                om.writeValueAsString(session),
                timeout > 0 ? SetParams.setParams().xx().ex(timeout) : SetParams.setParams().xx());
    }

    @Override
    public void deleteSession(@NonNull LxmSession session) {
        jedis.del(session.getLoginId());
    }

    @Override
    public void setForbid(String loginId, long timeout) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        jedis.set(
                key,
                loginId,
                timeout > 0 ? SetParams.setParams().ex(timeout) : SetParams.setParams());
    }

    @Override
    public boolean isForbid(String loginId) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        return StringUtils.isNotBlank(jedis.get(key));
    }

    @Override
    public void permit(String loginId) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        jedis.del(key);
    }

    /*@Override
    public String get(@NonNull String key) {
        return tokenRedis.opsForValue().get(key);
    }

    @Override
    public void set(@NonNull String key, @NonNull String value, long timeout) {
        tokenRedis.opsForValue().set(key, value, timeout, SEC_UNIT);
    }

    @Override
    public void update(@NonNull String key, @NonNull String value, long timeout) {
        set(key, value, timeout);
    }

    @Override
    public void updateTimeout(@NonNull String key, long timeout) {
        String value = get(key);
        if (StringUtils.isNotBlank(value)) {
            set(key, value, timeout);
        }
    }

    @Override
    public void delete(@NonNull String key) {
        tokenRedis.delete(key);
    }

    @Override
    public long getTimeout(@NonNull String key) {
        final Long expire = tokenRedis.getExpire(key);
        return null != expire ? expire : 0L;
    }

    @Override
    public LxmSession getSession(@NonNull String key) {
        final Object o = sessionRedis.opsForValue().get(key);
        return null != o ? (LxmSession) o : null;
    }

    @Override
    public void setSession(@NonNull LxmSession session, long timeout) {
        sessionRedis.opsForValue().set(session.getLoginId(), session, timeout, SEC_UNIT);
    }

    @Override
    public void updateSession(@NonNull LxmSession session, long timeout) {
        final LxmSession se = getSession(session.getLoginId());
        if (null != se) {
            setSession(se, timeout);
        }
    }

    @Override
    public void deleteSession(@NonNull LxmSession session) {
        sessionRedis.delete(session.getLoginId());
    }

    @Override
    public void setForbid(String loginId, long timeout) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        tokenRedis.opsForValue().set(key, loginId, timeout, SEC_UNIT);
    }

    @Override
    public boolean isForbid(String loginId) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        return null != tokenRedis.opsForValue().get(key);
    }

    @Override
    public void permit(String loginId) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        tokenRedis.delete(key);
    }*/
    @Override
    public void close() {
        jedis.close();
    }
}
