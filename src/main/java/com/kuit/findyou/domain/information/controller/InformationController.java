package com.kuit.findyou.domain.information.controller;

import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.service.facade.InformationServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.exception.CustomException;
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

import static com.kuit.findyou.domain.information.validation.InformationRequestValidator.*;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;
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

            //TODO #28 머지 후 validator 적용하기
            @Parameter(description = "위도", example = "37.4967")
            @RequestParam(defaultValue = "") String lat,

            @Parameter(description = "경도", example = "127.0623")
            @RequestParam(name = "long", defaultValue = "") String lng,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        String typeNorm = normalizeType(type);
        String sidoNorm = nullIfBlank(sido);
        String sigunguNorm = nullIfBlank(sigungu);
        Double latVal = parseDoubleOrNull(lat);
        Double lonVal = parseDoubleOrNull(lng);

        Long cursor = validateCursor(lastId);
        validateGeoOrFilter(latVal, lonVal, sidoNorm, sigunguNorm);
        validateLatLngPair(latVal, lonVal);
        boolean hasGeo = (latVal != null && lonVal != null);


        //위경도가 있으면 반경 조회, 없으면 일반 조회
        List<AnimalShelterResponse> results = (hasGeo) ? informationServiceFacade.getNearbyCenters(cursor, latVal, lonVal, size):informationServiceFacade.getShelters(cursor, typeNorm, sidoNorm, sigunguNorm, null, null, size);
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
