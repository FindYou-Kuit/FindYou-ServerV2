package com.kuit.findyou.domain.user.service.register;

import com.kuit.findyou.domain.auth.service.IssueTokenService;
import com.kuit.findyou.domain.user.dto.request.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.response.RegisterUserResponse;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {
    @InjectMocks
    private RegisterUserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IssueTokenService issueTokenService;

    @DisplayName("처음 로그인한 사용자가 회원등록을 하면 성공한다")
    @Test
    void should_Succeed_When_AnyoneWhoFirstLoggedInRegister(){
        // given
        final Long USER_ID = 1L;
        final String ACCESS_TOKEN = "accessToken";
        final String REFRESH_TOKEN = "refreshToken";

        RegisterUserRequest request = getRegisterUserRequest();

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.empty());
        when(userRepository.findByDeviceId(request.deviceId())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(USER_ID)
                .name(request.nickname())
                .build());

        when(issueTokenService.issueAccessToken(any(), any())).thenReturn(ACCESS_TOKEN);
        when(issueTokenService.issueRefreshToken(any())).thenReturn(REFRESH_TOKEN);

        // when
        RegisterUserResponse response = userService.registerUser(request);

        // then
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.nickname()).isEqualTo(request.nickname());
        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    private static RegisterUserRequest getRegisterUserRequest() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .nickname("유저1")
                .kakaoId(1234L)
                .deviceId("1234")
                .build();
        return request;
    }

    @DisplayName("비회원이 회원등록을 하면 성공한다")
    @Test
    void should_Succeed_When_GuestRegister(){
        // given
        final Long USER_ID = 1L;
        final String ACCESS_TOKEN = "accessToken";
        final String REFRESH_TOKEN = "refreshToken";

        RegisterUserRequest request = getRegisterUserRequest();

        User user = mock(User.class);

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.empty());
        when(userRepository.findByDeviceId(request.deviceId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(USER_ID)
                .name(request.nickname())
                .build());

        when(issueTokenService.issueAccessToken(any(), any())).thenReturn(ACCESS_TOKEN);
        when(issueTokenService.issueRefreshToken(any())).thenReturn(REFRESH_TOKEN);

        // when
        RegisterUserResponse response = userService.registerUser(request);

        // then
        verify(user).upgradeToMember(eq(request.kakaoId()), eq(request.nickname()));

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.nickname()).isEqualTo(request.nickname());
        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @DisplayName("이미 가입한 회원이 회원등록을 하면 예외를 발생시킨다")
    @Test
    void should_ThrowException_When_ExistingUserRegister(){
        // given
        final Long USER_ID = 1L;

        RegisterUserRequest request = getRegisterUserRequest();

        User user = User.builder()
                .id(USER_ID)
                .build();

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ALREADY_REGISTERED_USER.getMessage());
    }
}