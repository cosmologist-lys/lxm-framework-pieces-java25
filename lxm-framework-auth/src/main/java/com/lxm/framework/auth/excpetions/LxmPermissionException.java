package com.lxm.framework.auth.excpetions;

import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.common.auth.errs.AbstractAuthException;

/**
 * @Author: Lys
 * @Date 2023/6/14
 * @Describe
 **/
public class LxmPermissionException extends AbstractAuthException {

    private static final long serialVersionUID = -8893226501417615083L;

    public LxmPermissionException(AuthPrompts error) {
        super(error);
    }
}
