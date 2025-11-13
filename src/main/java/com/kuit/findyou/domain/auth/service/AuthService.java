package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.request.GuestLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.GuestLoginResponse;
import com.kuit.findyou.domain.auth.dto.request.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.KakaoLoginResponse;

public interface AuthService {
    KakaoLoginResponse kakaoLogin(KakaoLoginRequest request);

    GuestLoginResponse guestLogin(GuestLoginRequest request);
}
