package com.lxm.framework.auth.filter;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/6/14
 * @Describe
 **/
public class PermissionDefaultBlankFilter implements PermissionFilter {

    @Override
    public List<String> getPermissionList(String loginId) {
        return null;
    }

    @Override
    public boolean hasPermission(String loginId, String permission) {
        return false;
    }
}
