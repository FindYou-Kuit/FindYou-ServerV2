package com.kuit.findyou.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 여부 조회 요청 DTO" )
public record CheckDuplicateNicknameRequest(
        @Schema(description = "중복여부를 확인할 닉네임",
                example = "찾아유")
        String nickname
) {
}
