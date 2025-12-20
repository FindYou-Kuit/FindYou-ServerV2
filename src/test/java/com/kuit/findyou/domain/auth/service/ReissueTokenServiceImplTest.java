package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.ReissueTokenRequest;
import com.kuit.findyou.domain.auth.dto.ReissueTokenResponse;
import com.kuit.findyou.domain.auth.repository.RedisRefreshTokenRepository;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.jwt.exception.JwtExpiredException;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.EXPIRED_JWT;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.REFRESH_TOKEN_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReissueTokenServiceImplTest {
    @InjectMocks
    ReissueTokenServiceImpl reissueTokenService;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    RedisRefreshTokenRepository redisRefreshTokenRepository;

    @Mock
    UserRepository userRepository;

    @DisplayName("올바른 리프레시 토큰이면 토큰을 반환한다")
    @Test
    void reissueToken_shouldReturnToken_whenValidRefreshToken(){
        // given
        Long userId = 1L;
        String existingRefreshToken = "valid refresh";
        String newAccessToken = "new access";
        String newRefreshToken = "new refresh";
        User user = User.builder().id(userId).role(Role.USER).build();

        ReissueTokenRequest request = new ReissueTokenRequest(existingRefreshToken);

        when(jwtUtil.getUserId(any(String.class))).thenReturn(userId);
        when(redisRefreshTokenRepository.findByUserId(any(Long.class))).thenReturn(Optional.of(existingRefreshToken));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(jwtUtil.createAccessJwt(any(Long.class), any(Role.class))).thenReturn(newAccessToken);
        when(jwtUtil.createRefreshJwt(any(Long.class))).thenReturn(newRefreshToken);

        // when
        ReissueTokenResponse response = reissueTokenService.reissueToken(request);

        // then
        verify(redisRefreshTokenRepository, times(1)).save(anyLong(), anyString());

        assertThat(response.accessToken()).isEqualTo(newAccessToken);
        assertThat(response.refreshToken()).isEqualTo(newRefreshToken);
    }

    @DisplayName("만료된 리프레시 토큰이면 예외를 발생시킨다")
    @Test
    void reissueToken_shouldThrowException_whenInvalidRefreshToken(){
        // given
        ReissueTokenRequest request = new ReissueTokenRequest("expired refresh");

        doThrow(new JwtExpiredException(EXPIRED_JWT)).when(jwtUtil).validateJwt(anyString());

        // when & then
        assertThatThrownBy(() -> reissueTokenService.reissueToken(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(EXPIRED_JWT.getMessage());

        verify(redisRefreshTokenRepository, never()).save(anyLong(), anyString());
    }

    @DisplayName("리프레시 토큰이 저장된 리프레시 토큰과 일치하지 않으면 예외를 발생시킨다")
    @Test
    void reissueToken_shouldThrowException_whenRefreshTokenIsNotMatched(){
        // given
        Long userId = 1L;

        ReissueTokenRequest request = new ReissueTokenRequest("old refresh");

        when(jwtUtil.getUserId(any(String.class))).thenReturn(userId);
        when(redisRefreshTokenRepository.findByUserId(any(Long.class))).thenReturn(Optional.of("valid refresh"));

        // when & then
        assertThatThrownBy(() -> reissueTokenService.reissueToken(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(REFRESH_TOKEN_NOT_FOUND.getMessage());

        verify(redisRefreshTokenRepository, never()).save(anyLong(), anyString());

    }

    @DisplayName("저장된 리프레시 토큰이 없으면 예외를 발생시킨다")
    @Test
    void reissueToken_shouldThrowException_whenNoSavedRefreshToken(){
        // given
        Long userId = 1L;

        ReissueTokenRequest request = new ReissueTokenRequest("refresh");

        when(jwtUtil.getUserId(any(String.class))).thenReturn(userId);
        when(redisRefreshTokenRepository.findByUserId(any(Long.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reissueTokenService.reissueToken(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(REFRESH_TOKEN_NOT_FOUND.getMessage());

        verify(redisRefreshTokenRepository, never()).save(anyLong(), anyString());
    }
}