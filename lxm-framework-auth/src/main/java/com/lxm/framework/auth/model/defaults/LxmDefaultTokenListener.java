package com.lxm.framework.auth.model.defaults;

import com.lxm.framework.auth.model.LxmTokenListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Lys
 * @Date 2023/5/31
 * @Describe
 **/
@Slf4j
public class LxmDefaultTokenListener implements LxmTokenListener {

    @Override
    public void signIn(String loginId, String token) {}

    @Override
    public void createSession(String loginId, String token) {}

    @Override
    public void signOut(String loginId, String device) {
        log.debug(
                "lxm-auth action <sign-out> caught by listener , loginId : {} , device : {}",
                loginId,
                device);
    }

    @Override
    public void tourist(String loginId, String token) {}

    @Override
    public void deleteSession(String loginId) {
        log.debug("lxm-auth action <delete-session> caught by listener , loginId : {} ", loginId);
    }

    @Override
    public void forbid(String loginId, long forbidTime) {
        log.debug(
                "lxm-auth action <forbid> caught by listener , loginId : {} , forbidTime : {}",
                loginId,
                forbidTime);
    }

    @Override
    public void kick(String loginId) {
        log.debug("lxm-auth action <kick> caught by listener , loginId : {}", loginId);
    }

    @Override
    public void permit(String loginId) {
        log.debug("lxm-auth action <permit> caught by listener , loginId : {}", loginId);
    }
}
