package com.kuit.findyou.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카드 목록 응답 DTO")
public record CardResponseDTO(
        @Schema(
                description = "카드 리스트",
                type = "array",
                implementation = Card.class,
                example = """
                        [
                          {
                            "reportId": 1,
                            "thumbnailImageUrl": "image1.png",
                            "title": "말티즈",
                            "tag": "보호중",
                            "date": "2025-07-01",
                            "location": "성산구 내동 628-1",
                            "interest": true
                          },
                          {
                            "reportId": 2,
                            "thumbnailImageUrl": "image2.png",
                            "title": "푸들",
                            "tag": "실종신고",
                            "date": "2025-06-30",
                            "location": "강남구 논현동",
                            "interest": false
                          }
                        ]
                        """
        )
        List<Card> cards,

        @Schema(description = "마지막으로 조회된 글의 ID", example = "25")
        Long lastReportId,

        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean isLast
) {
}
