package com.kuit.findyou.domain.information.controller;

import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.service.facade.InformationServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.jwt.annotation.LoginUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.*;

@RestController
@Slf4j
@RequestMapping("/api/v2/informations")
@RequiredArgsConstructor
@Tag(name = "Information", description = "정보 조회 API - 보호소/병원, 추천 콘텐츠")
public class InformationController {
    private final InformationServiceFacade informationServiceFacade;

    @Operation(summary = "보호소/병원 조회", description = "사용자 위치 또는 관할구역/유형으로 보호소/병원을 조회합니다.")
    @GetMapping("/shelters-and-hospitals")
    @CustomExceptionDescription(DEFAULT)
    public BaseResponse<Map<String, List<AnimalShelterResponse>>> getSheltersAndHospitals(
            @Parameter(hidden = true) @LoginUserId Long userId, // @LoginUserId로 통일

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
        // 이 뷰에 최초에 접근할 때 사용자 위치 기반 조회 (반경 3km)
        if (!lat.isBlank() && !lng.isBlank()) {
            Double latitude = Double.parseDouble(lat);
            Double longitude = Double.parseDouble(lng);
            List<AnimalShelterResponse> results = informationServiceFacade.getNearbyCenters(lastId, latitude, longitude);
            return BaseResponse.ok(Map.of("centers", results));
        }
        List<AnimalShelterResponse> results = informationServiceFacade.getShelters(lastId, type, sido, sigungu, null, null);
        return BaseResponse.ok(Map.of("centers", results));
    }


    @Operation(summary = "추천 영상 조회 API", description = "추천 영상 목록을 조회합니다.")
    @GetMapping("/videos")
    @CustomExceptionDescription(RECOMMENDED_VIDEO)
    public BaseResponse<List<RecommendedContentResponse>> getRecommendedVideos(
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        List<RecommendedContentResponse> response = informationServiceFacade.getRecommendedContents(ContentType.VIDEO);
        return BaseResponse.ok(response);
    }

    @Operation(summary = "추천 기사 조회 API", description = "추천 기사 목록을 조회합니다.")
    @GetMapping("/news")
    @CustomExceptionDescription(RECOMMENDED_NEWS)
    public BaseResponse<List<RecommendedContentResponse>> getRecommendedNews(
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        List<RecommendedContentResponse> response = informationServiceFacade.getRecommendedContents(ContentType.NEWS);
        return BaseResponse.ok(response);
    }
}
