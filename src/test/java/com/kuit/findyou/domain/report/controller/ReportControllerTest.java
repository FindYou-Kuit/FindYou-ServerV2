package com.kuit.findyou.domain.report.controller;

import com.kuit.findyou.global.common.util.TestInitializer;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ReportControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestInitializer testInitializer;

    @BeforeAll
    void setUp() {
        RestAssured.port = port;
        testInitializer.initializeReportControllerTestData();
    }

    @Test
    @DisplayName("GET /api/v2/reports/protecting/{id}: ProtectingReport 상세 조회 성공")
    void getProtectingReportDetail() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/reports/protecting/1")
                .then()
                .statusCode(200)
                .body("data.imageUrls[0]", equalTo("https://img.com/1.png"))
                .body("data.breed", equalTo("믹스견"))
                .body("data.tag", equalTo("보호중"))
                .body("data.age", equalTo("2살"))
                .body("data.weight", equalTo("5kg"))
                .body("data.furColor", equalTo("갈색"))
                .body("data.sex", equalTo("수컷"))
                .body("data.neutering", equalTo("Y"))
                .body("data.significant", equalTo("절뚝거림"))
                .body("data.careName", equalTo("광진보호소"))
                .body("data.careAddr", equalTo("서울"))
                .body("data.latitude", equalTo(37.0f))
                .body("data.longitude", equalTo(127.0f))
                .body("data.careTel", equalTo("02"))
                .body("data.foundDate", notNullValue())
                .body("data.foundLocation", equalTo("홍대"))
                .body("data.noticeDuration", notNullValue())
                .body("data.noticeNumber", equalTo("NOTICE123"))
                .body("data.authority", equalTo("관청"))
                .body("data.interest", equalTo(true));
    }

    @Test
    @DisplayName("GET /api/v2/reports/missing/{id}: MissingReport 상세 조회 성공")
    void getMissingReportDetail() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/reports/missing/2")
                .then()
                .statusCode(200)
                .body("data.imageUrls[0]", equalTo("https://img.com/missing.png"))
                .body("data.breed", equalTo("포메라니안"))
                .body("data.tag", equalTo("실종신고"))
                .body("data.age", equalTo("3살"))
                .body("data.sex", equalTo("암컷"))
                .body("data.missingDate", equalTo("2024-10-05"))
                .body("data.rfid", equalTo("RF12345"))
                .body("data.significant", equalTo("눈 주변 갈색 털"))
                .body("data.missingLocation", equalTo("강남역 10번 출구"))
                .body("data.missingAddress", equalTo("서울시 강남구"))
                .body("data.latitude", equalTo(37.501f))
                .body("data.longitude", equalTo(127.025f))
                .body("data.reporterName", equalTo("이슬기"))
                .body("data.reporterTel", equalTo("010-1111-2222"))
                .body("data.interest", equalTo(true));
    }

    @Test
    @DisplayName("GET /api/v2/reports/witness/{id}: WitnessReport 상세 조회 성공")
    void getWitnessReportDetail() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/reports/witness/3")
                .then()
                .statusCode(200)
                .body("data.imageUrls[0]", equalTo("https://img.com/witness.png"))
                .body("data.breed", equalTo("진돗개"))
                .body("data.tag", equalTo("목격신고"))
                .body("data.furColor", equalTo("하얀 털"))
                .body("data.significant", equalTo("목줄 없음"))
                .body("data.witnessLocation", equalTo("해변가"))
                .body("data.witnessAddress", equalTo("부산시 해운대구"))
                .body("data.latitude", equalTo(35.158f))
                .body("data.longitude", equalTo(129.16f))
                .body("data.reporterInfo", equalTo("신성훈"))
                .body("data.witnessDate", equalTo("2024-08-10"))
                .body("data.interest", equalTo(true));
    }
}
