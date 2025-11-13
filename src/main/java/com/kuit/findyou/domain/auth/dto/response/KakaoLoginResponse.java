package com.kuit.findyou.domain.auth.dto.response;

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
        return new KakaoLoginResponse(userInfo, false);
    }

    public static KakaoLoginResponse notFound() {
        return new KakaoLoginResponse(null, true);
    }

    public record UserInfoDto(
            @Schema(description = "사용자 식별자", example = "1")
            Long userId,
            @Schema(description = "사용자 닉네임", example = "유저1")
            String nickname,
            @Schema(description = "찾아유 엑세스 토큰", example = "token1234token1234token1234")
            String accessToken
    ) {
    }
}
