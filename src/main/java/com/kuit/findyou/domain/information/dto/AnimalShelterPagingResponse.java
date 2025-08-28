package com.kuit.findyou.domain.information.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "보호센터 커서 페이징 응답 DTO")
public record AnimalShelterPagingResponse<T>(
        @Schema(description = "센터 목록")
        List<T> centers,

        @Schema(description = "다음 페이지 요청에 사용할 lastId. 더 없으면 null")
        Long lastId,

        @Schema(description = "마지막 페이지 여부")
        boolean isLast
) {

}
