package com.kuit.findyou.domain.user.dto;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "관심동물 목록 조회 응답 DTO")
public record RetrieveInterestAnimalsResponse(
        @Schema(description = "관심동물 목록")
        List<InterestAnimalPreview> interestAnimals,
        @Schema(description = "페이지에서 조회된 마지막 데이터의 id")
        long lastId,
        @Schema(description = "마지막 페이지 여부")
        boolean isLast
) {
    public static RetrieveInterestAnimalsResponse from(List<ReportProjection> reportProjections, long lastId, boolean isLast) {
        return new RetrieveInterestAnimalsResponse(reportProjections.stream()
                .map(InterestAnimalPreview::from)
                .collect(Collectors.toList()),
                lastId,
                isLast);
    }

    @Schema(description = "관심동물")
    public record InterestAnimalPreview(
            @Schema(description = "게시글 id")
            Long reportId,
            @Schema(description = "썸네일 이미지")
            String thumbnailImageUrl,
            @Schema(description = "제목")
            String title,
            @Schema(description = "신고글 태그")
            String tag,
            @Schema(description = "날짜")
            LocalDate date,
            @Schema(description = "주소")
            String address
    ){
        public static InterestAnimalPreview from(ReportProjection proj){
            return new InterestAnimalPreview(proj.getReportId(), proj.getThumbnailImageUrl(), proj.getTitle(), proj.getTag(), proj.getDate(), proj.getAddress());
        }

    }
}
