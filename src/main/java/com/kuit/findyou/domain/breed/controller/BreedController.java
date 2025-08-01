package com.kuit.findyou.domain.breed.controller;


import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.breed.service.facade.BreedServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.DEFAULT;

@RestController
@Tag(name = "Breed", description = "품종 관련 API")
@RequestMapping("api/v2/breeds")
@RequiredArgsConstructor
public class BreedController {

    private final BreedServiceFacade breedServiceFacade;

    @Operation(summary = "품종 정보 반환 API", description = "품종 정보를 반환하기 위한 API")
    @GetMapping
    @CustomExceptionDescription(DEFAULT)
    public BaseResponse<BreedListResponseDTO> getBreedList() {
        return BaseResponse.ok(breedServiceFacade.getBreedList());
    }
}
