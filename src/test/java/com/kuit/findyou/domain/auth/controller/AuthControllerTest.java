package com.kuit.findyou.domain.auth.controller;

import com.kuit.findyou.domain.auth.dto.ReissueTokenRequest;
import com.kuit.findyou.domain.auth.dto.ReissueTokenResponse;
import com.kuit.findyou.domain.auth.dto.request.GuestLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.GuestLoginResponse;
import com.kuit.findyou.domain.auth.dto.request.KakaoLoginRequest;
import com.kuit.findyou.domain.auth.dto.response.KakaoLoginResponse;
import com.kuit.findyou.domain.auth.repository.RedisRefreshTokenRepository;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.response.BaseErrorResponse;
import com.kuit.findyou.global.common.response.BaseResponse;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.config.TestDatabaseConfig;
import com.kuit.findyou.global.jwt.util.JwtClaimKey;
import com.kuit.findyou.global.jwt.util.JwtTokenType;
import com.kuit.findyou.global.jwt.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;
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
    private RedisRefreshTokenRepository redisRefreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Value("${findyou.jwt.secret-key}") 
    String secret;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();

        RestAssured.port = port;
    }

    @DisplayName("기존 회원이 로그인하면 유저 정보를 반환한다")
    @Test
    void kakaoLogin_shouldReturnUserInfo_WhenAlreadyRegisteredUser(){
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
    void guestLogin_shouldSucceed_WhenAlreadyRegisteredGuest(){
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
    void guestLogin_shouldFail_WhenUnknownUser(){
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
                .extract()
                .as(new TypeRef<BaseErrorResponse>() { });

        // then
        assertThat(response.getCode()).isEqualTo(GUEST_LOGIN_FAILED.getCode());
        assertThat(response.getMessage()).isEqualTo(GUEST_LOGIN_FAILED.getMessage());
        assertThat(response.getSuccess()).isFalse();
    }

    @DisplayName("올바른 리프레시 토큰으로 요청하면 200 코드를 응답한다")
    @Test
    void reissueToken_shouldReturnSuccess_WhenValidRefreshToken(){
        // given
        User user = createUser("회원", Role.USER, null, "asdf-1234-asdf");
        String refreshToken = jwtUtil.createRefreshJwt(user.getId());
        redisRefreshTokenRepository.save(user.getId(), refreshToken);

        // when
        ReissueTokenResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new ReissueTokenRequest(refreshToken))
                .when()
                .post("/api/v2/auth/reissue/token")
                .then()
                .extract()
                .jsonPath()
                .getObject("data", ReissueTokenResponse.class);

        // then
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    @DisplayName("리프레시 토큰이 만료되었으면 401 코드를 응답한다")
    @Test
    void reissueToken_shouldReturnUnauthorized_WhenRefreshTokenIsNotMatched(){
        // given
        User user = createUser("회원", Role.USER, null, "asdf-1234-asdf");
        String expiredRefreshToken = createExpiredRefreshJwt(user.getId());

        // when
        BaseErrorResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new ReissueTokenRequest(expiredRefreshToken))
                .when()
                .post("/api/v2/auth/reissue/token")
                .then()
                .log().all()
                .extract()
                .as(new TypeRef<BaseErrorResponse>() {
                });

        // then
        assertThat(response.getCode()).isEqualTo(EXPIRED_JWT.getCode());
        assertThat(response.getMessage()).isEqualTo(EXPIRED_JWT.getMessage());
    }

    private String createExpiredRefreshJwt(Long userId) {
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimKey.USER_ID.getKey(), userId)
                .claim(JwtClaimKey.TOKEN_TYPE.getKey(), JwtTokenType.REFRESH_TOKEN)
                .issuedAt(new Date(System.currentTimeMillis() - 100))
                .expiration(new Date(System.currentTimeMillis()))
                .signWith(secretKey)
                .compact();
    }

    @DisplayName("리프레시 토큰이 일치하지 않으면 404 코드를 응답한다")
    @Test
    void reissueToken_shouldReturnNotFound_WhenRefreshTokenIsNotMatched(){
// given
        User user = createUser("회원", Role.USER, null, "asdf-1234-asdf");
        String unknownRefreshToken = jwtUtil.createRefreshJwt(user.getId());
        String savedRefreshToken = jwtUtil.createRefreshJwt(user.getId());
        redisRefreshTokenRepository.save(user.getId(), savedRefreshToken);

        // when
        BaseErrorResponse response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(new ReissueTokenRequest(unknownRefreshToken))
                .when()
                .post("/api/v2/auth/reissue/token")
                .then()
                .log().all()
                .extract()
                .as(new TypeRef<BaseErrorResponse>() {
                });

        // then
        assertThat(response.getCode()).isEqualTo(REFRESH_TOKEN_NOT_FOUND.getCode());
        assertThat(response.getMessage()).isEqualTo(REFRESH_TOKEN_NOT_FOUND.getMessage());
    }
}