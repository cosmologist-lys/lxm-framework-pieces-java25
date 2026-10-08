package com.lxm.framework.common.principle;

import java.io.Serializable;

/**
 * @Author: Lys
 * @Date 2023/1/9
 * @Describe
 **/
public class StandardPrinciple implements Serializable {

    private static final long serialVersionUID = 9033442920096366029L;

    private String loginId;

    private String tokenName;

    private String tokenValue;

    private String userName;

    private String accountName;

    private int userId;

    private int accountId;

    private int tenantId;

    public StandardPrinciple() {
        this.defaultValue();
    }


    public StandardPrinciple(int accountId, String accountName) {
        this.accountName = accountName;
        this.accountId = accountId;
        this.defaultValue();
    }

    public StandardPrinciple(int accountId, String accountName, int userId, String userName, int tenantId) {
        this.userName = userName;
        this.accountName = accountName;
        this.userId = userId;
        this.accountId = accountId;
        this.tenantId = tenantId;
    }

    private void defaultValue() {
        this.tenantId = -1;
        this.userId = -1;
        this.userName = null;
    }


    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

}
