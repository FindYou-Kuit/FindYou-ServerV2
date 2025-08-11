package com.kuit.findyou.domain.animalProtection.controller;

import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class AnimalShelterControllerTest {
    @LocalServerPort
    int port;

    @Autowired
    TestInitializer testInitializer;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    JwtUtil jwtUtil;

    User user;

    @BeforeAll
    void setUp() {
        databaseCleaner.execute();
        RestAssured.port = port;
        testInitializer.userWith3InterestReportsAnd2ViewedReports();
        this.user = testInitializer.getDefaultUser();
    }

    @Test
    @DisplayName("GET /api/v2/informations/shelters-and-hospitals - 관할구역, 유형 필터 조회")
     void getShelters_withJurisdictionAndType() {
        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", 0L)
                .param("type", "hospital")
                .param("sido", "서울특별시")
                .param("sigungu", "강남구")
        .when()
                .get("/api/v2/informations/shelters-and-hospitals")
        .then()
                .statusCode(200)
                .body("data.centers[0].jurisdiction.size()", equalTo(2))
                .body("data.centers[0].jurisdiction", hasItems("서울특별시 강남구", "서울특별시 서초구"))
                .body("data.centers[0].centerName", containsString("병원"));
    }

    @Test
    @DisplayName("GET /api/v2/informations/shelters-and-hospitals - 위치 기반 조회")
    void getNearbyShelters_withLatLng() {
        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("lastId", 0L)
                .param("lat", 37.5)
                .param("long", 127.1)
        .when()
                .get("/api/v2/informations/shelters-and-hospitals")
        .then()
                .statusCode(200)
                .body("data.centers.size()", greaterThan(0))
                .body("data.centers[0].centerName", notNullValue());
    }
}
