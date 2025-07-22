package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.KakaoLoginResponse;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    public KakaoLoginResponse kakaoLogin(KakaoLoginRequest request) {
        log.info("[kakaoLogin] kakaoId = {}", request.kakaoId());

        return userRepository.findByKakaoId(request.kakaoId())
                .map(loginUser -> {
                    log.info("[kakaoLogin] user found");
                    String token = jwtUtil.createAccessJwt(loginUser.getId(), loginUser.getRole());
                    return KakaoLoginResponse.fromUserAndAccessToken(loginUser, token);
                })
                .orElseGet(() -> {
                    log.info("[kakaoLogin] user not found");
                    return KakaoLoginResponse.notFound();
                });
    }
}
