package com.kuit.findyou.domain.city.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SigunguListResponseDTO(
        @Schema(
                description = "시/군/구 이름 리스트",
                example = """
            {
              "sigunguList": ["강남구", "강동구", "강북구"]
            }
        """
        )
        List<String> sigunguList
) {
}
