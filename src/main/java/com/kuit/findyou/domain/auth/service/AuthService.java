package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.KakaoLoginResponse;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    public KakaoLoginResponse kakaoLogin(KakaoLoginRequest request) {
        log.info("[kakaoLogin] kakaoId = {}", request.kakaoId());

        // 유저를 찾는다
        Optional<User> optUser = userRepository.findByKakaoId(request.kakaoId());

        // 없으면 회원가입 유도
        if(optUser.isEmpty()){
            return KakaoLoginResponse.notFound();
        }

        // 있으면 회원정보와 엑세스 토큰 발급
        User loginUser = optUser.get();
        String token = jwtUtil.createAccessJwt(loginUser.getId());
        return KakaoLoginResponse.fromUserAndAccessToken(loginUser, token);
    }
}
