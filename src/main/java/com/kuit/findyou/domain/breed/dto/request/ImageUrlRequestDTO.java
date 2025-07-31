package com.kuit.findyou.domain.breed.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record ImageUrlRequestDTO(
        @JsonProperty(value = "image_url")
        @Schema(description = "품종을 판별하고자 하는 이미지의 url", example = "http://image1.png")
        String imageUrl) {
}
