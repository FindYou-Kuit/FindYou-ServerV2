package com.kuit.findyou.domain.information.controller;

import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.service.facade.RecommendedContentFacade;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.*;


@RestController
@Slf4j
@RequestMapping("/api/v2/informations")
@RequiredArgsConstructor
@Tag(name = "추천 콘텐츠", description = "영상 / 기사 추천 API")
public class RecommendedContentController {

    private final RecommendedContentFacade recommendedContentFacade;

    @Operation(summary = "추천 영상 조회 API", description = "추천 영상 목록을 조회합니다.")
    @GetMapping("/videos")
    @CustomExceptionDescription(RECOMMENDED_VIDEO)
    public BaseResponse<List<RecommendedContentResponse>> getRecommendedVideos(
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        List<RecommendedContentResponse> response = recommendedContentFacade.getRecommendedContents(ContentType.VIDEO);
        return BaseResponse.ok(response);
    }

    @Operation(summary = "추천 기사 조회 API", description = "추천 기사 목록을 조회합니다.")
    @GetMapping("/news")
    @CustomExceptionDescription(RECOMMENDED_NEWS)
    public BaseResponse<List<RecommendedContentResponse>> getRecommendedNews(
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        List<RecommendedContentResponse> response = recommendedContentFacade.getRecommendedContents(ContentType.NEWS);
        return BaseResponse.ok(response);
    }
}