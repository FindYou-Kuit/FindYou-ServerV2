package com.kuit.findyou.domain.auth.controller;

import com.kuit.findyou.domain.auth.dto.request.GuestLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.GuestLoginResponse;
import com.kuit.findyou.domain.auth.dto.request.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.KakaoLoginResponse;
import com.kuit.findyou.domain.auth.service.AuthService;
import com.kuit.findyou.global.common.annotation.CustomExceptionDescription;
import com.kuit.findyou.global.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kuit.findyou.global.common.swagger.SwaggerResponseDescription.GUEST_LOGIN;
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
    @PostMapping("/login/kakao")
    @CustomExceptionDescription(KAKAO_LOGIN)
    public BaseResponse<KakaoLoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request){
        log.info("[kakaoLogin]");
        return BaseResponse.ok(authService.kakaoLogin(request));
    }

    @Operation(
            summary = "게스트 로그인 API",
            description = "디바이스id 식별자를 이용해서 유저 정보와 엑세스 토큰을 얻을 수 있습니다. 기존 게스트가 아니면 별도의 가입 API 호출 없이 정보가 자동으로 저장됩니다."
    )
    @PostMapping("/login/guest")
    @CustomExceptionDescription(GUEST_LOGIN)
    public BaseResponse<GuestLoginResponse> guestLogin(@RequestBody GuestLoginRequest request){
        log.info("[guestLogin] deviceId = {}", request.deviceId());
        return BaseResponse.ok(authService.guestLogin(request));
    }
}
