package com.kuit.findyou.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 컨텐츠 목록")
public record RecommendedContentResponse(
        @Schema(description = "컨텐츠 제목", example = "강아지 키우기")
        String title,
        @Schema(description = "게시자 (유튜브 채널명 또는 언론사 )", example = "찾아유 채널")
        String uploader,
        @Schema(description = "컨텐츠 링크", example = "youtube.com")
        String url
) {
}
