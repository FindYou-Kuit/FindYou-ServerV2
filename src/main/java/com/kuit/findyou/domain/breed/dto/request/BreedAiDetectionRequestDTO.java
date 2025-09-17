package com.kuit.findyou.domain.breed.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BreedAiDetectionRequestDTO(
        @NotBlank(message = "이미지 Base64 데이터는 비어 있을 수 없습니다.")
        @JsonProperty(value = "base64Image")
        @Schema(description = "품종을 판별하고자 하는 이미지의 Base64 인코딩된 문자열")
        String base64Image) {
}
