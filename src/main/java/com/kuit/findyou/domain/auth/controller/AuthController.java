package com.kuit.findyou.domain.auth.controller;

import com.kuit.findyou.domain.auth.dto.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.KakaoLoginResponse;
import com.kuit.findyou.domain.auth.service.AuthService;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.common.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.KAKAO_LOGIN;

@Tag(name = "Login", description = "로그인 관련 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/v2/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "카카오 로그인 API",
            description = "카카오 사용자 식별자를 이용해서 유저 정보와 엑세스 토큰을 얻을 수 있습니다. 가입된 회원인지 여부를 반환합니다."
    )
    @PostMapping("login/kakao")
    @CustomExceptionDescription(KAKAO_LOGIN)
    public BaseResponse<KakaoLoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request){
        log.info("[kakaoLogin]");
        return BaseResponse.ok(authService.kakaoLogin(request));
    }
}
