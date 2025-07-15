package com.kuit.findyou.global.jwt.util;

public enum JwtClaimKeys {
    USER_ID("userId"),
    ROLE("role"),
    TOKEN_TYPE("tokenType");
    private String key;

    private JwtClaimKeys(String key){
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }
}
