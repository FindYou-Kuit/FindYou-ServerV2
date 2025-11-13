package com.kuit.findyou.domain.inquiry.controller;

import com.kuit.findyou.domain.inquiry.dto.AddInquiryRequest;
import com.kuit.findyou.domain.inquiry.model.Inquiry;
import com.kuit.findyou.domain.inquiry.repository.InquiryRepository;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import com.kuit.findyou.global.config.TestDatabaseConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.SUCCESS;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
class InquiryControllerTest {
    @LocalServerPort
    int port;

    @Autowired
    TestInitializer testInitializer;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    InquiryRepository inquiryRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        RestAssured.port = port;
    }

    @DisplayName("요청이 올바르면 문의사항을 저장한다")
    @Test
    void shouldSaveNewInquiry_WhenRequestIsCorrect(){
        // given
        User user = testInitializer.createTestUser();

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        final String title = "버그 신고";
        // when
        final String content = "홈화면에서 버튼이 안 눌려요";
        List<String> categories = List.of("오류/버그 신고", "개선 및 피드백");

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(new AddInquiryRequest(title, content, categories))
                .when()
                .post("/api/v2/inquiries")
                .then()
                .statusCode(200)
                .body("code", equalTo(SUCCESS.getCode()))
                .body("message", equalTo(SUCCESS.getMessage()));

        // then
        List<Inquiry> inquiries = inquiryRepository.findAll();
        assertThat(inquiries).hasSize(1);
        assertThat(inquiries.get(0).getCategory()).isEqualTo("오류/버그 신고&개선 및 피드백");
        assertThat(inquiries.get(0).getTitle()).isEqualTo(title);
        assertThat(inquiries.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("카테고리가 비어 있으면 문의사항을 저장하지 않는다")
    @Test
    void shouldNotSaveNewInquiry_WhenCategoryIsEmpty(){
        // given
        User user = testInitializer.createTestUser();

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        final String title = "버그 신고";
        // when
        final String content = "홈화면에서 버튼이 안 눌려요";
        List<String> categories = List.of();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(new AddInquiryRequest(title, content, categories))
                .when()
                .post("/api/v2/inquiries")
                .then()
                .statusCode(200)
                .body("code", equalTo(BAD_REQUEST.getCode()));

        // then
        List<Inquiry> inquiries = inquiryRepository.findAll();
        assertThat(inquiries).hasSize(0);
    }

    @DisplayName("제목이 빈 문자열이면 문의사항을 저장하지 않는다")
    @Test
    void shouldNotSaveNewInquiry_WhenTitleIsBlank(){
        // given
        User user = testInitializer.createTestUser();

        String accessToken = jwtUtil.createAccessJwt(user.getId(), user.getRole());

        final String title = "";
        // when
        final String content = "홈화면에서 버튼이 안 눌려요";
        List<String> categories = List.of("오류/버그 신고", "개선 및 피드백");

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(new AddInquiryRequest(title, content, categories))
                .when()
                .post("/api/v2/inquiries")
                .then()
                .statusCode(200)
                .body("code", equalTo(BAD_REQUEST.getCode()));

        // then
        List<Inquiry> inquiries = inquiryRepository.findAll();
        assertThat(inquiries).hasSize(0);
    }
}