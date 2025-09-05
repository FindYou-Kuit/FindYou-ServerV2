package com.kuit.findyou.domain.image.service;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportImageUploadServiceImplTest {
    @Mock
    ImageUploader imageUploader;

    @InjectMocks
    ReportImageUploadServiceImpl sut;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(sut, "maxFileSizeValue", "30MB");
    }


    @DisplayName("빈 files 업로드 시 빈 리스트 반환")
    @Test
    void null_or_empty_files_returns_empty_list() {
        assertThat(sut.uploadImages(null)).isEmpty();
        assertThat(sut.uploadImages(List.of())).isEmpty();
    }

    @DisplayName("최대 이미지 개수를 초과하면 IMAGE_UPLOAD_LIMIT_EXCEEDED 예외 발생")
    @Test
    void exceed_max_files_throws_IMAGE_UPLOAD_LIMIT_EXCEEDED() {
        MultipartFile f = new MockMultipartFile("files", "a.jpg", "image/jpeg", new byte[]{1});
        List<MultipartFile> files = Arrays.asList(f, f, f, f, f, f); //6장 (최대는 5장)
        assertThatThrownBy(() -> sut.uploadImages(files))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(IMAGE_UPLOAD_LIMIT_EXCEEDED.getMessage());
    }

    @DisplayName("이미지 형식이 아닌 파일(pdf 등)은 거절")
    @Test
    void reject_non_image_content_type() {
        var pdf = new MockMultipartFile("files", "a.pdf", "application/pdf", new byte[]{1});
        assertThatThrownBy(() -> sut.uploadImages(List.of(pdf)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(INVALID_IMAGE_FORMAT.getMessage());
    }

    @DisplayName("이미지와 octet-stream 형식 허용, 빈 파일은 무시")
    @Test
    void allow_image_or_octet_stream_and_skip_empty() throws Exception {
        var img = new MockMultipartFile("files", "a.jpg", "image/jpeg", new byte[]{1});
        var octet = new MockMultipartFile("files", "b.bin", "application/octet-stream", new byte[]{1});
        var empty = new MockMultipartFile("files", "c.jpg", "image/jpeg", new byte[]{}); // isEmpty=true

        when(imageUploader.upload(img)).thenReturn("https://cdn.example/a.jpg");
        when(imageUploader.upload(octet)).thenReturn("https://cdn.example/b.bin");

        var urls = sut.uploadImages(List.of(img, octet, empty));
        assertThat(urls).containsExactlyInAnyOrder(
                "https://cdn.example/a.jpg", "https://cdn.example/b.bin"
        );
    }

    @DisplayName("ImageUploader에서 예외 발생 시 IMAGE_UPLOAD_FAILED")
    @Test
    void uploader_failure_maps_to_IMAGE_UPLOAD_FAILED() throws Exception {
        var img = new MockMultipartFile("files", "a.jpg", "image/jpeg", new byte[]{1});
        when(imageUploader.upload(any(MultipartFile.class)))
                .thenThrow(new FileUploadingFailedException("boom"));

        assertThatThrownBy(() -> sut.uploadImages(List.of(img)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(IMAGE_UPLOAD_FAILED.getMessage());
    }
}
