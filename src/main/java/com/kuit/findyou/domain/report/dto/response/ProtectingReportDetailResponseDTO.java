package com.kuit.findyou.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record ProtectingReportDetailResponseDTO(
        @Schema(description = "보호 신고 이미지 URL 목록", example = "[\"https://example.com/protecting1.jpg\", \"https://example.com/protecting2.jpg\"]")
        List<String> imageUrls,

        @Schema(description = "품종", example = "골든 리트리버")
        String breed,

        @Schema(description = "태그", example = "보호중")
        String tag,

        @Schema(description = "나이", example = "3살")
        String age,

        @Schema(description = "체중", example = "25kg")
        String weight,

        @Schema(description = "털색", example = "황금색")
        String furColor,

        @Schema(description = "성별", example = "수컷")
        String sex,

        @Schema(description = "중성화 여부", example = "Y")
        String neutering,

        @Schema(description = "특징", example = "귀여운 골든 리트리버입니다")
        String significant,

        @Schema(description = "보호소 이름", example = "강남동물보호소")
        String careName,

        @Schema(description = "보호소 주소", example = "서울시 강남구 보호소로 456")
        String careAddr,

        @Schema(description = "위도", example = "37.566512")
        double latitude,

        @Schema(description = "경도", example = "126.978006")
        double longitude,

        @Schema(description = "보호소 연락처", example = "02-1234-5678")
        String careTel,

        @Schema(description = "발견 날짜", example = "2024-01-10")
        String foundDate,

        @Schema(description = "발견 장소", example = "강남역 근처")
        String foundLocation,

        @Schema(description = "공고 기간", example = "2024-01-10 ~ 2024-01-20")
        String noticeDuration,

        @Schema(description = "공고 번호", example = "2024-001123132")
        String noticeNumber,

        @Schema(description = "관할 기관", example = "서울시 강남구청")
        String authority,

        @Schema(description = "관심글 여부", example = "true")
        boolean interest
) {
}