package com.lxm.framework.auth.filter;

import com.lxm.framework.auth.aop.LxmMode;
import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.auth.excpetions.LxmRoleException;
import com.lxm.framework.auth.model.Xmauth;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/6/13
 * @Describe 需要使用者实现
 **/
public interface RoleFilter {

    /**
     * get role list
     * @param loginId /
     * @return  /
     */
    List<String> getRoleList(String loginId);

    /**
     * has role or not
     * @param loginId   /
     * @param role  /
     * @return  true or false
     */
    boolean hasRole(String loginId, String role);

    default List<String> getRoleList() {
        return getRoleList(getLoginId());
    }

    default boolean hasRole(String role) {
        return hasRole(getLoginId(), role);
    }

    default String getLoginId() {
        return Xmauth.getLoginId();
    }

    default void checkRole(String role) {
        if (hasRole(getLoginId(), role)) {
            return;
        }
        throw new LxmRoleException(AuthPrompts.UNQUALIFIED_ROLE);
    }

    /**
     * 根据mode来判断是否包含role
     * 如果不包含，记得要抛错
     *
     * @param mode /
     * @param roles /
     */
    default void checkRoleArray(LxmMode mode, String... roles) throws LxmRoleException {
        boolean safe = false;
        if (LxmMode.AND.equals(mode)) {
            // and模式下，要全部都匹配
            safe = hasRoleAnd(roles);
        } else {
            // or模式下，匹配上一个即可
            safe = hasRoleOr(roles);
        }
        if (safe) {
            return;
        }
        throw new LxmRoleException(AuthPrompts.UNQUALIFIED_ROLE);
    }

    /**
     * 有一个即可
     * @param roles
     * @return
     */
    default boolean hasRoleOr(String... roles) {
        if (null == roles || 0 >= roles.length) {
            return true;
        }
        boolean check = false;
        for (String role : roles) {
            String loginId = getLoginId();
            if (hasRole(loginId, role)) {
                check = true;
                break;
            }
        }
        return check;
    }

    /**
     * 全部都得有
     * @param roles
     * @return
     */
    default boolean hasRoleAnd(String... roles) {
        if (null == roles || 0 >= roles.length) {
            return true;
        }
        boolean check = true;
        for (String role : roles) {
            String loginId = getLoginId();
            if (!hasRole(loginId, role)) {
                check = false;
                break;
            }
        }
        return check;
    }
}
