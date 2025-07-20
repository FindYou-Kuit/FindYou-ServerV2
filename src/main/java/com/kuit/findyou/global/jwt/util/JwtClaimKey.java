package com.kuit.findyou.global.jwt.util;

public enum JwtClaimKey {
    USER_ID("userId"),
    ROLE("role"),
    TOKEN_TYPE("tokenType");
    private String key;

    private JwtClaimKey(String key){
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }
}
