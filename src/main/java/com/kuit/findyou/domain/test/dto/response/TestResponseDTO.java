package com.kuit.findyou.domain.test.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TestResponseDTO(
        @Schema(description = "테스트 응답 메세지", example = "테스트용 성공 응답 메세지입니다.")
        String message) {
}
