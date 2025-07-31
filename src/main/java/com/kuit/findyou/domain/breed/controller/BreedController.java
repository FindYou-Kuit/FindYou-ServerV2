package com.kuit.findyou.domain.breed.controller;


import com.kuit.findyou.domain.breed.dto.request.ImageUrlRequestDTO;
import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.breed.service.facade.BreedServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.BREED_AI_DETECTION;
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

    @Operation(summary = "품종 AI 판별 API", description = "AI를 활용해 품종을 판별하기 위한 API")
    @PostMapping("/ai-detection")
    @CustomExceptionDescription(BREED_AI_DETECTION)
    public BaseResponse<BreedAiDetectionResponseDTO> analyzeBreedWithAi(@Valid @RequestBody ImageUrlRequestDTO request) {
        return BaseResponse.ok(breedServiceFacade.analyzeBreedWithAi(request.imageUrl()));
    }
}
