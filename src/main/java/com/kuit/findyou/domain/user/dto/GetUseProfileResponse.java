package com.kuit.findyou.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 프로필 조회 API 응답 DTO ")
public record GetUseProfileResponse(
        @Schema(description = "닉네임", example = "유저1")
        String nickname,
        @Schema(description = "프로필 이미지", example = "image.png")
        String profileImage
) {
}
