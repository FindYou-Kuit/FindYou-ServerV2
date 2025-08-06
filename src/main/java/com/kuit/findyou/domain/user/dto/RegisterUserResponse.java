package com.kuit.findyou.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원정보 등록 응답 DTO")
public record RegisterUserResponse(
        @Schema(description = "유저 ID",
                example = "1234")
        Long userId,
        @Schema(description = "유저 닉네임",
                example = "찾아유")
        String nickname,
        @Schema(description = "엑세스 토큰",
                example = "accessToken")
        String accessToken
) {
}
