package com.kuit.findyou.domain.city.controller;

import com.kuit.findyou.domain.city.dto.response.SidoListResponseDTO;
import com.kuit.findyou.domain.city.service.facade.CityServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.DEFAULT;

@RestController
@Slf4j
@Tag(name = "Sido/Sigungu", description = "시도/시군구 관련 API")
@RequiredArgsConstructor
public class CityController {

    private final CityServiceFacade cityServiceFacade;

    @Operation(summary = "시도 정보 반환 API", description = "시도 정보를 반환하기 위한 API")
    @GetMapping("/api/v2/sidos")
    @CustomExceptionDescription(DEFAULT)
    public BaseResponse<SidoListResponseDTO> getSidoNames() {
        return BaseResponse.ok(cityServiceFacade.getSidoNames());
    }
}
