package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.response.AdminLoginResponse;
import com.kuit.findyou.domain.auth.repository.RedisRefreshTokenRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AdminLoginServiceImpl implements AdminLoginService{
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;

    @Value("${admin.admin-user-id}")
    private Long adminUserId;

    @Value("${admin.refresh-ttl-ms}")
    private Long adminRefreshTtlMs;

    @Override
    public AdminLoginResponse adminLogin() {
        User user = userRepository.findById(adminUserId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());
        String refreshToken = jwtUtil.createRefreshJwt(user.getId(), adminRefreshTtlMs);

        // 관리자 계정만 TTL 1년으로 저장
        redisRefreshTokenRepository.save(user.getId(), refreshToken, adminRefreshTtlMs);

        return new AdminLoginResponse(accessToken, refreshToken);
    }
}
