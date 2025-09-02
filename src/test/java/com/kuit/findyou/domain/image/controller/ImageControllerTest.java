package com.kuit.findyou.domain.image.controller;

import com.kuit.findyou.domain.image.dto.ReportImageResponse;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import com.kuit.findyou.global.infrastructure.FileUploadingFailedException;
import com.kuit.findyou.global.infrastructure.ImageUploader;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ImageControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    TestInitializer testInitializer;

    @Autowired
    JwtUtil jwtUtil;

    @MockitoBean
    private ImageUploader imageUploader;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        RestAssured.port = port;
    }
    @Test
    @DisplayName("여러 개의 이미지를 업로드하고 CDN URL 목록을 반환")
    void uploadImages_Success() throws Exception {
        User testUser = testInitializer.createTestUser();
        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());

        MockMultipartFile file1 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1-content".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file2 = new MockMultipartFile("files", "image2.png", MediaType.IMAGE_PNG_VALUE, "image2-content".getBytes(StandardCharsets.UTF_8));

        when(imageUploader.upload(any(MultipartFile.class)))
                .thenReturn("https://cdn.findyou.com/image1.jpg", "https://cdn.findyou.com/image2.png");

        BaseResponse<ReportImageResponse> response =
        given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("files", file1.getOriginalFilename(), file1.getBytes(), file1.getContentType())
                .multiPart("files", file2.getOriginalFilename(), file2.getBytes(), file2.getContentType())
        .when()
                .post("/api/v2/reports/images/upload")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body()
                .as(new TypeRef<>() {});

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().urls()).hasSize(2)
                .containsExactly("https://cdn.findyou.com/image1.jpg", "https://cdn.findyou.com/image2.png");
    }


    @Test
    @DisplayName("파일이 첨부되지 않았을 때 에러가 아닌 200 OK와 빈 배열을 반환")
    void uploadImages_NoFiles_ReturnsEmptyList() {
        User testUser = testInitializer.createTestUser();
        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());

        BaseResponse<ReportImageResponse> response =
            given()
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .multiPart("non-file", "non-file")
            .when()
                    .post("/api/v2/reports/images/upload")
            .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract()
                    .body()
                    .as(new TypeRef<>() {});

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().urls()).isEmpty();
    }

    @Test
    @DisplayName("최대 이미지 개수(5개)를 초과하면 400 에러를 반환")
    void uploadImages_ExceedsLimit_ThrowsException() throws IOException {
        User testUser = testInitializer.createTestUser();
        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());
        MockMultipartFile file = new MockMultipartFile("files", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("files", "file1.jpg", file.getBytes(), file.getContentType())
                .multiPart("files", "file2.jpg", file.getBytes(), file.getContentType())
                .multiPart("files", "file3.jpg", file.getBytes(), file.getContentType())
                .multiPart("files", "file4.jpg", file.getBytes(), file.getContentType())
                .multiPart("files", "file5.jpg", file.getBytes(), file.getContentType())
                .multiPart("files", "file6.jpg", file.getBytes(), file.getContentType()) // 6번째 파일
        .when()
                .post("/api/v2/reports/images/upload")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("success", equalTo(false))
                .body("code", equalTo(IMAGE_UPLOAD_LIMIT_EXCEEDED.getCode()));
    }

    @Test
    @DisplayName("이미지 형식이 아닌 파일을 업로드하면 400 에러를 반환한다")
    void uploadImages_InvalidFormat_ThrowsException() throws IOException {
        User testUser = testInitializer.createTestUser();
        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());
        MockMultipartFile pdfFile = new MockMultipartFile("files", "document.pdf", "application/pdf", "pdf-content".getBytes());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("files", pdfFile.getOriginalFilename(), pdfFile.getBytes(), pdfFile.getContentType())
        .when()
                .post("/api/v2/reports/images/upload")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("success", equalTo(false))
                .body("code", equalTo(INVALID_IMAGE_FORMAT.getCode()));
    }

    @Test
    @DisplayName("업로더(S3) 오류 발생 시 500 에러를 반환한다")
    void uploadImages_UploaderFails_ThrowsException() throws Exception {
        User testUser = testInitializer.createTestUser();
        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());
        MockMultipartFile file = new MockMultipartFile("files", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

        when(imageUploader.upload(any(MultipartFile.class)))
                .thenThrow(new FileUploadingFailedException("S3 upload failed"));

        given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("files", file.getOriginalFilename(), file.getBytes(), file.getContentType())
        .when()
                .post("/api/v2/reports/images/upload")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .body("success", equalTo(false))
                .body("code", equalTo(IMAGE_UPLOAD_FAILED.getCode()));
    }
}

