package com.lxm.framework.enigma.spi;

import jakarta.servlet.http.HttpServletRequest;

/** 每次请求验证凭证有效性；返回 null 表示未登录。不能仅相信 sid、用户名或租户 header。 */
@FunctionalInterface
public interface EnigmaIdentityResolver {
    EnigmaIdentity resolve(HttpServletRequest request);
}
