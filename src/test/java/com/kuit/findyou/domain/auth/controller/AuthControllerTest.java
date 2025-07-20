package com.kuit.findyou.domain.auth.controller;

import com.kuit.findyou.domain.auth.dto.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.KakaoLoginResponse;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.response.BaseResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {
    @LocalServerPort
    int port;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void should_ReturnUserInfo_When_ExistingUserLogsIn(){
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        User user = createUser(NAME, ROLE, KAKAO_ID);

        // when
        BaseResponse<KakaoLoginResponse> response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new KakaoLoginRequest(KAKAO_ID))
                .when()
                .post("/api/v2/auth/login/kakao")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<BaseResponse<KakaoLoginResponse>>() {});

        // then
        assertThat(response.getData().isFirstLogin()).isFalse();
        assertThat(response.getData().userInfo()).isNotNull();
        assertThat(response.getData().userInfo().userId()).isEqualTo(user.getId());
        assertThat(response.getData().userInfo().nickname()).isEqualTo(user.getName());
    }

    private User createUser(String name, Role role, Long kakaoId){
        User build = User.builder()
                .name(name)
                .role(role)
                .kakaoId(kakaoId)
                .build();

        return userRepository.save(build);
    }
}