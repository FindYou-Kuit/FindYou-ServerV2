package com.kuit.findyou.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MissingReportDetailResponseDTO(
        @Schema(description = "실종 신고 이미지 URL 목록", example = "[\"https://example.com/missing1.jpg\", \"https://example.com/missing2.jpg\"]")
        List<String> imageUrls,

        @Schema(description = "품종", example = "웰시코기")
        String breed,

        @Schema(description = "태그", example = "실종신고")
        String tag,

        @Schema(description = "나이", example = "1살")
        String age,

        @Schema(description = "성별", example = "수컷")
        String sex,

        @Schema(description = "실종 날짜", example = "2024-01-15")
        String missingDate,

        @Schema(description = "RFID 번호", example = "RFID123456")
        String rfid,

        @Schema(description = "특징", example = "나비라는 이름의 귀여운 웰시코기입니다")
        String significant,

        @Schema(description = "실종 장소", example = "서초역 1번 출구")
        String missingLocation,

        @Schema(description = "실종 주소", example = "서울시 서초구 서초대로 789")
        String missingAddress,

        @Schema(description = "위도", example = "37.491916")
        double latitude,

        @Schema(description = "경도", example = "127.007912")
        double longitude,

        @Schema(description = "신고자 이름", example = "김철수")
        String reporterName,

        @Schema(description = "신고자 연락처", example = "010-1234-5678")
        String reporterTel,

        @Schema(description = "관심글 여부", example = "true")
        boolean interest
) {
}
