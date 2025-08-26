package com.kuit.findyou.domain.information.controller;

import com.kuit.findyou.domain.information.dto.GetAnimalDepartmentsResponse;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

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

    @DisplayName("보호부서 조회 - 데이터가 없으면 빈 리스트와 isLast=true 반환")
    @Test
    void should_ReturnEmptyDepartments_When_NoDataExists() {
        // given
        User user = testInitializer.createTestUser();
        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        var response = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", 0L)
                .param("size", 20)
                .when()
                .get("api/v2/informations/departments")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("data");

        // then
        assertThat((List<?>) response.get("departments")).isEmpty();
        assertThat(response.get("lastId")).isEqualTo(-1);
        assertThat(response.get("isLast")).isEqualTo(true);
    }

    @DisplayName("보호부서 조회 - 데이터가 있으면 정상적으로 반환")
    @Test
    void should_ReturnDepartments_When_DataExists() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestAnimalDepartments(5);

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        // when
        var response  = given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", 0L)
                .param("size", 20)
                .when()
                .get("api/v2/informations/departments")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GetAnimalDepartmentsResponse.class);

        // then
        assertThat(response.departments()).isNotEmpty();
        assertThat(response.departments().get(0).department()).isNotBlank();   // ← DTO 필드로 검증
        assertThat(response.isLast()).isTrue();
    }
    @TestConfiguration
    static class TestRestConfig {
        @Bean
        RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder.build();
        }
    }
}