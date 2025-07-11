package com.kuit.findyou.domain.auth.dto;

public record KakaoLoginResponse(
        UserInfoDto userInfo,
        Boolean isFirstLogin
) {
}
