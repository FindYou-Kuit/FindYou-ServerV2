package com.kuit.findyou.domain.auth.dto;

import com.kuit.findyou.domain.user.model.User;

public record KakaoLoginResponse(
        UserInfoDto userInfo,
        Boolean isFirstLogin
) {
    public static KakaoLoginResponse fromUserAndAccessToken(User user, String accessToken) {
        UserInfoDto userInfo = new UserInfoDto(user.getId(), user.getName(), accessToken);
        return new KakaoLoginResponse(userInfo, true);
    }

    public static KakaoLoginResponse notFound() {
        return new KakaoLoginResponse(null, true);
    }
}
