package com.lxm.framework.auth.model;

/**
 * @Author: Lys
 * @Date 2023/5/30
 * @Describe
 **/
public interface LxmTokenListener {

    /**
     * on sign-in
     *
     * @param loginId
     * @param token
     */
    void signIn(String loginId, String token);

    /**
     * on create-session
     *
     * @param loginId   /
     * @param token  /
     */
    void createSession(String loginId, String token);

    /**
     * sign-out
     * @param loginId
     * @param device
     */
    void signOut(String loginId, String device);

    /**
     * 以游客身份登录
     * @param loginId /
     * @param token   /
     */
    void tourist(String loginId, String token);

    /**
     * delete session
     * @param loginId
     */
    void deleteSession(String loginId);

    /**
     * forbid a login-id
     * @param loginId
     * @param forbidTime
     */
    void forbid(String loginId, long forbidTime);

    /**
     * kick a login-id
     * @param loginId
     */
    void kick(String loginId);

    /**
     * recover a login-id from forbid to normal
     * @param loginId
     */
    void permit(String loginId);
}
