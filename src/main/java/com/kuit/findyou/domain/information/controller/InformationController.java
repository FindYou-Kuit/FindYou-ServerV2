package com.kuit.findyou.domain.information.controller;

import com.kuit.findyou.domain.information.dto.*;
import com.kuit.findyou.domain.information.dto.response.*;
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

import static com.kuit.findyou.domain.information.validation.InformationRequestValidator.*;
import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.*;

@RestController
@Slf4j
@RequestMapping("/api/v2/informations")
@RequiredArgsConstructor
@Tag(name = "Information", description = "정보 조회 API - 보호센터, 추천 콘텐츠")
public class InformationController {
    private final InformationServiceFacade informationServiceFacade;

    @Operation(summary = "보호센터 조회", description = "사용자 위치 또는 관할구역/유형으로 보호센터를 조회합니다.")
    @GetMapping("/protection-centers")
    @CustomExceptionDescription(DEFAULT)
    public BaseResponse<AnimalCenterPagingResponse<AnimalCenterResponse>> getCenters(
            @Parameter(hidden = true) @LoginUserId Long userId,

            @Parameter(description = "커서 페이징용 마지막 ID", example = "10")
            @RequestParam(defaultValue = "0") Long lastId,

            @Parameter(description = "관할구역 전체 문자열", example = "서울특별시 송파구")
            @RequestParam(defaultValue = "") String district,

            //TODO #28 머지 후 validator 적용하기
            @Parameter(description = "위도", example = "37.4967")
            @RequestParam(defaultValue = "") String lat,

            @Parameter(description = "경도", example = "127.0623")
            @RequestParam(name = "long", defaultValue = "") String lng,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        String normalizedDistrict = (district == null || district.isBlank()) ? null : district.trim();
        Double latVal = parseDoubleOrNull(lat);
        Double lonVal = parseDoubleOrNull(lng);

        Long cursor = validateCursor(lastId);
        validateGeoOrFilter(latVal, lonVal, normalizedDistrict);
        validateLatLngPair(latVal, lonVal);
        validatePageSize(size);
        boolean hasGeo = (latVal != null && lonVal != null);


        //위경도가 있으면 반경 조회, 없으면 일반 조회
        return BaseResponse.ok(
                hasGeo
                        ? informationServiceFacade.getNearbyCenters(cursor, latVal, lonVal, size)
                        : informationServiceFacade.getCenters(cursor, normalizedDistrict, size)
        );
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

    @Operation(summary = "봉사활동 조회 API", description = """
봉사활동 목록을 조회합니다.

**[주의]** 커서 페이징을 지원합니다.
- 첫 요청에서는 lastId를 Long타입의 MAX 값으로 세팅해서 전달해주세요 
- 다음 요청에는 이전 요청의 lastId를 세팅해서 전달해주세요. 
""")
    @CustomExceptionDescription(DEFAULT)
    @GetMapping("volunteer-works")
    public BaseResponse<GetVolunteerWorksResponse> getVolunteerWorks(@Parameter @RequestParam Long lastId){
        return BaseResponse.ok(informationServiceFacade.getVolunteerWorks(lastId));
    }

    @Operation(summary = "보호부서 조회 API", description = """
보호부서 목록을 조회합니다.

**[주의]** 커서 페이징을 지원합니다.
- 첫 요청에서는 lastId를 0 or 미지정 (null)
- 다음 요청에는 이전 응답의 lastId를 세팅해서 전달해주세요. 마지막 페이지인 경우 null
""")
    @CustomExceptionDescription(DEFAULT)
    @GetMapping("/departments")
    public BaseResponse<GetAnimalDepartmentsResponse> getDepartments(
            @Parameter(description = "커서 페이징용 마지막 ID", example = "0")
            @RequestParam(defaultValue = "0") Long lastId,

            @Parameter(description = "담당기관 전체 문자열", example = "서울특별시 광진구")
            @RequestParam(defaultValue = "") String district
    ) {
        String normalizedDistrict = (district == null || district.isBlank()) ? null : district.trim(); //공백제거
        return BaseResponse.ok(informationServiceFacade.getDepartments(lastId, normalizedDistrict));
    }
}
