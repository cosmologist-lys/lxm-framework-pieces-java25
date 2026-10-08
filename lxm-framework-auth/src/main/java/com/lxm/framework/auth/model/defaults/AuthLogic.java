package com.lxm.framework.auth.model.defaults;

import com.lxm.framework.auth.consts.AuthConstants;
import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.auth.excpetions.LxmAuthException;
import com.lxm.framework.auth.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: Lys
 * @Date 2023/6/1
 * @Describe
 **/
@Slf4j
public class AuthLogic implements LxmAuthLogic {

    private final AuthAction authAction;

    @Autowired
    public AuthLogic(AuthAction authAction) {
        this.authAction = authAction;
    }

    /**
     * 检查登录状态
     */
    @Override
    public void checkSignIn() {
        if (!isSignIn()) {
            throw new LxmAuthException(AuthPrompts.NOT_SIGN_IN);
        }
    }

    @Override
    public boolean isSignIn() {
        // 先看请求会话里有没有
        final String token = authAction.getTokenFromRequest();
        if (StringUtils.isBlank(token)) {
            return false;
        }
        // 再看内存里有没有
        final LxmTokenDao dao = AuthManager.getTokenDao();
        final String loginId = dao.get(token);
        if (StringUtils.isBlank(loginId)) {
            return false;
        }
        final LxmSession session = dao.getSession(loginId);
        return session != null && session.getTokenBox(token) != null;
    }

    /**
     * 登录   几种情况
     * 1：是否允许多点登录
     * 1.1 允许：
     * 2.同一device下是否允许复用token:
     * 2.1 允许 复用token
     * 2.2 不允许 创建一个新token和新的token-box
     * 1.2 不允许
     * 3.同login-id是否已登录(是否存在session)
     * 3.1 已登录，删除session下的所有token-box，然后创建一个新token和新的token-box
     * 3.2 未登录，创建一个新token和新的token-box
     *
     * @param loginId
     * @param device
     * @return
     */
    @Override
    public String signIn(String loginId, String device) {
        authAction.checkIsForbid(loginId);
        final LxmTokenDao dao = AuthManager.getTokenDao();
        String token = authAction.createFullToken();
        var session = dao.getSession(loginId);
        boolean hasSession = true;
        if (null == session) {
            hasSession = false;
            session = authAction.createSession(loginId);
            AuthManager.getListener().createSession(loginId, token);
        }
        // 允许多点登录
        if (AuthManager.getProperties().getAllowConcurrentLogin()) {
            if (hasSession && AuthManager.getProperties().getIsShare()) {
                // 同一设备下复用token
                String oldToken = authAction.getTokenOfSameDevice(session, device);
                if (StringUtils.isNotBlank(oldToken)) {
                    token = oldToken;
                } else {
                    session.addTokenBox(new TokenBox(token, device));
                }
            } else {
                session.addTokenBox(new TokenBox(token, device));
            }
        } else {
            // 只能单点登录
            // 如果已登录
            if (hasSession) {
                // 删除旧token
                for (TokenBox tokenBox : session.getTokenList()) {
                    session.removeTokenBox(tokenBox.getTokenValue());
                    dao.delete(tokenBox.getTokenValue());
                }
            }
            // 新token覆盖
            session.addTokenBox(new TokenBox(token, device));
        }
        // 存储token
        dao.set(token, loginId, AuthManager.getProperties().getTimeout());
        // 存储session
        dao.setSession(session, AuthManager.getProperties().getTimeout());
        // 写入会话
        authAction.writeResponseCookie(token);
        AuthManager.getListener().signIn(loginId, token);
        return token;
    }

