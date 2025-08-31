package com.kuit.findyou.domain.city.controller;

import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.SIDO_NOT_FOUND;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CityControllerTest {

    @LocalServerPort
    int port;

    @Autowired DatabaseCleaner databaseCleaner;
    @Autowired TestInitializer testInitializer;
    @Autowired JwtUtil jwtUtil;

    private String accessToken;

    @BeforeAll
    void setUp() {
        databaseCleaner.execute();
        testInitializer.createTestCities();
        RestAssured.port = port;
        accessToken = jwtUtil.createAccessJwt(1L, Role.USER);
    }

    @Nested
    @DisplayName("GET /api/v2/sidos - 시도 목록 조회")
    class GetSidos {

        @Test
        @DisplayName("200 OK - 시도 목록 반환")
        void success() {
            // when
            var json =
                    given()
                            .header("Authorization", accessToken)
                            .accept(ContentType.JSON)
                            .when()
                            .get("/api/v2/sidos")
                    .then()
                            .body("data.sidoList", notNullValue())
                            .extract().jsonPath();

            // then
            // 이름 리스트만 뽑아서 검증 (DTO 구조 상관없이 안정적)
            List<String> names = json.getList("data.sidoList.name", String.class);
            assertThat(names).contains("서울특별시", "부산광역시");
        }
    }

    @Nested
    @DisplayName("GET /api/v2/sigungus - 시군구 목록 조회")
    class GetSigungus {

        @Test
        @DisplayName("200 OK - 유효한 sidoId로 시군구 목록 반환")
        void success() {
            // given:
            Long sidoId = 1L;

            // when
            var json =
                    given()
                            .header("Authorization", accessToken)
                            .accept(ContentType.JSON)
                            .queryParam("sidoId", sidoId)
                            .when()
                    .get("/api/v2/sigungus")
                            .then()
                            .body("data.sigunguList", notNullValue())
                            .extract().jsonPath();

            // then
            List<String> sigungus = json.getList("data.sigunguList", String.class);
            assertThat(sigungus).contains("강남구", "송파구");
        }

        @Test
        @DisplayName("400 Bad Request - 필수 파라미터 누락 시")
        void missingParam() {
            given()
                    .header("Authorization", accessToken)
                    .accept(ContentType.JSON)
                    .when()
            .get("/api/v2/sigungus") // sidoId 없음
                    .then()
                    .body("code", equalTo(400))
                    .body("message", equalTo("필수 요청 파라미터 'sidoId'가 누락되었습니다."));
        }

        @Test
        @DisplayName("SIDO_NOT_FOUND 매핑 - 존재하지 않는 sidoId")
        void notFoundSido() {
            given()
                    .header("Authorization", accessToken)
                    .accept(ContentType.JSON)
                    .queryParam("sidoId", 999999L)
                    .when()
                    .get("/api/v2/sigungus")
                    .then()
                    .body("code", equalTo(SIDO_NOT_FOUND.getCode()))
                    .body("message", equalTo(SIDO_NOT_FOUND.getMessage()));
        }
    }
}
