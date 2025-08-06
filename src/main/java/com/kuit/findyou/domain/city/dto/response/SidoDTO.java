package com.kuit.findyou.domain.city.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SidoDTO(
        @Schema(name = "시도 ID", example = "1")
        Long id,
        @Schema(name = "시도 이름", example = "서울특별시")
        String name
) {
}

