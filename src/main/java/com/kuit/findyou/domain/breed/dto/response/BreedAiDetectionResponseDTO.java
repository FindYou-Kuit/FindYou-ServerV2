package com.kuit.findyou.domain.breed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "GPT Vision API 를 통해 추출된 품종 판별 결과")
public record BreedAiDetectionResponseDTO(
        @Schema(description = "축종 (예: 강아지, 고양이, 기타)", example = "강아지")
        String species,

        @Schema(description = "품종 이름", example = "골든 리트리버")
        String breed,

        @Schema(description = "털 색상 목록", example = "[\"흰색\", \"노란색\", \"기타\"]")
        List<String> furColors
) {
}
