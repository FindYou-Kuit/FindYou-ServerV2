package com.kuit.findyou.domain.information.controller;


import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.service.AnimalShelterService;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.DEFAULT;

@RestController
@Tag(name = "Animal Shelter", description = "보호소 및 동물병원 조회 API")
@RequestMapping("api/v2/informations/shelters-and-hospitals")
@RequiredArgsConstructor
public class AnimalShelterController {

    private final AnimalShelterService animalShelterService;

    @Operation(summary = "보호소/병원 조회", description = "관할구역 및 유형으로 보호소/병원을 조회합니다.")
    @GetMapping
    @CustomExceptionDescription(DEFAULT)
    public BaseResponse<Map<String, List<AnimalShelterResponse>>> getShelters(
            @Parameter(description = "커서 페이징용 마지막 ID", example = "10")
            @RequestParam(defaultValue = "0") Long lastId,

            @Parameter(description = "기관 종류 (all | shelter | hospital)",example = "hospital")
            @RequestParam(defaultValue = "all") String type,

            @Parameter(description = "도/광역시", example = "서울특별시")
            @RequestParam(defaultValue = "") String sido,

            @Parameter(description = "구/리/시/읍", example = "강남구")
            @RequestParam(defaultValue = "") String sigungu,

            @Parameter(description = "위도", example = "37.4967")
            @RequestParam(defaultValue = "") String lat,

            @Parameter(description = "경도", example = "127.0623")
            @RequestParam(defaultValue = "") String lng
    ) {
        Long userId = getUserIdFromSecurityContext();

        // 이 뷰에 최초에 접근할 때 사용자 위치 기반 조회 (반경 3km)
        if (!lat.isBlank() && !lng.isBlank()) {
            Double latitude = Double.parseDouble(lat);
            Double longitude = Double.parseDouble(lng);
            List<AnimalShelterResponse> results = animalShelterService.getNearbyCenters(lastId, latitude, longitude);
            return BaseResponse.ok(Map.of("centers", results));
        }
        List<AnimalShelterResponse> results = animalShelterService.getShelters(lastId, type, sido, sigungu, null, null);
        return BaseResponse.ok(Map.of("centers", results));
    }
    private Long getUserIdFromSecurityContext() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
