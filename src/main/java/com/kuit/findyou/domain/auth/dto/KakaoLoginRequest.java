package com.kuit.findyou.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 DTO")
public record KakaoLoginRequest(
        @Schema(description = "사용자 카카오 ID", example = "12345678")
        Long kakaoId
) {
}
