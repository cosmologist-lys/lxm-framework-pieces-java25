package com.lxm.framework.auth.filter;

import com.lxm.framework.auth.aop.LxmCheckPermission;
import com.lxm.framework.auth.aop.LxmCheckRole;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/6/13
 * @Describe
 *      对于role和permission同时存在的情况下
 *      优先匹配role
 *      其次再匹配permission
 **/
public interface PrincipleFilter {

    /**
     * 根据登录信息获取permission-list
     * @return /
     */
    List<String> getPermissionList();

    /**
     * 根据role的信息获取对应的permission-list
     * 但是具体情况可能有所不同 所以mark为了default
     *
     * 大部分系统permission是和user关联的，而不和role直接关联
     *
     * @param role /
     * @return /
     */
    default List<String> getPermissionListByRole(String role) {
        return null;
    }

    /**
     *  根据登录信息获取role-list
     * @return /
     */
    List<String> getRoleList();

    /**
     *  当前用户是否存在对应permission，如果不存在，抛错
     * @param permissionAnnotation /
     */
    void checkByAnnotation(LxmCheckPermission permissionAnnotation);

    /**
     * 当前用户是否存在对应role，如果不存在，抛错
     * @param roleAnnotation /
     */
    void checkByAnnotation(LxmCheckRole roleAnnotation);
}
