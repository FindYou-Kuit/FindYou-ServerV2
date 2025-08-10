package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.GuestLoginRequest;
import com.kuit.findyou.domain.auth.dto.GuestLoginResponse;
import com.kuit.findyou.domain.auth.dto.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.KakaoLoginResponse;
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

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.GUEST_LOGIN_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @InjectMocks
    private AuthServiceImpl authService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    @Test
    void should_ReturnfirstLoginWithTrue_When_UserWithKakaoIdNotFound(){
        // given
        final Long KAKAO_ID = 1234L;
        when(userRepository.findByKakaoId(any())).thenReturn(Optional.empty());

        // when
        KakaoLoginResponse response = authService.kakaoLogin(new KakaoLoginRequest(KAKAO_ID));

        // then
        assertThat(response.isFirstLogin()).isTrue();
        assertThat(response.userInfo()).isNull();
    }

    @Test
    void should_ReturnUserInfo_When_UserWithKakaoIdExists(){
        // given
        final Long KAKAO_ID = 1234L;
        final String ACCESS_TOKEN = "accessToken";
        final String NAME = "유저";

        User user = mockUser(NAME, Role.USER, KAKAO_ID);
        when(userRepository.findByKakaoId(KAKAO_ID)).thenReturn(Optional.of(user));
        when(jwtUtil.createAccessJwt(user.getId(), user.getRole())).thenReturn(ACCESS_TOKEN);

        // when
        KakaoLoginResponse response = authService.kakaoLogin(new KakaoLoginRequest(KAKAO_ID));

        // then
        assertThat(response.isFirstLogin()).isFalse();
        assertThat(response.userInfo()).isNotNull();
        assertThat(response.userInfo().userId()).isEqualTo(user.getId());
        assertThat(response.userInfo().accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.userInfo().nickname()).isEqualTo(NAME);
    }

    private User mockUser(String name, Role role, Long kakaoId){
        User build = User.builder()
                .id(1L)
                .name(name)
                .role(role)
                .kakaoId(kakaoId)
                .build();

        return build;
    }

    @DisplayName("게스트가 로그인하면 새로 유저를 추가하지 않는다")
    @Test()
    void should_DoesNotSaveNewGuest_When_UserWithDeviceIdExists(){
        // given
        final String deviceId = "asdf-1234-asdf";
        final String accessToken = "accessToken";

        User user = mockUser("게스트", Role.GUEST, null);
        when(userRepository.findByDeviceId(eq(deviceId))).thenReturn(Optional.of(user));
        when(jwtUtil.createAccessJwt(user.getId(), user.getRole())).thenReturn(accessToken);

        // when
        GuestLoginResponse response = authService.guestLogin(new GuestLoginRequest(deviceId));

        // then
        verify(userRepository, never()).save(any());
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.accessToken()).isEqualTo(accessToken);
    }

    @DisplayName("디바이스 id가 일치하는 유저가 없으면 새로 게스트를 저장한다")
    @Test()
    void should_SaveNewGuest_When_UserWithDeviceIdDoesNotExists(){
        // given
        final String deviceId = "asdf-1234-asdf";
        final String accessToken = "accessToken";

        User user = mockUser("게스트", Role.GUEST, null);
        when(userRepository.findByDeviceId(eq(deviceId))).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);
        when(jwtUtil.createAccessJwt(user.getId(), user.getRole())).thenReturn(accessToken);

        // when
        GuestLoginResponse response = authService.guestLogin(new GuestLoginRequest(deviceId));

        // then
        verify(userRepository).save(any(User.class));
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.accessToken()).isEqualTo(accessToken);
    }

    @DisplayName("게스트가 아니면 예외가 발생한다.")
    @Test()
    void should_ThrowException_When_NonGuestUserLogsIn(){
        // given
        final String deviceId = "asdf-1234-asdf";
        final String accessToken = "accessToken";

        User user = mockUser("게스트", Role.USER, null);
        when(userRepository.findByDeviceId(eq(deviceId))).thenReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> authService.guestLogin(new GuestLoginRequest(deviceId)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GUEST_LOGIN_FAILED.getMessage());
        verify(userRepository, never()).save(any());
    }
}