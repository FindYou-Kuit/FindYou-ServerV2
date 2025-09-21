package com.kuit.findyou.domain.home.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
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
    private RedisConnectionFactory redisConnectionFactory;

    static private WireMockServer wireMockServer;

    static {
        // 컨텍스트 로딩 전에 실행되도록 static 초기화 블록 사용
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

    }

    @Autowired
    private JwtUtil jwtUtil;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // 외부 API url 덮어 쓰기
        String baseUrl = "http://localhost:" + wireMockServer.port();

        registry.add("openapi.protecting-animal.api-url", () -> baseUrl + "/abandonmentPublicService_v2");
        registry.add("openapi.rescue-animal-stats.api-url", () -> baseUrl + "/rescueAnimalStatsService/rescueAnimalStats");
        registry.add("openapi.loss-animal-info.api-url", () -> baseUrl + "/lossInfoService/lossInfo");
    }

    @BeforeAll
    void setUp() {
        databaseCleaner.execute();

        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetMocks() {
        // 이전 stub 제거
        WireMock.reset();

        // 레디스 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @DisplayName("요청의 위도 경도가 올바르면 홈화면 조회에 성공한다")
    @Test
    void should_Succeed_When_RequestWithCorrectCoordinate(){
        // given
        final Double lat = 33.0;
        final Double lng = 127.0;
        final int protectingAnimalCount = 200;
        final int rescuedAnimalCount = 125;
        final int adoptedAnimalCount = 30;
        final int lostAnimalCount = 128;

        mockAbandonmentPublicServer(rescuedAnimalCount);     // 외부 API 모킹
        mockRescueAnimalStatsServer(protectingAnimalCount, adoptedAnimalCount);
        mockLossInfoServer(lostAnimalCount);

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
        assertThat(response.statistics().recent1Year().lostAnimalCount()).isEqualTo(String.valueOf(lostAnimalCount));
        assertThat(response.statistics().recent1Year().protectingAnimalCount()).isEqualTo(String.valueOf(protectingAnimalCount));
        assertThat(response.statistics().recent1Year().adoptedAnimalCount()).isEqualTo(String.valueOf(adoptedAnimalCount));
        assertThat(response.statistics().recent1Year().rescuedAnimalCount()).isEqualTo(String.valueOf(rescuedAnimalCount));
        assertThat(response.protectingAnimals()).hasSize(10);
        assertThat(response.witnessedOrMissingAnimals()).hasSize(10);
    }

    private void mockLossInfoServer(int totalCount) {
        String body = String.format("""
        {
          "response": {
            "header": {
              "reqNo": 11929220,
              "resultCode": "00",
              "resultMsg": "NORMAL SERVICE."
            },
            "body": {
              "items": {
                "item": []
              },
              "numOfRows": 0,
              "pageNo": 1,
              "totalCount": %d
            }
          }
        }
        """, totalCount);

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/lossInfoService/lossInfo"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private void mockRescueAnimalStatsServer(int rescuedAnimalCount, int adoptedAnimalCount) {
        String body = String.format("""
                               {
                                        "response": {
                                          "header": {
                                            "reqNo": 11929129,
                                            "resultCode": "00",
                                            "resultMsg": "NORMAL SERVICE."
                                          },
                                          "body": {
                                            "items": {
                                              "item": [
                                                {
                                                  "se": "chart1",
                                                  "rgn": "전체 지역",
                                                  "prcsNm": "보호중",
                                                  "tot": %d
                                                },
                                                {
                                                  "se": "chart1",
                                                  "rgn": "전체 지역",
                                                  "prcsNm": "입양",
                                                  "tot": %d
                                                }
                                              ]
                                            },
                                            "numOfRows": 2,
                                            "pageNo": 1,
                                            "totalCount": 23
                                          }
                                        }
                                      }
                         """, rescuedAnimalCount, adoptedAnimalCount);

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/rescueAnimalStatsService/rescueAnimalStats"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private static void mockAbandonmentPublicServer(int protectingAnimalCount) {
        String body = String.format("""
                                    {
                                      "response": {
                                        "header": {
                                          "reqNo": 11924386,
                                          "resultCode": "00",
                                          "resultMsg": "NORMAL SERVICE."
                                        },
                                        "body": {
                                          "items": {
                                            "item": [
                                              {
                                                "desertionNo": "441399202501796",
                                                "happenDt": "20250803",
                                                "happenPlace": "남양주시 진건 119 안전센터",
                                                "kindFullNm": "[개] 믹스견",
                                                "upKindCd": "417000",
                                                "upKindNm": "개",
                                                "kindCd": "000114",
                                                "kindNm": "믹스견",
                                                "colorCd": "흰색",
                                                "age": "2021(년생)",
                                                "weight": "15(Kg)",
                                                "noticeNo": "경기-남양주-2025-00484",
                                                "noticeSdt": "20250803",
                                                "noticeEdt": "20250813",
                                                "popfile1": "http://openapi.animal.go.kr/openapi/service/rest/fileDownloadSrvc/files/shelter/2025/07/202508031008157.jpg",
                                                "popfile2": "http://openapi.animal.go.kr/openapi/service/rest/fileDownloadSrvc/files/shelter/2025/07/202508031008225.jpg",
                                                "processState": "보호중",
                                                "sexCd": "M",
                                                "neuterYn": "N",
                                                "specialMark": "경계가 있으며 공격성이 보임",
                                                "careRegNo": "341399202200002",
                                                "careNm": "남양주시동물보호센터",
                                                "careTel": "031-579-3604",
                                                "careAddr": "경기도 남양주시 경강로163번길 32-27 (이패동) ",
                                                "careOwnerNm": "최성용",
                                                "orgNm": "경기도 남양주시",
                                                "vaccinationChk": "광견병,종합백신,코로나",
                                                "healthChk": "사상충,파보,코로나,홍역",
                                                "updTm": "2025-08-03 10:39:18.0"
                                              }
                                            ]
                                          },
                                          "numOfRows": 1,
                                          "pageNo": 1,
                                          "totalCount": %d
                                        }
                                      }
                                    }
                        """, protectingAnimalCount);

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/abandonmentPublicService_v2/abandonmentPublic_v2"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    @DisplayName("요청에 위도 경도가 없어도 홈화면 조회에 성공한다")
    @Test
    void should_Succeed_When_RequestWithoutCoordinate(){
        // given
        final int protectingAnimalCount = 200;
        final int rescuedAnimalCount = 125;
        final int adoptedAnimalCount = 30;
        final int lostAnimalCount = 128;

        mockAbandonmentPublicServer(rescuedAnimalCount); // 외부 API 모킹
        mockRescueAnimalStatsServer(protectingAnimalCount, adoptedAnimalCount);
        mockLossInfoServer(lostAnimalCount);

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

    @DisplayName("외부 서버가 응답하지 않으면 홈화면 조회에서 빈 통계를 반환한다")
    @Test
    void should_ReturnEmptyStatistics_When_ExternalServerDoesNotReply(){
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
    void should_ReturnBadRequestResponse_When_RequestIncludesOnlyLatitudeOrLongitude(){
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
    void should_ReturnBadRequestResponse_When_LatitudeOrLongitudeIsInvalid(){
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