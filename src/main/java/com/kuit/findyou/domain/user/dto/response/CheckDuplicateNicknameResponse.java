package com.kuit.findyou.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 여부 조회 응답 DTO" )
public record CheckDuplicateNicknameResponse(
        @Schema(description = "닉네임 중복 여부",
                example = "true")
        boolean isDuplicate
) {
}
