package com.kuit.findyou.domain.auth.service;

import com.kuit.findyou.domain.auth.dto.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.KakaoLoginResponse;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
}