package com.kuit.findyou.domain.breed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record BreedDTO(
        @Schema(description = "품종", example = "골든 리트리버")
        String breedName,
        @Schema(description = "축종", example = "개")
        String species
) {
}
