package com.kuit.findyou.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게스트 로그인 응답 DTO")
public record GuestLoginResponse (
        @Schema(description = "유저 식별자")
        Long userId,
        @Schema(description = "엑세스 토큰")
        String accessToken
){
}
