package com.lxm.framework.auth.model;

/**
 * @Author: Lys
 * @Date 2023/6/9
 * @Describe
 **/
public interface AuthAction {

    /**
     * 创建token，在createTokenValue基础之上，根据properties配置的前缀信息生成完整token
     * @return
     */
    String createFullToken();

    /**
     * 创建token，根据properties里的配置规则生成对应token
     *
     * @return
     */
    String createTokenValue();

    LxmSession createSession(String loginId);

    /**
     * 如果 可以复用token(lxm-properties.is-share = true)，则寻找此device下的token直接返回
     *
     * @param session
     * @param device
     * @return
     */
    String getTokenOfSameDevice(LxmSession session, String device);

    /**
     * 检查该账号是否被封禁
     *
     * @param loginId
     */
    void checkIsForbid(String loginId);

    /**
     * 写到cookie里
     *
     * @param token
     */
    void writeResponseCookie(String token);

    /**
     * 登出时，清除cookie
     */
    void deleteResponseCookie();

    /**
     * 从请求里获取token
     * 优先从cookie获取
     * 其次header
     * 最后从请求体
     *
     * @return
     */
    String getTokenFromRequest();

    /**
     * 从dao里清除一个session里所有token值
     * @param session
     */
    void clearSessionTokenFromDao(LxmSession session);
}
