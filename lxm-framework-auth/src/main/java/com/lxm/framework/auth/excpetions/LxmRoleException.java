package com.lxm.framework.auth.excpetions;

import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.common.auth.errs.AbstractAuthException;

/**
 * @Author: Lys
 * @Date 2023/6/14
 * @Describe
 **/
public class LxmRoleException extends AbstractAuthException {

    private static final long serialVersionUID = 4682584298737724733L;

    public LxmRoleException(AuthPrompts error) {
        super(error);
    }
}
