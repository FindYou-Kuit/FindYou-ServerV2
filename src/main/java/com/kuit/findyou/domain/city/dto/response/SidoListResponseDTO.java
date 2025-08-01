package com.kuit.findyou.domain.city.dto.response;

import com.kuit.findyou.domain.report.dto.response.Card;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SidoListResponseDTO(

        @Schema(
                description = "시도 리스트",
                type = "array",
                implementation = SidoDTO.class,
                example = """
                        [
                          {
                            "id": 1,
                            "name": "서울특별시"
                          },
                          {
                            "id": 2,
                            "name": "부산광역시"
                          }
                        ]
                        """
        )
        List<SidoDTO> sidoList
) {
}
