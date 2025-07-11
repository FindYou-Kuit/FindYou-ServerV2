package com.kuit.findyou.domain.auth.dto;

import com.kuit.findyou.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 로그인 응답 DTO")
public record KakaoLoginResponse(
        @Schema(description = "사용자 정보")
        UserInfoDto userInfo,
        @Schema(description = "첫 로그인 여부 = 회원가입 여부", example = "false")
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
