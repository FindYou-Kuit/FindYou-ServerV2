package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.repository.RedisRefreshTokenRepository;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IssueTokenServiceImpl implements IssueTokenService {
    private final JwtUtil jwtUtil;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;

    @Override
    public String issueAccessToken(Long userId, Role role) {
        return jwtUtil.createAccessJwt(userId, role);
    }

    @Override
    public String issueRefreshToken(Long userId) {
        String refreshToken = jwtUtil.createRefreshJwt(userId);
        redisRefreshTokenRepository.save(userId, refreshToken);
        return refreshToken;
    }
}
