package com.lxm.framework.auth.model.defaults;

import com.lxm.framework.auth.consts.AuthConstants;
import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.auth.excpetions.LxmAuthException;
import com.lxm.framework.auth.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @Author: Lys
 * @Date 2023/6/9
 * @Describe
 **/
@Slf4j
public class AuthActionDefault implements AuthAction {

    @Override
    public String createFullToken() {
        final LxmTokenProperties properties = AuthManager.getProperties();
        final String tokenPrefix = properties.getTokenPrefix();
        final String token = createTokenValue();
        String finalTokenValue =
                (StringUtils.isNotBlank(tokenPrefix) ? tokenPrefix.concat(token) : token);
        return finalTokenValue;
    }

    @Override
    public String createTokenValue() {
        final String tokenStyle = AuthManager.getProperties().getTokenStyle();
        if (tokenStyle.equals(AuthConstants.TOKEN_STYLE_UUID)) {
            return UUID.randomUUID().toString();
        }
        if (tokenStyle.equals(AuthConstants.TOKEN_STYLE_SIMPLE_UUID)) {
            return UUID.randomUUID().toString().replaceAll("-", "");
        }
        if (tokenStyle.equals(AuthConstants.TOKEN_STYLE_TIK)) {
            return RandomStringUtils.secure()
                    .nextAlphanumeric(2)
                    .concat("_")
                    .concat(RandomStringUtils.secure().nextAlphabetic(14))
                    .concat("_")
                    .concat(RandomStringUtils.secure().nextAlphabetic(16));
        }
        if (tokenStyle.equals(AuthConstants.TOKEN_STYLE_RANDOM_32)) {
            return RandomStringUtils.secure().nextAlphanumeric(32);
        }
        if (tokenStyle.equals(AuthConstants.TOKEN_STYLE_RANDOM_64)) {
            return RandomStringUtils.secure().nextAlphanumeric(64);
        }
        return RandomStringUtils.secure().nextAlphanumeric(128);
    }

    @Override
    public LxmSession createSession(String loginId) {
        return new LxmSession(loginId);
    }

    @Override
    public String getTokenOfSameDevice(LxmSession session, String device) {
        final List<TokenBox> tokenSignList = session.getTokenList();
        for (TokenBox tokenBox : tokenSignList) {
            if (tokenBox.getDevice().equals(device)) {
                return tokenBox.getTokenValue();
            }
        }
        return null;
    }

    @Override
    public void checkIsForbid(String loginId) {
        if (AuthManager.getTokenDao().isForbid(loginId)) {
            throw new LxmAuthException(AuthPrompts.IS_FORBID);
        }
    }

    @Override
    public void writeResponseCookie(String token) {
        final LxmTokenProperties properties = AuthManager.getProperties();
        final LxmResponse response = AuthManager.getContext().getResponse();
        response.addCookie(
                properties.getTokenName(),
                token,
                "/",
                properties.getCookieDomain(),
                (int) properties.getTimeout());
    }

    @Override
    public void deleteResponseCookie() {
        final LxmTokenProperties properties = AuthManager.getProperties();
        final LxmResponse response = AuthManager.getContext().getResponse();
        response.deleteCookie(properties.getTokenName());
    }

    @Override
    public String getTokenFromRequest() {
        final LxmRequest request = AuthManager.getContext().getRequest();
        String tokenName = AuthManager.getProperties().getTokenName();
        String token = null;

        if (AuthManager.getProperties().getIsReadCookie()) {
            // if read token from token
            token = request.getCookieValue(tokenName);
        }
        if (StringUtils.isBlank(token) && AuthManager.getProperties().getIsReadHead()) {
            // if read token from header
            token = request.getHeader(tokenName);
        }
        if (StringUtils.isBlank(token) && AuthManager.getProperties().getIsReadParameter()) {
            // if read token from request request-parameter
            token = request.getParameter(tokenName);
        }
        return token;
    }

    @Override
    public void clearSessionTokenFromDao(LxmSession session) {
        final LxmTokenDao dao = AuthManager.getTokenDao();
        for (TokenBox tokenBox : session.getTokenList()) {
            dao.delete(tokenBox.getTokenValue());
        }
    }
}
