package com.kuit.findyou.domain.home.controller;

import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.service.HomeServiceFacade;
import com.kuit.findyou.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/v2/home")
@RestController
public class HomeController {
    private final HomeServiceFacade homeServiceFacade;
    @GetMapping
    public BaseResponse<GetHomeResponse> getHome(@RequestParam(name = "lat", required = false) Double latitude,
                                                 @RequestParam(name = "lng", required = false) Double longitude){
        log.info("[getHome] latitude = {} longitude = {}", latitude, longitude);
        return new BaseResponse<>(homeServiceFacade.getHome(latitude, longitude));
    }
}
