package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.request.GuestLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.GuestLoginResponse;
import com.kuit.findyou.domain.auth.dto.request.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.KakaoLoginResponse;
import com.kuit.findyou.domain.user.constant.DefaultProfileImage;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.GUEST_LOGIN_FAILED;

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

    @Transactional
    @Override
    public GuestLoginResponse guestLogin(GuestLoginRequest request) {
        log.info("[guestLogin] deviceId = {}", request.deviceId());

        User user = userRepository.findByDeviceId(request.deviceId())
                .orElseGet(()->{
                    // 디바이스 id에 해당하는 유저가 없으면 게스트 추가
                    User build = User.builder()
                            .name("게스트")
                            .profileImageUrl(DefaultProfileImage.DEFAULT.getName())
                            .role(Role.GUEST)
                            .deviceId(request.deviceId())
                            .build();
                    return userRepository.save(build);
                });

        // 게스트가 아니면 로그인 실패
        if(!user.isGuest()){
            throw new CustomException(GUEST_LOGIN_FAILED);
        }

        // 응답 반환
        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());
        return new GuestLoginResponse(user.getId(), accessToken);
    }
}
