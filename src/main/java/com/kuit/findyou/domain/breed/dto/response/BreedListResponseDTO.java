package com.kuit.findyou.domain.breed.dto.response;

import com.kuit.findyou.domain.city.dto.response.SidoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record BreedListResponseDTO(

        @Schema(
                description = "품종 리스트",
                type = "array",
                implementation = BreedDTO.class,
                example = """
                        [
                          {
                            "breedName": "골든 리트리버",
                            "species": "개"
                          },
                          {
                            "breedName": "치와와",
                            "species": "개"
                          }
                        ]
                        """
        )
        List<BreedDTO> breedList
) {
}
