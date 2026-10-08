package com.lxm.framework.auth.filter;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/6/14
 * @Describe
 **/
public class RoleDefaultBlankFilter implements RoleFilter {

    @Override
    public List<String> getRoleList(String loginId) {
        return null;
    }

    @Override
    public boolean hasRole(String loginId, String role) {
        return false;
    }
}
