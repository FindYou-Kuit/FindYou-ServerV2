package com.kuit.findyou.domain.home.controller;

import com.kuit.findyou.domain.home.dto.GetHomeRequest;
import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.service.HomeServiceFacade;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.GET_HOME;

@Tag(name = "Home", description = "홈화면 관련 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/v2/home")
@RestController
public class HomeController {
    private final HomeServiceFacade homeServiceFacade;

    @Operation(
            summary = "홈화면 조회 API",
            description = """
                    홈화면을 조회하는 API입니다. 통계 정보와 추천글 카드를 응답으로 받을 수 있습니다.
                    """
    )
    @CustomExceptionDescription(GET_HOME)
    @GetMapping
    public BaseResponse<GetHomeResponse> getHome(@ModelAttribute GetHomeRequest request){
        log.info("[getHome] latitude = {} longitude = {}", request.lat(), request.lng());
        return new BaseResponse<>(homeServiceFacade.getHome(request.lat(), request.lng()));
    }
}
