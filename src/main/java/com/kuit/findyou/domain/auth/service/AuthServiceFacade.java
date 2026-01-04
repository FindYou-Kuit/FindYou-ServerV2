package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.ReissueTokenRequest;
import com.kuit.findyou.domain.auth.dto.ReissueTokenResponse;
import com.kuit.findyou.domain.auth.dto.request.GuestLoginRequest;
import com.kuit.findyou.domain.auth.dto.request.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.AdminLoginResponse;
import com.kuit.findyou.domain.auth.dto.response.GuestLoginResponse;
import com.kuit.findyou.domain.auth.dto.response.KakaoLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthServiceFacade {
    private final LoginService loginService;
    private final ReissueTokenService reissueTokenService;
    private final AdminLoginService adminLoginService;

    public KakaoLoginResponse kakaoLogin(KakaoLoginRequest request) {
        return loginService.kakaoLogin(request);
    }

    public GuestLoginResponse guestLogin(GuestLoginRequest request) {
        return loginService.guestLogin(request);
    }

    public ReissueTokenResponse reissueToken(ReissueTokenRequest request) {
        return reissueTokenService.reissueToken(request);
    }

    public AdminLoginResponse adminLogin() {
        return adminLoginService.adminLogin();
    }
}
