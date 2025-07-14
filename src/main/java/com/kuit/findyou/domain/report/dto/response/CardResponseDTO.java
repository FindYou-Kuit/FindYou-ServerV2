package com.kuit.findyou.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카드 목록 응답 DTO")
public record CardResponseDTO(
        @Schema(description = "카드 리스트", implementation = Card.class)
        List<Card> cards,

        @Schema(description = "마지막으로 조회된 글의 ID", example = "25")
        Long lastReportId,

        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean isLast
) {
}
