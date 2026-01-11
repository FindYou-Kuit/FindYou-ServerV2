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

    @DisplayName("관리자 로그인 성공 시 access 토큰을 반환한다")
    @Test
    void adminLogin_shouldReturnAccessToken() {
        // given
        Long adminUserId = 9999L;
        Long adminAccessTtlMs = 5_184_000_000L; // 60일
        User admin = User.builder()
                .id(adminUserId)
                .role(Role.ADMIN)
                .build();

        String accessToken = "admin access";

        ReflectionTestUtils.setField(adminLoginService, "adminUserId", adminUserId);
        ReflectionTestUtils.setField(adminLoginService, "adminAccessTtlMs", adminAccessTtlMs);

        when(userRepository.findById(adminUserId)).thenReturn(Optional.of(admin));
        when(jwtUtil.createAccessJwt(
                eq(adminUserId),
                eq(Role.ADMIN),
                eq(adminAccessTtlMs)
        )).thenReturn(accessToken);

        // when
        AdminLoginResponse response = adminLoginService.adminLogin();

        // then
        assertThat(response.userId()).isEqualTo(adminUserId);
        assertThat(response.accessToken()).isEqualTo(accessToken);

        verify(jwtUtil).createAccessJwt(
                eq(adminUserId),
                eq(Role.ADMIN),
                eq(adminAccessTtlMs)
        );
    }

    @DisplayName("관리자 유저가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킨다")
    @Test
    void adminLogin_shouldThrowException_whenAdminUserNotFound() {
        // given
        Long adminUserId = 9999L;
        ReflectionTestUtils.setField(adminLoginService, "adminUserId", adminUserId);

        when(userRepository.findById(adminUserId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminLoginService.adminLogin())
                .isInstanceOf(CustomException.class)
                .hasMessage(USER_NOT_FOUND.getMessage());
    }
}
