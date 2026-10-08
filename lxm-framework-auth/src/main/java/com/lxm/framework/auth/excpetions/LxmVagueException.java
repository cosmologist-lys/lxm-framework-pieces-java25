package com.lxm.framework.auth.excpetions;

import com.lxm.framework.auth.enums.AuthPrompts;
import com.lxm.framework.common.auth.errs.AbstractAuthException;

import java.io.Serial;

/**
 * @Author: Lys
 * @Date 2023/6/14
 * @Describe
 **/
public class LxmVagueException extends AbstractAuthException {
    @Serial private static final long serialVersionUID = 4234414520559354019L;

    public LxmVagueException(AuthPrompts error) {
        super(error);
    }
}
