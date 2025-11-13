package com.kuit.findyou.domain.user.repository;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.report.repository.*;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.config.TestDatabaseConfig;
import jakarta.persistence.EntityManager;
import com.kuit.findyou.global.common.util.TestInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@Import({TestInitializer.class, TestDatabaseConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestInitializer testInitializer;

    @Autowired
    private ProtectingReportRepository protectingReportRepository;
    @Autowired
    private MissingReportRepository missingReportRepository;
    @Autowired
    private WitnessReportRepository witnessReportRepository;
    @Autowired
    private InterestReportRepository interestReportRepository;
    @Autowired
    private ViewedReportRepository viewedReportRepository;
    @Autowired
    private ReportImageRepository reportImageRepository;

    @Autowired
    private EntityManager em;

    @DisplayName("중복된 닉네임 존재 여부가 조회되는지 테스트")
    @Test
    void should_ReturnTrue_When_DuplicateNameExists() {
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        // when
        boolean exists = userRepository.existsByName(NAME);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("디바이스 ID로 유저가 조회되는지 테스트")
    @Test
    void should_ReturnUser_When_UserWithDeviceIdExists() {
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        // when
        Optional<User> optUser = userRepository.findByDeviceId(DEVICE_ID);

        // then
        assertThat(optUser.isPresent()).isTrue();
        User foundUser = optUser.get();
        assertThat(foundUser.getName()).isEqualTo(NAME);
        assertThat(foundUser.getRole()).isEqualTo(ROLE);
        assertThat(foundUser.getKakaoId()).isEqualTo(KAKAO_ID);
        assertThat(foundUser.getDeviceId()).isEqualTo(DEVICE_ID);
    }

    @DisplayName("카카오 ID로 유저가 조회되는지 테스트")
    @Test
    void should_ReturnUser_When_UserWithKakaoIdExists() {
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        // when
        Optional<User> optUser = userRepository.findByKakaoId(KAKAO_ID);

        // then
        assertThat(optUser.isPresent()).isTrue();
        User foundUser = optUser.get();
        assertThat(foundUser.getName()).isEqualTo(NAME);
        assertThat(foundUser.getRole()).isEqualTo(ROLE);
        assertThat(foundUser.getKakaoId()).isEqualTo(KAKAO_ID);
        assertThat(foundUser.getDeviceId()).isEqualTo(DEVICE_ID);
    }

    @Test
    @DisplayName("changeNickname 호출 후 더티체킹으로 UPDATE 가 반영된다")
    void dirtyChecking_NicknameChange() {
        // given
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        // when
        user.changeNickname("찾아유");
        em.flush();
        em.clear();

        // then
        User found = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("찾아유", found.getName());
    }

    @DisplayName("유저 ID로 유저가 삭제되는지 테스트")
    @Test
    void should_DeleteUser_When_UserIdExists() {
        final String NAME = "유저";
        final Role ROLE = Role.USER;
        final Long KAKAO_ID = 1234L;
        final String DEVICE_ID = "1234";
        User user = createUser(NAME, ROLE, KAKAO_ID, DEVICE_ID);

        Long userId = user.getId();

        // when
        userRepository.deleteById(userId);

        // then
        Optional<User> optUser = userRepository.findById(userId);

        assertThat(optUser).isEmpty();
    }

    @DisplayName("유저 삭제 시 생성된 연관 엔티티들이 전부 삭제되는지 검증")
    @Test
    void should_DeleteUserAndAllRelatedEntities_ByIds() {
        // given: 초기 데이터 구성 (리포트 3 + 관심 3 + 조회 2 + 각 리포트 이미지)
        User testUser = testInitializer.createTestUser();

        ProtectingReport testProtectingReport = testInitializer.createTestProtectingReportWithImage(testUser);
        MissingReport testMissingReport = testInitializer.createTestMissingReportWithImage(testUser);
        WitnessReport testWitnessReport = testInitializer.createTestWitnessReportWithImage(testUser);

        InterestReport testInterestReport1 = testInitializer.createTestInterestReport(testUser, testProtectingReport);
        InterestReport testInterestReport2 = testInitializer.createTestInterestReport(testUser, testMissingReport);
        InterestReport testInterestReport3 = testInitializer.createTestInterestReport(testUser, testWitnessReport);

        ViewedReport testViewedReport1 = testInitializer.createTestViewedReport(testUser, testProtectingReport);
        ViewedReport testViewedReport2 = testInitializer.createTestViewedReport(testUser, testMissingReport);

        Long userId = testUser.getId();
        Long protectingId = testProtectingReport.getId();
        Long missingId = testMissingReport.getId();
        Long witnessId = testWitnessReport.getId();

        Long interestId1 = testInterestReport1.getId();
        Long interestId2 = testInterestReport2.getId();
        Long interestId3 = testInterestReport3.getId();

        Long viewedId1 = testViewedReport1.getId();
        Long viewedId2 = testViewedReport2.getId();

        // 각 리포트에 대한 ReportImage ID들
        List<ReportImage> allImages = reportImageRepository.findAll();
        List<Long> imageIds = allImages.stream()
                .filter(img -> img.getReport() != null &&
                        (img.getReport().getId().equals(protectingId)
                                || img.getReport().getId().equals(missingId)
                                || img.getReport().getId().equals(witnessId)))
                .map(ReportImage::getId)
                .toList();

        assertThat(imageIds).isNotEmpty();

        // when: 유저 삭제
        userRepository.deleteById(userId);
        userRepository.flush();

        // then
        assertThat(userRepository.findById(userId)).isEmpty();

        assertThat(protectingReportRepository.findById(protectingId)).isEmpty();
        assertThat(missingReportRepository.findById(missingId)).isEmpty();
        assertThat(witnessReportRepository.findById(witnessId)).isEmpty();

        assertThat(interestReportRepository.findById(interestId1)).isEmpty();
        assertThat(interestReportRepository.findById(interestId2)).isEmpty();
        assertThat(interestReportRepository.findById(interestId3)).isEmpty();

        assertThat(viewedReportRepository.findById(viewedId1)).isEmpty();
        assertThat(viewedReportRepository.findById(viewedId2)).isEmpty();

        for (Long imageId : imageIds) {
            assertThat(reportImageRepository.findById(imageId)).isEmpty();
        }
    }

    private User createUser(String name, Role role, Long kakaoId, String deviceId) {
        User build = User.builder()
                .name(name)
                .role(role)
                .kakaoId(kakaoId)
                .deviceId(deviceId)
                .build();
        return userRepository.save(build);
    }

    @Test
    @DisplayName("더티체킹으로 기본 프로필 이미지(enum 문자열) 저장")
    void dirtyChecking_SaveDefaultProfileName() {
        // given
        User user = userRepository.save(User.builder()
                .name("유저")
                .role(Role.USER)
                .deviceId("dev-1")
                .build());

        // when
        user.changeProfileImage("puppy");
        em.flush();
        em.clear();

        // then
        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getProfileImageUrl()).isEqualTo("puppy");
    }

    @Test
    @DisplayName("더티체킹으로 CDN URL이 저장")
    void dirtyChecking_SaveCdnUrl() {
        // given
        User user = userRepository.save(User.builder()
                .name("유저")
                .role(Role.USER)
                .deviceId("dev-2")
                .build());

        // when
        user.changeProfileImage("https://cdn.example/profile.jpg");
        em.flush();
        em.clear();

        // then
        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getProfileImageUrl()).isEqualTo("https://cdn.example/profile.jpg");
    }
}