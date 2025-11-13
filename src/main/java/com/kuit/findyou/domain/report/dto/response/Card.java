package com.kuit.findyou.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "글 카드 정보")
public record Card(
        @Schema(description = "글 Id", example = "1")
        Long reportId,
        @Schema(description = "썸네일 이미지 url", example = "image1.png")
        String thumbnailImageUrl,
        @Schema(description = "글 제목", example = "말티즈")
        String title,
        @Schema(description = "태그", example = "보호중")
        String tag,
        @Schema(description = "날짜 (발견 날짜/분실 날짜/목격 날짜)", example = "2025-07-01")
        String date,
        @Schema(description = "장소 (발견 장소/분실 장소/목격 장소)", example = "성산구 내동 628-1")
        String location,
        @Schema(description = "관심 여부", example = "true")
        boolean interest
) {}
