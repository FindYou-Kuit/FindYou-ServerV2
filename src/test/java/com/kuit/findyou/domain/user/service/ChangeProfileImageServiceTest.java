package com.kuit.findyou.domain.user.service;

import com.kuit.findyou.domain.user.dto.request.ChangeProfileImageRequest;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.domain.user.service.change_profileImage.ChangeProfileImageServiceImpl;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.IMAGE_UPLOAD_FAILED;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChangeProfileImageServiceTest {
    @InjectMocks
    ChangeProfileImageServiceImpl service;
    @Mock
    UserRepository userRepository;
    @Mock
    ImageUploader imageUploader;

    @Test
    @DisplayName("기본 프로필(enum)로 변경 성공")
    void changeToDefaultProfileImage_Success() {
        // given
        User user = User.builder()
                .id(1L).name("유저").role(Role.USER).deviceId("1234").build();

        when(userRepository.getReferenceById(1L)).thenReturn(user);

        ChangeProfileImageRequest req = new ChangeProfileImageRequest(null, "chick");

        // when
        service.changeProfileImage(1L, req);

        // then
        assertThat(user.getProfileImageUrl()).isEqualTo("chick");
    }

    @Test
    @DisplayName("파일 업로드로 변경 성공")
    void changeToUploadedImage_Success() {
        // given
        User user = User.builder()
                .id(1L).name("유저").role(Role.USER).deviceId("dev").build();

        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(imageUploader.upload(any())).thenReturn("https://cdn.test/uploaded.jpg");

        MockMultipartFile file = new MockMultipartFile(
                "profileImageFile", "p.jpg", "image/jpeg", "x".getBytes());
        ChangeProfileImageRequest req = new ChangeProfileImageRequest(file, null);

        // when
        service.changeProfileImage(1L, req);

        // then
        assertThat(user.getProfileImageUrl()).isEqualTo("https://cdn.test/uploaded.jpg");
    }

    @Test
    @DisplayName("존재하지 않는 사용자이면 USER_NOT_FOUND")
    void userNotFound_Throws() {
        when(userRepository.getReferenceById(99L)).thenThrow(new EntityNotFoundException());
        ChangeProfileImageRequest req = new ChangeProfileImageRequest(null, "puppy");

        assertThatThrownBy(() -> service.changeProfileImage(99L, req))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미지 업로드 실패 시 IMAGE_UPLOAD_FAILED")
    void uploadFailed_Throws() {
        User user = User.builder()
                .id(1L).name("유저").role(Role.USER).deviceId("1234").build();
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(imageUploader.upload(any())).thenThrow(new FileUploadingFailedException("S3 업로드 실패"));

        MockMultipartFile file = new MockMultipartFile(
                "profileImageFile", "p.jpg", "image/jpeg", "x".getBytes());
        ChangeProfileImageRequest req = new ChangeProfileImageRequest(file, null);

        assertThatThrownBy(() -> service.changeProfileImage(1L, req))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(IMAGE_UPLOAD_FAILED.getMessage());
    }
}
