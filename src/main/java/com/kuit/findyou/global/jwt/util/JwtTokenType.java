package com.kuit.findyou.global.jwt.util;

public enum JwtTokenType {
    ACCESS_TOKEN("accessToken"),
    REFRESH_TOKEN("refreshToken");

    private String value;

    private JwtTokenType(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
