package com.lxm.framework.auth.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: Lys
 * @Date 2023/6/8
 * @Describe
 **/
@Slf4j

public class Xmauth {

    private static LxmAuthLogic authLogic;

    @Autowired
    public Xmauth(LxmAuthLogic authLogic) {
        Xmauth.authLogic = authLogic;
    }

    public static void checkSignIn() {
        authLogic.checkSignIn();
    }

    public static boolean isSignIn() {
        return authLogic.isSignIn();
    }

    public static String signIn(String loginId, String device) {
        return authLogic.signIn(loginId, device);
    }

    public static void signOut() {
        authLogic.signOut();
    }

    public static void forbid(String loginId, long forbidTime) {
        authLogic.forbid(loginId, forbidTime);
    }

    public static void forbidByToken(String token, long forbidTime) {
        authLogic.forbidByToken(token, forbidTime);
    }

    public static void permit(String loginId) {
        authLogic.permit(loginId);
    }

    public static void kick(String loginId) {
        authLogic.kick(loginId);
    }

    public static void kickByToken(String token) {
        authLogic.kickByToken(token);
    }

    public static String getLoginId() {
        return authLogic.getLoginId();
    }
}
