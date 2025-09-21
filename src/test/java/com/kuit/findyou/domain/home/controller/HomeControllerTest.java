package com.kuit.findyou.domain.home.controller;

import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.domain.home.service.CacheHomeStatsService;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import com.kuit.findyou.global.config.RedisTestContainersConfig;
import com.kuit.findyou.global.config.TestDatabaseConfig;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.IntStream;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Import({RedisTestContainersConfig.class, TestDatabaseConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class HomeControllerTest {
    @LocalServerPort
    int port;

    @Autowired
    private TestInitializer testInitializer;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CacheHomeStatsService cacheHomeStatsService;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeAll
    void setUp() {
        databaseCleaner.execute();

        RestAssured.port = port;
    }

    @BeforeEach
    void resetMocks() {
        // 레디스 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @DisplayName("요청의 위도 경도가 올바르면 홈화면 조회에 성공한다")
    @Test
    void should_Succeed_When_RequestWithCoordinate(){
        // given
        final Double lat = 33.0;
        final Double lng = 127.0;
        final String protectingAnimalCount = "200";
        final String rescuedAnimalCount = "125";
        final String adoptedAnimalCount = "30";
        final String lostAnimalCount = "128";

        // 레디스에 저장
        GetHomeResponse.Statistics stats = new GetHomeResponse.Statistics(rescuedAnimalCount, protectingAnimalCount, adoptedAnimalCount, lostAnimalCount);
        GetHomeResponse.TotalStatistics totalStats = new GetHomeResponse.TotalStatistics(stats, stats, stats);
        cacheHomeStatsService.cacheTotalStatistics(totalStats);

        User testUser = testInitializer.createTestUser();
        IntStream.rangeClosed(1, 20).forEach(i -> {
            testInitializer.createTestProtectingReportWithImage(testUser);
        });
        IntStream.rangeClosed(1, 10).forEach(i -> {
            testInitializer.createTestMissingReportWithImage(testUser);
            testInitializer.createTestWitnessReportWithImage(testUser);
        });

        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());


        // when
        GetHomeResponse response = given()
                .header("Authorization","Bearer " + accessToken)
                .queryParam("lat", lat)
                .queryParam("lng", lng)
                .when()
                .get("api/v2/home")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GetHomeResponse.class);

        // then
        assertThat(response.statistics().recent1Year().lostAnimalCount()).isEqualTo(lostAnimalCount);
        assertThat(response.statistics().recent1Year().protectingAnimalCount()).isEqualTo(protectingAnimalCount);
        assertThat(response.statistics().recent1Year().adoptedAnimalCount()).isEqualTo(adoptedAnimalCount);
        assertThat(response.statistics().recent1Year().rescuedAnimalCount()).isEqualTo(rescuedAnimalCount);
        assertThat(response.protectingAnimals()).hasSize(10);
        assertThat(response.witnessedOrMissingAnimals()).hasSize(10);
    }

    @DisplayName("요청에 위도 경도가 없어도 홈화면 조회에 성공한다")
    @Test
    void should_Succeed_When_RequestWithoutCoordinate(){
        // given
        final String protectingAnimalCount = "200";
        final String rescuedAnimalCount = "125";
        final String adoptedAnimalCount = "30";
        final String lostAnimalCount = "128";

        // 레디스에 저장
        GetHomeResponse.Statistics stats = new GetHomeResponse.Statistics(rescuedAnimalCount, protectingAnimalCount, adoptedAnimalCount, lostAnimalCount);
        GetHomeResponse.TotalStatistics totalStats = new GetHomeResponse.TotalStatistics(stats, stats, stats);
        cacheHomeStatsService.cacheTotalStatistics(totalStats);

        User testUser = testInitializer.createTestUser();
        IntStream.rangeClosed(1, 20).forEach(i -> {
            testInitializer.createTestProtectingReportWithImage(testUser);
        });
        IntStream.rangeClosed(1, 10).forEach(i -> {
            testInitializer.createTestMissingReportWithImage(testUser);
            testInitializer.createTestWitnessReportWithImage(testUser);
        });

        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());


        // when
        GetHomeResponse response = given()
                .header("Authorization","Bearer " + accessToken)
                .when()
                .get("api/v2/home")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GetHomeResponse.class);

        // then
        assertThat(response.statistics().recent1Year().lostAnimalCount()).isEqualTo(String.valueOf(lostAnimalCount));
        assertThat(response.statistics().recent1Year().protectingAnimalCount()).isEqualTo(String.valueOf(protectingAnimalCount));
        assertThat(response.statistics().recent1Year().adoptedAnimalCount()).isEqualTo(String.valueOf(adoptedAnimalCount));
        assertThat(response.statistics().recent1Year().rescuedAnimalCount()).isEqualTo(String.valueOf(rescuedAnimalCount));
        assertThat(response.protectingAnimals()).hasSize(10);
        assertThat(response.witnessedOrMissingAnimals()).hasSize(10);
    }

    @DisplayName("캐시에 통계가 없으면 홈화면 조회에서 빈 통계를 반환한다")
    @Test
    void should_ReturnEmptyStats_When_NoCachedStatsExists(){
        // given
        final String unknown = "-";

        User testUser = testInitializer.createTestUser();
        IntStream.rangeClosed(1, 20).forEach(i -> {
            testInitializer.createTestProtectingReportWithImage(testUser);
        });
        IntStream.rangeClosed(1, 10).forEach(i -> {
            testInitializer.createTestMissingReportWithImage(testUser);
            testInitializer.createTestWitnessReportWithImage(testUser);
        });

        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());

        // when
        GetHomeResponse response = given()
                .header("Authorization","Bearer " + accessToken)
                .when()
                .get("api/v2/home")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GetHomeResponse.class);

        // then
        assertThat(response.statistics().recent1Year().lostAnimalCount()).isEqualTo(unknown);
        assertThat(response.statistics().recent1Year().protectingAnimalCount()).isEqualTo(unknown);
        assertThat(response.statistics().recent1Year().adoptedAnimalCount()).isEqualTo(unknown);
        assertThat(response.statistics().recent1Year().rescuedAnimalCount()).isEqualTo(unknown);
        assertThat(response.protectingAnimals()).hasSize(10);
        assertThat(response.witnessedOrMissingAnimals()).hasSize(10);
    }

    @DisplayName("위도 경도 중 하나만 요청에 포함하면 요청에 실패한다")
    @Test
    void should_RespondBadRequest_When_CoordinateIncludesOnlyLatOrLng(){
        // given

        User testUser = testInitializer.createTestUser();
        double lat = 33.0;

        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());

        // when & then
        given()
                .header("Authorization","Bearer " + accessToken)
                .queryParam("lat", lat)
                .when()
                .get("api/v2/home")
                .then()
                .statusCode(200)
                .body("code", equalTo(BAD_REQUEST.getCode()))
                .body("message", equalTo(BAD_REQUEST.getMessage()));
    }

    @DisplayName("위도 경도가 올바르지 않으면 요청에 실패한다")
    @Test
    void should_RespondBadRequest_When_LatOrLngIsInvalid(){
        // given

        User testUser = testInitializer.createTestUser();
        double lat = 100.0;
        double lng = 190.0;

        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());

        // when & then
        given()
                .header("Authorization","Bearer " + accessToken)
                .queryParam("lat", lat)
                .queryParam("lng", lng)
                .when()
                .get("api/v2/home")
                .then()
                .statusCode(200)
                .body("code", equalTo(BAD_REQUEST.getCode()))
                .body("message", equalTo(BAD_REQUEST.getMessage()));
    }
}