    @Override
    public void signOut() {
        String token = authAction.getTokenFromRequest();
        if (StringUtils.isBlank(token)) {
            // 如果本身就没登录，就啥也不做
            return;
        }
        if (AuthManager.getProperties().getIsReadCookie()) {
            // 登录了，且是read-cookie模式，就清除cookie
            AuthManager.getContext()
                    .getResponse()
                    .deleteCookie(AuthManager.getProperties().getTokenName());
        }
        final LxmTokenDao dao = AuthManager.getTokenDao();
        final String loginId = dao.get(token);
        if (StringUtils.isBlank(loginId)) {
            return;
        }
        // 删除token的键值对
        dao.delete(token);
        // 找到session
        final LxmSession session = dao.getSession(loginId);
        if (null != session) {
            String deivce = AuthConstants.DEFAULT_DEVICE;
            final TokenBox tokenBox = session.getTokenBox(token);
            if (null != tokenBox) {
                deivce = tokenBox.getDevice();
            }
            // 删除此session下的token记录
            session.removeTokenBox(token);
            AuthManager.getListener().signOut(loginId, deivce);
            if (session.isEmpty()) {
                // 如果已经没有登录信息了，清空此session
                dao.deleteSession(session);
                AuthManager.getListener().deleteSession(loginId);
            }
        }
        authAction.deleteResponseCookie();
    }

    @Override
    public String tourist() {
        String loginId =
                AuthConstants.TOURIST.concat(RandomStringUtils.secure().nextAlphabetic(18));
        final String token = authAction.createFullToken();
        var session = authAction.createSession(loginId);
        session.addTokenBox(new TokenBox(token, "guest"));
        final LxmTokenProperties properties = AuthManager.getProperties();
        final LxmTokenDao dao = AuthManager.getTokenDao();
        dao.set(token, loginId, properties.getOnceTimeout());
        dao.setSession(session, properties.getOnceTimeout());
        authAction.writeResponseCookie(token);
        AuthManager.getListener().tourist(loginId, token);
        return token;
    }

    @Override
    public void forbid(String loginId, long forbidTime) {
        final LxmTokenDao dao = AuthManager.getTokenDao();
        final LxmSession session = dao.getSession(loginId);
        if (null == session) {
            return;
        }
        // 清除此session下的所有token
        authAction.clearSessionTokenFromDao(session);
        // 清除session
        dao.deleteSession(session);
        dao.setForbid(loginId, forbidTime);
        AuthManager.getListener().forbid(loginId, forbidTime);
    }

    @Override
    public void forbidByToken(String token, long forbidTime) {
        final LxmTokenDao dao = AuthManager.getTokenDao();
        final String loginId = dao.get(token);
        if (StringUtils.isBlank(loginId)) {
            return;
        }
        forbid(loginId, forbidTime);
    }

    @Override
    public void permit(String loginId) {
        // 确认是否被禁用
        final boolean forbid = AuthManager.getTokenDao().isForbid(loginId);
        if (!forbid) {
            return;
        }
        // 放开禁用
        AuthManager.getTokenDao().permit(loginId);
        // 通知监听
        AuthManager.getListener().permit(loginId);
    }

    @Override
    public void kick(String loginId) {
        final LxmTokenDao dao = AuthManager.getTokenDao();
        final LxmSession session = dao.getSession(loginId);
        if (null == session) {
            return;
        }
        // 删除所有token记录
        for (TokenBox tokenBox : session.getTokenList()) {
            dao.delete(tokenBox.getTokenValue());
        }
        // 删除session
        dao.deleteSession(session);
        AuthManager.getListener().kick(loginId);
    }

    @Override
    public void kickByToken(String token) {
        final LxmTokenDao dao = AuthManager.getTokenDao();
        final String loginId = dao.get(token);
        if (StringUtils.isBlank(loginId)) {
            return;
        }
        kick(loginId);
    }

    @Override
    public String getLoginId() {
        final String token = authAction.getTokenFromRequest();
        if (StringUtils.isBlank(token)) {
            return null;
        }
        final String loginId = AuthManager.getTokenDao().get(token);
        if (StringUtils.isBlank(loginId)) return null;
        final LxmSession session = AuthManager.getTokenDao().getSession(loginId);
        if (session == null || session.getTokenBox(token) == null) return null;
        if (AuthManager.getProperties().getAutoRenew()) {
            AuthManager.getTokenDao()
                    .updateTimeout(token, AuthManager.getProperties().getTimeout());
        }
        return loginId;
    }
}
