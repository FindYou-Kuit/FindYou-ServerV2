package com.kuit.findyou.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record WitnessReportDetailResponseDTO(
        @Schema(description = "목격 신고 이미지 URL 목록", example = "[\"https://example.com/witness1.jpg\"]")
        List<String> imageUrls,

        @Schema(description = "품종", example = "믹스견")
        String breed,

        @Schema(description = "태그", example = "목격신고")
        String tag,

        @Schema(description = "털색", example = "흰색")
        String furColor,

        @Schema(description = "특징", example = "공원에서 혼자 돌아다니는 강아지를 봤습니다")
        String significant,

        @Schema(description = "목격 장소", example = "홍대입구역 2번 출구")
        String witnessLocation,

        @Schema(description = "목격 주소", example = "서울시 마포구 홍대로 321")
        String witnessAddress,

        @Schema(description = "위도", example = "37.557112")
        Double latitude,

        @Schema(description = "경도", example = "126.925643")
        Double longitude,

        @Schema(description = "신고자 정보", example = "이영희")
        String reporterInfo,

        @Schema(description = "목격 날짜", example = "2024-01-14")
        String witnessDate,

        @Schema(description = "관심글 여부", example = "true")
        boolean interest
) {
}