package com.kuit.findyou.domain.auth.controller;

import com.kuit.findyou.domain.auth.dto.request.GuestLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.GuestLoginResponse;
import com.kuit.findyou.domain.auth.dto.request.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.KakaoLoginResponse;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.config.TestDatabaseConfig;
import com.kuit.findyou.global.jwt.util.JwtTokenType;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.GUEST_LOGIN_FAILED;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
class AuthControllerTest {
    @LocalServerPort
    int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();

        RestAssured.port = port;
    }

    @DisplayName("기존 회원이 로그인하면 유저 정보를 반환한다")
    @Test
    void should_ReturnUserInfo_When_ExistingUserLogsIn(){
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String deviceId = "asdf-1234-asdf";
        User user = createUser(NAME, ROLE, KAKAO_ID, deviceId);

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
        assertThat(response.getData().userInfo().accessToken()).isNotNull();
        String accessToken = response.getData().userInfo().accessToken();
        assertThat(jwtUtil.getUserId(accessToken)).isEqualTo(user.getId());
        assertThat(jwtUtil.getRole(accessToken)).isEqualTo(user.getRole());
        assertThat(jwtUtil.getTokenType(accessToken)).isEqualTo(JwtTokenType.ACCESS_TOKEN);
    }

    private User createUser(String name, Role role, Long kakaoId, String deviceId){
        User build = User.builder()
                .name(name)
                .role(role)
                .kakaoId(kakaoId)
                .deviceId(deviceId)
                .build();

        return userRepository.save(build);
    }

    @DisplayName("게스트가 로그인하면 성공한다.")
    @Test
    void should_Succeed_When_GuestLogsIn(){
        // given
        final String deviceId = "asdf-1234-asdf";

        User user = createUser("게스트", Role.GUEST, null, deviceId);

        // when
        GuestLoginResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new GuestLoginRequest(deviceId))
                .when()
                .post("/api/v2/auth/login/guest")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("data", GuestLoginResponse.class);

        // then
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(jwtUtil.getUserId(response.accessToken())).isEqualTo(user.getId());
        assertThat(jwtUtil.getRole(response.accessToken())).isEqualTo(user.getRole());
    }
    @DisplayName("게스트가 아닌 유저가 로그인하면 실패한다.")
    @Test
    void should_Fail_When_NonGuestUserLogsIn(){
        // given
        final String deviceId = "asdf-1234-asdf";

        User user = createUser("회원", Role.USER, null, deviceId);

        // when
        BaseErrorResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new GuestLoginRequest(deviceId))
                .when()
                .post("/api/v2/auth/login/guest")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<BaseErrorResponse>() {});

        // then
        assertThat(response.getCode()).isEqualTo(GUEST_LOGIN_FAILED.getCode());
        assertThat(response.getMessage()).isEqualTo(GUEST_LOGIN_FAILED.getMessage());
        assertThat(response.getSuccess()).isFalse();
    }
}