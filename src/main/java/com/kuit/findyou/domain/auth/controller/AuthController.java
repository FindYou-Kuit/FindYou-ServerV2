package com.kuit.findyou.domain.auth.controller;

import com.kuit.findyou.domain.auth.dto.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.KakaoLoginResponse;
import com.kuit.findyou.domain.auth.service.AuthService;
import com.kuit.findyou.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/v2/auth")
@RestController
public class AuthController {
    private final AuthService authService;
    @PostMapping("login/kakao")
    public BaseResponse<KakaoLoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request){
        log.info("[kakaoLogin]");
        return BaseResponse.ok(authService.kakaoLogin(request));
    }
}
