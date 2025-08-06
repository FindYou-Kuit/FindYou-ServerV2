package com.kuit.findyou.domain.breed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;

public record BreedListResponseDTO(

        @Schema(
                description = "강아지 품종 리스트",
                example = "[\"골든 리트리버\", \"진도견\"]"
        )
        List<String> dogBreedList,

        @Schema(
                description = "고양이 품종 리스트",
                example = "[\"러시안 블루\", \"페르시안\"]"
        )
        List<String> catBreedList,

        @Schema(
                description = "기타 동물 품종 리스트",
                example = "[\"기타축종\"]"
        )
        List<String> etcBreedList

) {
}

