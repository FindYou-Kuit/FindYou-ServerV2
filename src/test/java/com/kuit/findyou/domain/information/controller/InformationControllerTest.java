package com.kuit.findyou.domain.information.controller;

import com.kuit.findyou.domain.information.dto.GetVolunteerWorksResponse;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class InformationControllerTest {
    @LocalServerPort
    int port;

    @Autowired
    TestInitializer testInitializer;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        RestAssured.port = port;
    }

    @DisplayName("봉사활동이 아무것도 없으면 첫 요청에서 마지막 페이지가 조회된다.")
    @Test
    void should_ReturnLastPage_When_RequestIsFirstOne_And_NoVolunteerWorkExists(){
        // given
        User user = testInitializer.createTestUser();
        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        GetVolunteerWorksResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
                .when()
                .get("api/v2/informations/volunteer-works")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GetVolunteerWorksResponse.class);

        // then
        assertThat(response.volunteerWorks()).isEmpty();
        assertThat(response.lastId()).isEqualTo(-1);
        assertThat(response.isLast()).isTrue();
    }

    @DisplayName("봉사활동이 페이지크기보다 적으면 첫 요청에서 마지막 페이지가 조회된다.")
    @Test
    void should_ReturnLastPage_When_RequestIsFirstOne_And_NumberOfVolunteerWorkIsLessThanPageSize(){
        // given
        final int volunteerWorkNumber = 10;
        User user = testInitializer.createTestUser();
        testInitializer.createTestVolunteerWorks(volunteerWorkNumber);

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        GetVolunteerWorksResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
                .when()
                .get("api/v2/informations/volunteer-works")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GetVolunteerWorksResponse.class);

        // then
        assertThat(response.volunteerWorks()).hasSize(volunteerWorkNumber);
        assertThat(response.lastId()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }

    @DisplayName("봉사활동이 페이지크기보다 크면 첫 요청에서 마지막 페이지가 아닌 페이지를 반환한다.")
    @Test
    void should_ReturnNonLastPage_When_RequestIsFirstOne_And_NumberOfVolunteerWorksExceedsPageSize(){
        // given
        final int size = 20;
        final int volunteerWorkNumber = 30;
        User user = testInitializer.createTestUser();
        testInitializer.createTestVolunteerWorks(volunteerWorkNumber);

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        GetVolunteerWorksResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
                .when()
                .get("api/v2/informations/volunteer-works")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GetVolunteerWorksResponse.class);

        // then
        assertThat(response.volunteerWorks()).hasSize(size);
        assertThat(response.lastId()).isEqualTo(11);
        assertThat(response.isLast()).isFalse();
    }

    @DisplayName("남은 봉사활동이 페이지크기와 같으면 첫 요청에서 마지막 페이지를 반환한다.")
    @Test
    void should_ReturnLastPage_When_RequestIsFirstOne_And_NumberOfVolunteerWorksIsEqualToPageSize(){
        // given
        final int size = 20;
        User user = testInitializer.createTestUser();
        testInitializer.createTestVolunteerWorks(size);

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        GetVolunteerWorksResponse response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", Long.MAX_VALUE)
                .when()
                .get("api/v2/informations/volunteer-works")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GetVolunteerWorksResponse.class);

        // then
        assertThat(response.volunteerWorks()).hasSize(size);
        assertThat(response.volunteerWorks().get(0).workTime()).isEqualTo("05:00 ~ 06:00");
        assertThat(response.lastId()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }
}