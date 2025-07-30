package com.kuit.findyou.domain.city.dto.response;

import com.kuit.findyou.domain.report.dto.response.Card;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SidoNameResponseDTO(
        @Schema(
                description = "시/도 이름 리스트",
                example = "[\"서울특별시\", \"부산광역시\"]",
                type = "array"
        )
        List<String> sidoNames
) {
}

