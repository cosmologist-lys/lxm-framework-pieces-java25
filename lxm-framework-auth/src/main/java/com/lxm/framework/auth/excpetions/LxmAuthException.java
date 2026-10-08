package com.lxm.framework.auth.excpetions;

import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.common.auth.errs.AbstractAuthException;

/**
 * @Author: Lys
 * @Date 2023/5/29
 * @Describe
 **/
public class LxmAuthException extends AbstractAuthException {

    private static final long serialVersionUID = 3125141211820147138L;

    public LxmAuthException(AuthPrompts prompts) {
        super(prompts);
    }
}
