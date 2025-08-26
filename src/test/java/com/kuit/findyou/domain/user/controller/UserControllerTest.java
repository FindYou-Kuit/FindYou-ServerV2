package com.kuit.findyou.domain.user.controller;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.model.WitnessReport;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.user.dto.request.AddInterestAnimalRequest;
import com.kuit.findyou.domain.user.dto.GetUserProfileResponse;
import com.kuit.findyou.domain.user.dto.response.RegisterUserResponse;
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
import java.util.Map;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.DUPLICATE_INTEREST_REPORT;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.SUCCESS;
import static com.kuit.findyou.global.common.util.RestAssuredUtils.multipartText;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

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

    @Autowired
    InterestReportRepository interestReportRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        RestAssured.port = port;
    }

    @Test
    @DisplayName("GET /api/v2/users/me/viewed-animals: 최근 본 글 조회 성공")
    void retrieveViewedAnimals() {
        // 작성자의 엑세스 토큰 생성
        User reportWriter = testInitializer.userWith3InterestReportsAnd2ViewedReports();

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

    @DisplayName("POST /api/v2/users : 처음 로그인한 사람이 회원가입에 성공한다")
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
        User user = testInitializer.userWith3InterestAnimals();

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        CardResponseDTO response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
                .when()
                .get("/api/v2/users/me/interest-animals")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", CardResponseDTO.class);

        // then
        assertThat(response.cards()).hasSize(3);
        assertThat(response.isLast()).isTrue();
        assertThat(response.cards()).allSatisfy(card -> {
             assertThat(card.interest()).isTrue();
        });
    }

    @DisplayName("GET /api/v2/users/me/interest-animals : 유저가 관심동물을 가지고 있지 않으면 빈 리스트를 반환한다")
    @Test
    void should_ReturnEmptyList_When_UserHasNoInterestAnimal(){
        // given
        User user = testInitializer.createTestUser();

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        CardResponseDTO response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
                .when()
                .get("/api/v2/users/me/interest-animals")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", CardResponseDTO.class);

        // then
        assertThat(response.cards()).hasSize(0);
        assertThat(response.lastId()).isEqualTo(-1L);
        assertThat(response.isLast()).isTrue();
    }

    @DisplayName("DELETE /api/v2/users/me : 회원 탈퇴에 성공한다.")
    @Test
    void should_DeleteUser(){
        // given
        User testUser = testInitializer.createTestUser();

        String accessToken = jwtUtil.createAccessJwt(testUser.getId(), testUser.getRole());

        // when: 회원 탈퇴 요청
        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v2/users/me")
                .then()
                .statusCode(200);

        // then
        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

    @DisplayName("PATCH /api/v2/users/me/nickname : 유저의 닉네임을 수정한다")
    @Test
    void shouldChangeNickname_whenValidNicknameProvided() {
        // given
        User user = testInitializer.createTestUser();

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(Map.of("newNickname", "찾아유"))
                .when()
                .patch("/api/v2/users/me/nickname")
                .then()
                .statusCode(200)
                .body("data", nullValue());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("찾아유");
    }

    @Test
    @DisplayName("닉네임이 정확히 8글자인 경우 닉네임 수정에 성공한다.")
    void shouldChangeNickname_whenLengthIsExactly8() {
        // given
        User user = testInitializer.createTestUser();
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(Map.of("newNickname", "찾아유찾아유찾아"))
                .when()
                .patch("/api/v2/users/me/nickname")
                .then()
                .statusCode(200)
                .body("data", nullValue());
    }

    @Test
    @DisplayName("닉네임 필드가 누락되면 400과 에러 메시지를 반환한다")
    void shouldReturn400_whenNewNicknameFieldMissing() {
        // given
        User user = testInitializer.createTestUser();
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(Map.of()) // {} : 본문은 있지만 newNickname 필드 누락
                .when()
                .patch("/api/v2/users/me/nickname")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("success", equalTo(false))
                .body("code", equalTo(400))
                .body("message", equalTo("닉네임이 비어있어요."));
    }

    @Test
    @DisplayName("닉네임이 빈 문자열이면 400과 에러 메시지를 반환한다")
    void shouldReturn400_whenNewNicknameIsEmpty() {
        // given
        User user = testInitializer.createTestUser();
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(Map.of("newNickname", "")) // 빈 문자열
                .when()
                .patch("/api/v2/users/me/nickname")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("success", equalTo(false))
                .body("code", equalTo(400))
                .body("message", equalTo("닉네임이 비어있어요."));
    }

    @Test
    @DisplayName("닉네임이 공백만으로 이루어지면 400과 에러 메시지를 반환한다")
    void shouldReturn400_whenNewNicknameIsBlank() {
        // given
        User user = testInitializer.createTestUser();
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(Map.of("newNickname", "   ")) // 공백만
                .when()
                .patch("/api/v2/users/me/nickname")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo(400))
                .body("message", equalTo("닉네임이 비어있어요."));
    }

    @Test
    @DisplayName("닉네임이 8글자를 넘어가면 400과 에러 메시지를 반환한다")
    void shouldReturn400_WhenLengthExceeds8() {
        // given
        User user = testInitializer.createTestUser();
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(Map.of("newNickname", "찾아유찾아유찾아유찾아유"))
                .when()
                .patch("/api/v2/users/me/nickname")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo(400))
                .body("message", equalTo("닉네임은 최대 8글자까지만 가능해요."));
    }

    @Test
    @DisplayName("닉네임에 특수문자가 포함될 시 400과 에러 메시지를 반환한다")
    void shouldReturnBadRequest_WhenPatternDoesNotMatch() {
        // given
        User user = testInitializer.createTestUser();
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(Map.of("newNickname", "찾아유@12"))
                .when()
                .patch("/api/v2/users/me/nickname")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo(400))
                .body("message", equalTo("특수문자는 들어갈 수 없어요."));
    }

    @Test
    @DisplayName("새로운 관심동물을 등록하면 성공한다")
    void shouldSucceed_WhenNewInterestAnimalIsAdded() {
        // given
        User user = testInitializer.createTestUser();
        User reportWriter = testInitializer.createTestUser();
        WitnessReport report = testInitializer.createTestWitnessReportWithImage(reportWriter);
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());
        final Long reportId = report.getId();

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new AddInterestAnimalRequest(reportId))
                .when()
                .post("/api/v2/users/me/interest-animals")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("message", equalTo(SUCCESS.getMessage()));

        assertThat(interestReportRepository.existsByReportIdAndUserId(reportId, user.getId())).isTrue();
    }

    @Test
    @DisplayName("이미 등록된 관심동물을 다시 등록하면 실패한다")
    void shouldFail_WhenInterestAnimalIsDuplicate() {
        // given
        User user = testInitializer.createTestUser();
        User reportWriter = testInitializer.createTestUser();
        WitnessReport report = testInitializer.createTestWitnessReportWithImage(reportWriter);
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());
        final Long reportId = report.getId();

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new AddInterestAnimalRequest(reportId))
                .when()
                .post("/api/v2/users/me/interest-animals")
                .then()
                .statusCode(200)
                .body("code", equalTo(SUCCESS.getCode()))
                .body("message", equalTo(SUCCESS.getMessage()));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new AddInterestAnimalRequest(reportId))
                .when()
                .post("/api/v2/users/me/interest-animals")
                .then()
                .statusCode(200)
                .body("code", equalTo(DUPLICATE_INTEREST_REPORT.getCode()))
                .body("message", equalTo(DUPLICATE_INTEREST_REPORT.getMessage()));

        assertThat(interestReportRepository.existsByReportIdAndUserId(reportId, user.getId())).isTrue();
    }

    @Test
    @DisplayName("관심동물이 존재하면 삭제에 성공한다")
    void shouldSucceedToDeleteInterestAnimal_WhenItExists(){
        // given
        User user = testInitializer.createTestUser();
        User reportWriter = testInitializer.createTestUser();
        ProtectingReport report = testInitializer.createTestProtectingReportWithImage(reportWriter);
        testInitializer.createTestInterestReport(user, report);

        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v2/users/me/interest-animals/" + report.getId())
                .then()
                .statusCode(200)
                .body("code", equalTo(SUCCESS.getCode()))
                .body("message", equalTo(SUCCESS.getMessage()));

        assertThat(interestReportRepository.existsByReportIdAndUserId(report.getId(), user.getId())).isFalse();
    }

    @Test
    @DisplayName("관심동물이 존재하지 않아도 삭제에 성공한다")
    void shouldSucceedToDeleteInterestAnimal_WhenItDoesNotExist(){
        // given
        User user = testInitializer.createTestUser();
        User reportWriter = testInitializer.createTestUser();
        ProtectingReport report = testInitializer.createTestProtectingReportWithImage(reportWriter);

        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when & then
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/api/v2/users/me/interest-animals/" + report.getId())
                .then()
                .statusCode(200)
                .body("code", equalTo(SUCCESS.getCode()))
                .body("message", equalTo(SUCCESS.getMessage()));

        assertThat(interestReportRepository.existsByReportIdAndUserId(report.getId(), user.getId())).isFalse();
    }
    @Test
    @DisplayName("사용자가 신고한 내역이 있다면 리턴한다.")
    void shouldReturnUserReports_WhenTheyExist(){
        // given
        User user = testInitializer.userWith3Reports();
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        CardResponseDTO reponse = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("lastId", Long.MAX_VALUE)
                .when()
                .get("/api/v2/users/me/reports")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", CardResponseDTO.class);

        // then
        assertThat(reponse.cards()).hasSize(3);
        assertThat(reponse.lastId()).isEqualTo(1);
        assertThat(reponse.isLast()).isTrue();
    }

    @Test
    @DisplayName("사용자가 신고한 내역이 없다면 빈 페이지를 리턴한다.")
    void shouldReturnEmptyPage_WhenNoUserReportExist(){
        // given
        User user = testInitializer.createTestUser();
        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        CardResponseDTO reponse = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("lastId", Long.MAX_VALUE)
                .when()
                .get("/api/v2/users/me/reports")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", CardResponseDTO.class);

        // then
        assertThat(reponse.cards()).hasSize(0);
        assertThat(reponse.lastId()).isEqualTo(-1);
        assertThat(reponse.isLast()).isTrue();
    }

    @Test
    @DisplayName("유저가 존재하면 유저 프로필을 반환한다.")
    void shouldReturnProfile_WhenUserExists(){
        // given
        User user = testInitializer.createTestUser();
        final String nickname = user.getName();
        final String profileImage = user.getProfileImageUrl();

        String token = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        GetUserProfileResponse response = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/users/me")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .jsonPath()
                .getObject("data", GetUserProfileResponse.class);

        // then
        assertThat(response.nickname()).isEqualTo(nickname);
        assertThat(response.profileImage()).isEqualTo(profileImage);
    }

}