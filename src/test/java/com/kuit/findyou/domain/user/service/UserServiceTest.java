package com.kuit.findyou.domain.user.service;

import com.kuit.findyou.domain.user.dto.RegisterUserRequest;
import com.kuit.findyou.domain.user.dto.RegisterUserResponse;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.domain.user.util.DefaultImageUrlProvider;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageUploader imageUploader;
    @Mock
    private DefaultImageUrlProvider defaultImageUrlProvider;
    @Mock
    private JwtUtil jwtUtil;

    @DisplayName("처음 로그인한 사용자가 회원등록을 하면 성공한다")
    @Test
    void should_succeed_When_AnyoneWhoFirstLogedInRegister(){
        // given
        final Long USER_ID = 1L;
        final String ACCESS_TOKEN = "accessToken";

        RegisterUserRequest request = getRegisterUserRequestWithoutImage();

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.empty());
        when(userRepository.findByDeviceId(request.deviceId())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(USER_ID)
                .name(request.nickname())
                .build());

        when(defaultImageUrlProvider.getImageUrl(any())).thenReturn("image-url");
        when(defaultImageUrlProvider.containsKey(any())).thenReturn(true);

        when(jwtUtil.createAccessJwt(any(), any())).thenReturn(ACCESS_TOKEN);

        // when
        RegisterUserResponse response = userService.registerUser(request);

        // then
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.nickname()).isEqualTo(request.nickname());
        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
    }

    @DisplayName("비회원이 회원등록을 하면 성공한다")
    @Test
    void should_succeed_When_GuestRegister(){
        // given
        final Long USER_ID = 1L;
        final String ACCESS_TOKEN = "accessToken";

        RegisterUserRequest request = getRegisterUserRequestWithImage();

        User user = User.builder()
                .id(USER_ID)
                .deviceId(request.deviceId())
                .build();

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.empty());
        when(userRepository.findByDeviceId(request.deviceId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(User.builder()
                .id(USER_ID)
                .name(request.nickname())
                .build());

        when(imageUploader.upload(any())).thenReturn("image-url");

        when(jwtUtil.createAccessJwt(any(), any())).thenReturn(ACCESS_TOKEN);

        // when
        RegisterUserResponse response = userService.registerUser(request);

        // then
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.nickname()).isEqualTo(request.nickname());
        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
    }

    @DisplayName("이미 가입한 회원이 회원등록을 하면 예외를 발생시킨다")
    @Test
    void should_ThrowException_When_ExistingUserRegister(){
        // given
        final Long USER_ID = 1L;

        RegisterUserRequest request = getRegisterUserRequestWithoutImage();

        User user = User.builder()
                .id(USER_ID)
                .build();

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ALREADY_SIGNED_UP_USER.getMessage());
    }

    @DisplayName("올바르지 않은 기본 프로필로 요청하면 예외가 발생한다")
    @Test
    void should_ThrowException_When_RequestContainsInvalidDefaultProfile(){
        // given
        RegisterUserRequest request = getRegisterUserRequestWithoutImage();

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.empty());
        when(defaultImageUrlProvider.containsKey(any())).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(BAD_REQUEST.getMessage());
    }

    private static RegisterUserRequest getRegisterUserRequestWithoutImage() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .profileImage(null)
                .defaultProfileImageName("default-name")
                .nickname("유저1")
                .kakaoId(1234L)
                .deviceId("1234")
                .build();
        return request;
    }

    @DisplayName("이미지 관련 내용 없이 요청하면 예외가 발생한다")
    @Test
    void should_ThrowException_When_RequestDoesNotContainProfile(){
        // given
        RegisterUserRequest request = getRegisterUserRequestWithoutProfile();

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(BAD_REQUEST.getMessage());
    }

    private static RegisterUserRequest getRegisterUserRequestWithoutProfile() {
        return RegisterUserRequest.builder()
                .profileImage(null)
                .defaultProfileImageName(null)
                .nickname("유저1")
                .kakaoId(1234L)
                .deviceId("1234")
                .build();
    }

    @DisplayName("이미지 업로드에 실패하면 요청하면 예외가 발생한다")
    @Test
    void should_ThrowException_When_ImageUploadingFailed(){
        // given
        RegisterUserRequest request = getRegisterUserRequestWithImage();

        when(userRepository.findByKakaoId(request.kakaoId())).thenReturn(Optional.empty());
        when(imageUploader.upload(any())).thenThrow(new FileUploadingFailedException("S3 업로드 실패"));

        // when
        // then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(IMAGE_UPLOAD_FAILED.getMessage());
    }

    private static RegisterUserRequest getRegisterUserRequestWithImage() {
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "test.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        RegisterUserRequest request = RegisterUserRequest.builder()
                .profileImage(profileImage)
                .defaultProfileImageName(null)
                .nickname("유저1")
                .kakaoId(1234L)
                .deviceId("1234")
                .build();
        return request;
    }
}