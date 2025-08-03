package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.GuestLoginRequest;
import com.kuit.findyou.domain.auth.dto.GuestLoginResponse;
import com.kuit.findyou.domain.auth.dto.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.KakaoLoginResponse;

public interface AuthService {
    KakaoLoginResponse kakaoLogin(KakaoLoginRequest request);

    GuestLoginResponse guestLogin(GuestLoginRequest request);
}
