package com.kuit.findyou.domain.user.controller;

import com.kuit.findyou.domain.user.dto.RegisterUserResponse;
import com.kuit.findyou.domain.user.dto.RetrieveInterestAnimalsResponse;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
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

import java.time.LocalDate;

import static com.kuit.findyou.global.common.util.RestAssuredUtils.multipartText;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UserControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestInitializer testInitializer;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    User reportWriter;

    @BeforeAll
    void setUp() {
        databaseCleaner.execute();

        RestAssured.port = port;

        testInitializer.initializeControllerTestData();
        this.reportWriter = testInitializer.getReportWriter();
    }

    @Test
    @DisplayName("GET /api/v2/users/me/viewed-animals: 최근 본 글 조회 성공")
    void retrieveViewedAnimals() {
        // 작성자의 엑세스 토큰 생성
        String accessToken = jwtUtil.createAccessJwt(reportWriter.getId(), reportWriter.getRole());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
        .when()
                .get("/api/v2/users/me/viewed-animals")
        .then()
                .statusCode(200)
                .body("data.cards[0].reportId", equalTo(2))
                .body("data.cards[0].thumbnailImageUrl", equalTo("https://img.com/missing.png"))
                .body("data.cards[0].title", equalTo("포메라니안"))
                .body("data.cards[0].tag", equalTo("실종신고"))
                .body("data.cards[0].date", equalTo("2024-10-05"))
                .body("data.cards[0].location", equalTo("서울시 강남구"))
                .body("data.cards[0].interest", equalTo(true))
                .body("data.cards[1].reportId", equalTo(1))
                .body("data.cards[1].thumbnailImageUrl", equalTo("https://img.com/1.png"))
                .body("data.cards[1].title", equalTo("믹스견"))
                .body("data.cards[1].tag", equalTo("보호중"))
                .body("data.cards[1].date", equalTo(LocalDate.now().toString()))
                .body("data.cards[1].location", equalTo("서울"))
                .body("data.cards[1].interest", equalTo(true))
                .body("data.lastId", equalTo(1))
                .body("data.isLast", equalTo(true));
    }

    @DisplayName("POST /api/v2/users : 처음 로그인한 사람이 회원가입 성공한다")
    @Test
    void should_Succeed_When_registerAnyoneWhoFirstLoggedIn(){
        // given
        final String NICKNAME = "유저1";

        // when
        RegisterUserResponse response = given()
//                    .log().all()
                    .contentType(ContentType.MULTIPART)
                    .multiPart(multipartText("defaultProfileImageName", "default"))
                    .multiPart(multipartText("nickname", NICKNAME))
                    .multiPart(multipartText("kakaoId", "123456"))
                    .multiPart(multipartText("deviceId", "device-001"))
                .when()
                    .post("/api/v2/users")
                .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getObject("data", RegisterUserResponse.class);

        // then
        Role role = jwtUtil.getRole(response.accessToken());

        assertThat(response.nickname()).isEqualTo(NICKNAME);
        assertThat(role).isEqualTo(Role.USER);
    }

    @DisplayName("GET /api/v2/users/me/interest-animals : 유저가 관심동물을 가지고 있으면 반환한다")
    @Test
    void should_ReturnInterestAnimals_When_UserHasInterestAnimals(){
        // given
        String accessToken = jwtUtil.createAccessJwt(reportWriter.getId(), reportWriter.getRole());

        // when
        RetrieveInterestAnimalsResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
                .when()
                .get("/api/v2/users/me/interest-animals")
                .then()
                .extract()
                .jsonPath()
                .getObject("data", RetrieveInterestAnimalsResponse.class);

        // then
        assertThat(response.interestAnimals()).hasSize(3);
        assertThat(response.isLast()).isTrue();
    }

    @DisplayName("GET /api/v2/users/me/interest-animals : 유저가 관심동물을 가지고 있지 않으면 빈 리스트를 반환한다")
    @Test
    void should_ReturnEmptyList_When_UserHasNoInterestAnimal(){
        // given
        User user = createUser();
        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        RetrieveInterestAnimalsResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
                .when()
                .get("/api/v2/users/me/interest-animals")
                .then()
                .extract()
                .jsonPath()
                .getObject("data", RetrieveInterestAnimalsResponse.class);

        // then
        assertThat(response.interestAnimals()).hasSize(0);
        assertThat(response.lastId()).isEqualTo(-1L);
        assertThat(response.isLast()).isTrue();
    }

    private User createUser() {
        User user = User.builder()
                .name("유저")
                .kakaoId(1234L)
                .role(Role.USER)
                .deviceId("asdf1234asdf")
                .profileImageUrl("image.png")
                .build();
        return userRepository.save(user);
    }
}