package com.kuit.findyou.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게스트 로그인 요청 DTO")
public record GuestLoginRequest(
        @Schema(description = "디바이스 id", example = "asdf1234asdf")
        String deviceId
) {
}
