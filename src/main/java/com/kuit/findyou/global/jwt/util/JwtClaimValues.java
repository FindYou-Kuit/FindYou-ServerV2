package com.kuit.findyou.global.jwt.util;

public enum JwtClaimValues {
    ACCESS_TOKEN("accessToken"),
    REFRESH_TOKEN("refreshToken");

    private String value;

    private JwtClaimValues(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
