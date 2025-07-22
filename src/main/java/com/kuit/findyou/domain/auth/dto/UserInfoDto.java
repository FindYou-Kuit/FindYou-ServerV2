package com.kuit.findyou.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserInfoDto(
        @Schema(description = "사용자 식별자", example = "1")
        Long userId,
        @Schema(description = "사용자 닉네임", example = "유저1")
        String nickname,
        @Schema(description = "찾아유 엑세스 토큰", example = "token1234token1234token1234")
        String accessToken
) {
}
