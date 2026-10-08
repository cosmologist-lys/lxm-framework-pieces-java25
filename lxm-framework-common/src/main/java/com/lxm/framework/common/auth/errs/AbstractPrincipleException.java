package com.lxm.framework.common.auth.errs;

import com.lxm.framework.common.auth.enums.AuthErrorEnums;



/**
 * @Author: Lys
 * @Date 2023/1/10
 * @Describe
 **/
public class AbstractPrincipleException extends RuntimeException {

    private static final long serialVersionUID = -152354336421630701L;

    private final AuthErrorEnums authErrorEnums;

    public AbstractPrincipleException(String message, AuthErrorEnums authErrorEnums) {
        super(message);
        this.authErrorEnums = authErrorEnums;
    }

    public AbstractPrincipleException(AuthErrorEnums authErrorEnums) {
        super(authErrorEnums.getMessage());
        this.authErrorEnums = authErrorEnums;
    }

    public AuthErrorEnums why(){
        return this.authErrorEnums;
    }
}
