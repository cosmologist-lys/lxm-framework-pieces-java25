package com.lxm.framework.enigma.spi;

import java.util.Map;

/** 这些值必须来自应用已认证的登录上下文。csrfToken 只用于 Cookie 身份的密钥端点。 */
public record EnigmaIdentity(
        String userId,
        String tenantId,
        String loginSession,
        boolean cookieAuthentication,
        String csrfToken) {
    public EnigmaIdentity {
        if (userId == null
                || userId.isBlank()
                || tenantId == null
                || loginSession == null
                || loginSession.isBlank())
            throw new IllegalArgumentException("Authenticated identity required");
    }

    @Override
    public String toString() {
        return "EnigmaIdentity[userId=" + userId + ",tenantId=" + tenantId + "]";
    }

    public Map<String, String> binding() {
        return Map.of("userId", userId, "tenantId", tenantId, "loginSession", loginSession);
    }
}
