package com.lxm.framework.auth.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Lys
 * @Date 2023/6/2
 * @Describe
 **/
@Data
public class TokenBox implements Serializable {
    private static final long serialVersionUID = -4162385306959402987L;

    private String tokenValue;
    private String device;

    public TokenBox() {}

    public TokenBox(String tokenValue, String device) {
        this.tokenValue = tokenValue;
        this.device = device;
    }
}
