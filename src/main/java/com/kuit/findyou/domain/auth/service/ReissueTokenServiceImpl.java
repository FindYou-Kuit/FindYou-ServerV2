package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.ReissueTokenRequest;
import com.kuit.findyou.domain.auth.dto.ReissueTokenResponse;
import com.kuit.findyou.domain.auth.repository.RedisRefreshTokenRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReissueTokenServiceImpl implements ReissueTokenService {
    private final JwtUtil jwtUtil;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;
    private final UserRepository userRepository;
    @Override
    public ReissueTokenResponse reissueToken(ReissueTokenRequest request) {
        log.info("[reissueToken] 토큰 재발급 시작");
        // 리프레시 토큰 검증
        jwtUtil.validateJwt(request.refreshToken());

        Long userId = jwtUtil.getUserId(request.refreshToken());

        // 저장된 리프레시 토큰이 없으면 에러
        String foundRefreshToken = redisRefreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.info("[reissueToken] 토큰이 존재하지 않음");
                    return new CustomException(REFRESH_TOKEN_NOT_FOUND);
                });

        // 토큰이 일차히지 않으면 에러
        if(!foundRefreshToken.equals(request.refreshToken())){
            log.info("[reissueToken] 토큰이 일치하지 않음");
            throw new CustomException(REFRESH_TOKEN_NOT_FOUND);
        }

        // 토큰이 일치하면 토큰 재발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());
        String refreshToken = jwtUtil.createRefreshJwt(user.getId());

        redisRefreshTokenRepository.save(user.getId(), refreshToken);

        log.info("[reissueToken] 토큰 재발급 완료");
        return new ReissueTokenResponse(accessToken, refreshToken);
    }
}
