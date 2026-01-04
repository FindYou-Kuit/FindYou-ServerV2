package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.response.AdminLoginResponse;
import com.kuit.findyou.domain.auth.repository.RedisRefreshTokenRepository;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminLoginServiceImplTest {

    @InjectMocks
    AdminLoginServiceImpl adminLoginService;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    UserRepository userRepository;

    @Mock
    RedisRefreshTokenRepository redisRefreshTokenRepository;

    @DisplayName("관리자 로그인 성공 시 access/refresh 토큰을 반환하고 refresh는 관리자 TTL로 Redis 저장한다")
    @Test
    void adminLogin_shouldReturnTokens_andSaveRefreshWithAdminTtl() {
        // given
        Long adminUserId = 9999L;
        Long adminRefreshTtlMs = 31_536_000_000L; // 1년
        User user = User.builder().id(adminUserId).role(Role.USER).build();

        String accessToken = "admin access";
        String refreshToken = "admin refresh";

        ReflectionTestUtils.setField(adminLoginService, "adminUserId", adminUserId);
        ReflectionTestUtils.setField(adminLoginService, "adminRefreshTtlMs", adminRefreshTtlMs);

        when(userRepository.findById(adminUserId)).thenReturn(Optional.of(user));
        when(jwtUtil.createAccessJwt(eq(adminUserId), eq(user.getRole()))).thenReturn(accessToken);

        when(jwtUtil.createRefreshJwt(eq(adminUserId), eq(adminRefreshTtlMs))).thenReturn(refreshToken);

        // when
        AdminLoginResponse response = adminLoginService.adminLogin();

        // then
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);

        // Redis 저장도 관리자 TTL로 호출되는지 검증
        verify(redisRefreshTokenRepository, times(1)).save(eq(adminUserId), eq(refreshToken), eq(adminRefreshTtlMs));
    }

    @DisplayName("관리자 유저가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킨다")
    @Test
    void adminLogin_shouldThrowException_whenAdminUserNotFound() {
        // given
        Long adminUserId = 9999L;
        Long adminRefreshTtlMs = 31_536_000_000L;

        ReflectionTestUtils.setField(adminLoginService, "adminUserId", adminUserId);
        ReflectionTestUtils.setField(adminLoginService, "adminRefreshTtlMs", adminRefreshTtlMs);

        when(userRepository.findById(adminUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminLoginService.adminLogin())
                .isInstanceOf(CustomException.class)
                .hasMessage(USER_NOT_FOUND.getMessage());

        verify(redisRefreshTokenRepository, never()).save(anyLong(), anyString(), anyLong());
    }
}
