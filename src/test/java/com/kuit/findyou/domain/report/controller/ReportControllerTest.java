package com.kuit.findyou.domain.report.controller;

import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.user.model.User;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.time.LocalDate;

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

    @Autowired
    JwtUtil jwtUtil;

    User reportWriter;

    @BeforeAll
    void setUp() {
        RestAssured.port = port;
        testInitializer.initializeReportControllerTestData();
        this.reportWriter = testInitializer.getReportWriter();
    }

    @Test
    @DisplayName("GET /api/v2/reports/protecting-reports/{id}: ProtectingReport 상세 조회 성공")
    void getProtectingReportDetail() {
        // 작성자의 엑세스 토큰 생성
        String accessToken = jwtUtil.createAccessJwt(reportWriter.getId(), reportWriter.getRole());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/reports/protecting-reports/1")
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
    @DisplayName("GET /api/v2/reports/missing-reports/{id}: MissingReport 상세 조회 성공")
    void getMissingReportDetail() {
        // 작성자의 엑세스 토큰 생성
        String accessToken = jwtUtil.createAccessJwt(reportWriter.getId(), reportWriter.getRole());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/reports/missing-reports/2")
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
    @DisplayName("GET /api/v2/reports/witness-reports/{id}: WitnessReport 상세 조회 성공")
    void getWitnessReportDetail() {
        // 작성자의 엑세스 토큰 생성
        String accessToken = jwtUtil.createAccessJwt(reportWriter.getId(), reportWriter.getRole());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/reports/witness-reports/3")
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

    @DisplayName("GET /api/v2/reports: 글 조회 성공")
    @Test
    void retrieveReportsWithFilters() {
        // 작성자의 엑세스 토큰 생성
        String accessToken = jwtUtil.createAccessJwt(reportWriter.getId(), reportWriter.getRole());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("type", ReportViewType.ALL)
                .param("lastReportId", Long.MAX_VALUE)
        .when()
                .get("/api/v2/reports")
        .then()
                .statusCode(200)
                .body("data.cards[0].reportId", equalTo(3))
                .body("data.cards[0].thumbnailImageUrl", equalTo("https://img.com/witness.png"))
                .body("data.cards[0].title", equalTo("진돗개"))
                .body("data.cards[0].tag", equalTo("목격신고"))
                .body("data.cards[0].date", equalTo("2024-08-10"))
                .body("data.cards[0].location", equalTo("부산시 해운대구"))
                .body("data.cards[0].interest", equalTo(true))
                .body("data.cards[1].reportId", equalTo(2))
                .body("data.cards[1].thumbnailImageUrl", equalTo("https://img.com/missing.png"))
                .body("data.cards[1].title", equalTo("포메라니안"))
                .body("data.cards[1].tag", equalTo("실종신고"))
                .body("data.cards[1].date", equalTo("2024-10-05"))
                .body("data.cards[1].location", equalTo("서울시 강남구"))
                .body("data.cards[1].interest", equalTo(true))
                .body("data.cards[2].reportId", equalTo(1))
                .body("data.cards[2].thumbnailImageUrl", equalTo("https://img.com/1.png"))
                .body("data.cards[2].title", equalTo("믹스견"))
                .body("data.cards[2].tag", equalTo("보호중"))
                .body("data.cards[2].date", equalTo(LocalDate.now().toString()))
                .body("data.cards[2].location", equalTo("서울"))
                .body("data.cards[2].interest", equalTo(true))
                .body("data.lastReportId", equalTo(1))
                .body("data.isLast", equalTo(true));
    }


}
