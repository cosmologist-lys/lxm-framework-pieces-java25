package com.lxm.framework.auth.model;

import com.lxm.framework.auth.consts.AuthConstants;

/**
 * @Author: Lys
 * @Date 2023/6/1
 * @Describe
 **/

public interface LxmAuthLogic {

    /**
     * 默认方法的登录
     * @param loginId  /
     * @return  /
     */
    default String signIn(String loginId) {
        return signIn(loginId, AuthConstants.DEFAULT_DEVICE);
    }

    /**
     *  检查登录状态，未登录就抛错
     */
    void checkSignIn();

    /**
     * 返回登录状态，不抛错
     * @return  /
     */
    boolean isSignIn();

    /**
     * 登录
     * @param loginId  /
     * @param device  /
     * @return   token
     */
    String signIn(String loginId, String device);

    /**
     * 登出
     */
    void signOut();

    /**
     * 禁用
     * @param loginId  /
     * @param forbidTime  /
     */
    void forbid(String loginId,long forbidTime);

    /**
     * 通过token禁用
     * @param token  /
     * @param forbidTime  /
     */
    void forbidByToken(String token,long forbidTime);

    /**
     * 启用forbid的账号
     * @param loginId  /
     */
    void permit(String loginId);

    /**
     * 踢人，强制下线
     * @param loginId  /
     */
    void kick(String loginId);

    /**
     * 通过token踢人
     * @param token  /
     */
    void kickByToken(String token);

    /**
     * 以游客身份登录
     * @return
     */
    String tourist();

    /**
     * 获取当前会话的loginId
     * @return login-id
     */
    String getLoginId();

}
