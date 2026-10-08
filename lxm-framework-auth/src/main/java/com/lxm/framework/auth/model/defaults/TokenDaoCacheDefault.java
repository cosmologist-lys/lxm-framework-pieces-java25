package com.lxm.framework.auth.model.defaults;

import com.lxm.framework.auth.consts.AuthConstants;
import com.lxm.framework.auth.model.LxmSession;
import com.lxm.framework.auth.model.LxmTokenDao;
import com.lxm.framework.common.cache.customized.impl.CacheBuilder;
import com.lxm.framework.common.cache.customized.impl.TimeCache;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Lys
 * @Date 2023/6/2
 * @Describe
 **/
public class TokenDaoCacheDefault implements LxmTokenDao, AutoCloseable {

    private final TimeCache<String, String> tokenCache;
    private final TimeCache<String, LxmSession> sessionCache;

    public TokenDaoCacheDefault() {
        tokenCache = CacheBuilder.newTimeCache(256, 30, TimeUnit.SECONDS);
        sessionCache = CacheBuilder.newTimeCache(256, 30, TimeUnit.SECONDS);
    }

    @Override
    public String get(@NonNull String key) {
        return tokenCache.get(key);
    }

    @Override
    public synchronized void set(@NonNull String key, @NonNull String value, long timeout) {
        tokenCache.put(key, value, timeout, SEC_UNIT);
    }

    @Override
    public synchronized void update(@NonNull String key, @NonNull String value, long timeout) {
        if (StringUtils.isNotBlank(get(key))) {
            tokenCache.put(key, value, timeout, SEC_UNIT);
        }
    }

    @Override
    public synchronized void updateTimeout(@NonNull String key, long timeout) {
        final String value = get(key);
        if (StringUtils.isNotBlank(value)) {
            update(key, value, timeout);
        }
    }

    @Override
    public synchronized void delete(@NonNull String key) {
        tokenCache.remove(key);
    }

    @Override
    public long getTimeout(@NonNull String key) {
        long ttl = tokenCache.getTimeout(key);
        return ttl <= 0 ? ttl : Math.max(1, TimeUnit.MILLISECONDS.toSeconds(ttl));
    }

    @Override
    public LxmSession getSession(@NonNull String key) {
        return sessionCache.get(key);
    }

    @Override
    public synchronized void setSession(@NonNull LxmSession session, long timeout) {
        sessionCache.put(session.getLoginId(), session, timeout, SEC_UNIT);
    }

    @Override
    public synchronized void updateSession(@NonNull LxmSession session, long timeout) {
        final LxmSession se = getSession(session.getLoginId());
        if (null != se) {
            sessionCache.put(se.getLoginId(), session, timeout, SEC_UNIT);
        }
    }

    @Override
    public synchronized void deleteSession(@NonNull LxmSession session) {
        sessionCache.remove(session.getLoginId());
    }

    @Override
    public void setForbid(String loginId, long timeout) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        tokenCache.put(key,loginId,timeout,SEC_UNIT);
    }

    @Override
    public boolean isForbid(String loginId) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        return tokenCache.exist(key);
    }

    @Override
    public void permit(String loginId) {
        String key = AuthConstants.TOKEN_FORBID_TAG.concat(loginId);
        tokenCache.remove(key);
    }
    @Override
    public void close() { tokenCache.close(); sessionCache.close(); }
}
