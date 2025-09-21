package com.kuit.findyou.domain.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "홈화면 조회 응답 DTO")
public record GetHomeResponse(
        TotalStatistics statistics,
        List<ProtectingAnimalPreview> protectingAnimals,
        List<WitnessedOrMissingAnimalPreview> witnessedOrMissingAnimals
) {
    @Schema(description = "전체 통계 정보")
    public record TotalStatistics(
            @Schema(description = "최근 7일 통계")
            Statistics recent7days,
            @Schema(description = "최근 3개월 통계")
            Statistics recent3months,
            @Schema(description = "최근 1년 통계")
            Statistics recent1Year
    ){
        public static TotalStatistics empty(){
            return new TotalStatistics(Statistics.empty(), Statistics.empty(), Statistics.empty());
        }
    }

    @Schema(description = "통계 정보")
    public record Statistics(
            @Schema(description = "구조", example = "2000")
            String rescuedAnimalCount,
            @Schema(description = "보호중", example = "2000")
            String protectingAnimalCount,
            @Schema(description = "입양", example = "2000")
            String adoptedAnimalCount,
            @Schema(description = "신고", example = "2000")
            String lostAnimalCount
    ){
        public static Statistics empty(){
            return new Statistics("-", "-", "-", "-");
        }
    }
}
