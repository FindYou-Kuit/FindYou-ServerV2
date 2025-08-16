package com.kuit.findyou.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관심동물 등록 DTO")
public record  AddInterestAnimalRequest (
        @Schema(description = "신고글 식별자", example = "1")
        Long reportId
){
}
