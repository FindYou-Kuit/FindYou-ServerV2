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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.GUEST_LOGIN_FAILED;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements LoginService {
    private final UserRepository userRepository;
    private final IssueTokenService issueTokenService;

    public KakaoLoginResponse kakaoLogin(KakaoLoginRequest request) {
        log.info("[kakaoLogin] kakaoId = {}", request.kakaoId());

        return userRepository.findByKakaoId(request.kakaoId())
                .map(user -> {
                    String accessToken = issueTokenService.issueAccessToken(user.getId(), user.getRole());
                    String refreshToken = issueTokenService.issueRefreshToken(user.getId());
                    log.info("[kakaoLogin] 카카오 로그인 성공");
                    return KakaoLoginResponse.fromUserAndTokens(user, accessToken, refreshToken);
                })
                .orElseGet(() -> {
                    log.info("[kakaoLogin] 일치하는 유저가 없어서 카카오 로그인 실패");
                    return KakaoLoginResponse.firstLogin();
                });
    }

    @Transactional
    @Override
    public GuestLoginResponse guestLogin(GuestLoginRequest request) {
        log.info("[guestLogin] deviceId = {}", request.deviceId());

        User user = userRepository.findByDeviceId(request.deviceId())
                .orElseGet(()->{
                    // 디바이스 id에 해당하는 유저가 없으면 게스트 추가
                    log.info("[guestLogin] 새로운 게스트 추가");
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
            log.info("[guestLogin] 게스트 권한이 없어서 게스트 로그인 실패");
            throw new CustomException(GUEST_LOGIN_FAILED);
        }

        // 토큰 생성
        String accessToken = issueTokenService.issueAccessToken(user.getId(), user.getRole());
        String refreshToken = issueTokenService.issueRefreshToken(user.getId());
        log.info("[guestLogin] 게스트 로그인 성공");
        return new GuestLoginResponse(user.getId(), accessToken, refreshToken);
    }
}
