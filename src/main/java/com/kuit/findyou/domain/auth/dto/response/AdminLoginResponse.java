package com.kuit.findyou.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 로그인 응답 DTO")
public record AdminLoginResponse(
        @Schema(description = "관리자 유저 식별자")
        Long userId,
        @Schema(description = "엑세스 토큰")
        String accessToken
) {
}
