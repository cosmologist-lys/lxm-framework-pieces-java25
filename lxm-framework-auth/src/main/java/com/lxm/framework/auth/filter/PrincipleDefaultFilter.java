package com.lxm.framework.auth.filter;

import com.lxm.framework.auth.aop.LxmCheckPermission;
import com.lxm.framework.auth.aop.LxmCheckRole;
import com.lxm.framework.auth.aop.LxmMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/6/14
 * @Describe
 **/
@Slf4j

@ConditionalOnBean({RoleFilter.class, PermissionFilter.class})
public class PrincipleDefaultFilter implements PrincipleFilter {

    private RoleFilter roleFilter;
    private PermissionFilter permissionFilter;

    @Autowired(required = false)
    public PrincipleDefaultFilter(RoleFilter roleFilter, PermissionFilter permissionFilter) {
        if (null == roleFilter || null == permissionFilter) {
            return;
        }
        this.roleFilter = roleFilter;
        this.permissionFilter = permissionFilter;
    }

    @Override
    public List<String> getPermissionList() {
        return permissionFilter.getPermissionList();
    }

    @Override
    public List<String> getPermissionListByRole(String role) {
        return null;
    }

    @Override
    public List<String> getRoleList() {
        return roleFilter.getRoleList();
    }

    @Override
    public void checkByAnnotation(LxmCheckPermission permissionAnnotation) {
        String[] permissionArray = permissionAnnotation.value();
        if (0 == permissionArray.length) {
            return;
        }
        final LxmMode mode = permissionAnnotation.mode();
        permissionFilter.checkPermissionArray(mode, permissionArray);
    }

    @Override
    public void checkByAnnotation(LxmCheckRole roleAnnotation) {
        String[] roleArray = roleAnnotation.value();
        if (0 == roleArray.length) {
            return;
        }
        final LxmMode mode = roleAnnotation.mode();
        roleFilter.checkRoleArray(mode, roleArray);
    }
}
