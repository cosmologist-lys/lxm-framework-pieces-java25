package com.lxm.framework.auth.filter;

import com.lxm.framework.auth.aop.LxmMode;
import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.auth.excpetions.LxmPermissionException;
import com.lxm.framework.auth.excpetions.LxmRoleException;
import com.lxm.framework.auth.model.Xmauth;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/6/13
 * @Describe 需要使用者实现
 **/
public interface PermissionFilter {

    /**
     * get permission list
     * @param loginId   /
     * @return  /
     */
    List<String> getPermissionList(String loginId);

    /**
     * has permission or not
     * @param loginId   /
     * @param permission    /
     * @return  true or false
     */
    boolean hasPermission(String loginId, String permission);

    default List<String> getPermissionList(){
        return getPermissionList(getLoginId());
    }

    default boolean hasPermission(String permission) {
        return hasPermission(getLoginId(), permission);
    }

    default String getLoginId() {
        return Xmauth.getLoginId();
    }

    default void checkPermission(String permission) {
        if (hasPermission(getLoginId(), permission)) {
            return;
        }
        throw new LxmPermissionException(AuthPrompts.UNQUALIFIED_PERMISSION);
    }


    /**
     * 根据mode类型来判断是否包含permission
     * 如果不包含，记得要抛错
     *
     * @param mode       or , and
     * @param permission /
     */
    default void checkPermissionArray(LxmMode mode, String... permission) throws LxmPermissionException {
        boolean safe = false;
        if (LxmMode.AND.equals(mode)) {
            // and模式下，要全部都匹配
            safe = hasPermissionAnd(permission);
        } else {
            // or模式下，匹配上一个即可
            safe = hasPermissionOr(permission);
        }
        if (safe) {
            return;
        }
        throw new LxmRoleException(AuthPrompts.UNQUALIFIED_PERMISSION);
    }

    /**
     * 如果满足1个，就返回true
     *
     * @param permissions /
     * @return /
     */
    default boolean hasPermissionOr(String... permissions) {
        if (null == permissions || 0 >= permissions.length) {
            return true;
        }
        boolean check = false;
        for (String permission : permissions) {
            String loginId = getLoginId();
            if (hasPermission(loginId, permission)) {
                check = true;
                break;
            }
        }
        return check;
    }

    /**
     * 如果有1个不满足，就返回false
     *
     * @param permissions /
     * @return /
     */
    default boolean hasPermissionAnd(String... permissions) {
        if (null == permissions || 0 >= permissions.length) {
            return true;
        }
        boolean check = true;
        for (String permission : permissions) {
            String loginId = getLoginId();
            if (!hasPermission(loginId, permission)) {
                check = false;
                break;
            }
        }
        return check;
    }
}
