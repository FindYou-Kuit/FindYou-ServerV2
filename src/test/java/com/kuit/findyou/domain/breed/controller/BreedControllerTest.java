package com.kuit.findyou.domain.breed.controller;

import com.kuit.findyou.domain.breed.dto.request.ImageUrlRequestDTO;
import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import com.kuit.findyou.global.external.client.OpenAiClient;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BREED_ANALYSIS_FAILED;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class BreedControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestInitializer testInitializer;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    JwtUtil jwtUtil;

    @MockitoBean
    OpenAiClient openAiClient;

    @BeforeAll
    void setUp() {
        databaseCleaner.execute();
        testInitializer.createTestUser();
        testInitializer.createTestBreeds();
        RestAssured.port = port;
    }

    @Test
    @DisplayName("GET /api/v2/breeds - 품종 리스트 조회 성공")
    void getBreedList_success() {
        String accessToken = jwtUtil.createAccessJwt(1L, Role.USER);

        // when
        BreedListResponseDTO response =
                given()
                        .header("Authorization","Bearer " +  accessToken)
                        .when()
                        .get("/api/v2/breeds")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getObject("data", BreedListResponseDTO.class);

        // then
        assertThat(response.dogBreedList()).contains("진돗개");
        assertThat(response.catBreedList()).contains("코리안 숏헤어");
        assertThat(response.etcBreedList()).contains("기타축종");
    }

    @Test
    @DisplayName("POST /api/v2/breeds/ai-detection - 성공 응답")
    void aiDetection_success() {
        String accessToken = jwtUtil.createAccessJwt(1L, Role.USER);

        // given
        when(openAiClient.analyzeImage(eq("https://img"), anyString()))
                .thenReturn(new BreedAiDetectionResponseDTO("강아지", "포메라니안", List.of("하얀색", "갈색")));

        ImageUrlRequestDTO request = new ImageUrlRequestDTO("https://img");

        // when
        BreedAiDetectionResponseDTO response =
                given()
                        .header("Authorization", "Bearer " +accessToken)
                        .contentType(ContentType.JSON)
                        .body(request)
                        .when()
                        .post("/api/v2/breeds/ai-detection")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getObject("data", BreedAiDetectionResponseDTO.class);

        // then
        assertThat(response.species()).isEqualTo("강아지");
        assertThat(response.breed()).isEqualTo("포메라니안");
        assertThat(response.furColors()).containsExactlyInAnyOrder("하얀색", "갈색");
    }

    @Test
    @DisplayName("POST /api/v2/breeds/ai-detection - OpenAiClientException 발생 시 CustomException 매핑")
    void aiDetection_openAiClientException() {
        String accessToken = jwtUtil.createAccessJwt(1L, Role.USER);

        // given
        when(openAiClient.analyzeImage(eq("https://img"), anyString()))
                .thenThrow(new OpenAiClientException("API 호출 실패"));

        ImageUrlRequestDTO request = new ImageUrlRequestDTO("https://img");

        // when & then
        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v2/breeds/ai-detection")
        .then()
                .body("code", equalTo(BREED_ANALYSIS_FAILED.getCode()))
                .body("message", equalTo(BREED_ANALYSIS_FAILED.getMessage()));
    }


}