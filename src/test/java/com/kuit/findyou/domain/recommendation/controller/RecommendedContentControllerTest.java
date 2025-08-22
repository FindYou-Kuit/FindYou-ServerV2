import com.kuit.findyou.FindyouApplication;
import com.kuit.findyou.domain.information.recommended.model.RecommendedNews;
import com.kuit.findyou.domain.information.recommended.model.RecommendedVideo;
import com.kuit.findyou.domain.information.recommended.repository.RecommendedNewsRepository;
import com.kuit.findyou.domain.information.recommended.repository.RecommendedVideoRepository;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
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

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = FindyouApplication.class
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class RecommendedContentControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecommendedNewsRepository recommendedNewsRepository;

    @Autowired
    RecommendedVideoRepository recommendedVideoRepository;


    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    JwtUtil jwtUtil;

    Long testUserId;

    @BeforeAll
    void setUp() {
        databaseCleaner.execute();
        RestAssured.port = port;

        User testUser = User.builder()
                .name("추천유저")
                .profileImageUrl("https://img.com/profile.png")
                .kakaoId(9999999L)
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
        this.testUserId = testUser.getId();

        // 추천 기사 저장
        recommendedNewsRepository.saveAll(List.of(
                RecommendedNews.builder()
                        .title("기사 제목 1")
                        .url("https://news.com/1")
                        .uploader("출처 1")
                        .build(),
                RecommendedNews.builder()
                        .title("기사 제목 2")
                        .url("https://news.com/2")
                        .uploader("출처 2")
                        .build()
        ));

        // 추천 영상 저장
        recommendedVideoRepository.saveAll(List.of(
                RecommendedVideo.builder()
                        .title("영상 제목 1")
                        .url("https://video.com/1")
                        .uploader("업로더 1")
                        .build()
        ));
    }

    private String getAccessToken() {
        return jwtUtil.createAccessJwt(testUserId, Role.USER);
    }

    @Test
    @DisplayName("GET /api/v2/informations/videos - 추천 영상 목록 조회 성공")
    void getRecommendedVideos() {
        String accessToken = getAccessToken();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/informations/videos")
                .then()
                .statusCode(200)
                .body("data.size()", greaterThanOrEqualTo(0))
                .body("data[0].title", notNullValue())
                .body("data[0].url", notNullValue());
    }
    @Test
    @DisplayName("VIDEO 타입 추천 콘텐츠 목록 조회 성공")
    void getVideoRecommendations_success() {
        String accessToken = getAccessToken();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/informations/videos")
                .then()
                .statusCode(200)
                .body("data[0].title", equalTo("영상 제목 1"))
                .body("data[0].url", equalTo("https://video.com/1"))
                .body("data[0].uploader", equalTo("업로더 1"));
    }

    @Test
    @DisplayName("GET /api/v2/informations/news - 추천 기사 목록 조회 성공")
    void getRecommendedNews() {
        String accessToken = getAccessToken();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/informations/news")
                .then()
                .statusCode(200)
                .body("data.size()", greaterThanOrEqualTo(0))
                .body("data[0].title", notNullValue())
                .body("data[0].url", notNullValue());
    }

    @Test
    @DisplayName("NEWS 타입 추천 콘텐츠 목록 조회 성공")
    void getNewsRecommendations_success() {
        String accessToken = getAccessToken();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/informations/news")
                .then()
                .statusCode(200)
                .body("data[0].title", equalTo("기사 제목 1"))
                .body("data[0].url", equalTo("https://news.com/1"))
                .body("data[0].uploader", equalTo("출처 1"))
                .body("data[1].title", equalTo("기사 제목 2"))
                .body("data[1].url", equalTo("https://news.com/2"))
                .body("data[1].uploader", equalTo("출처 2"));
    }

}
