package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@Import({TestInitializer.class})
@Transactional
@ActiveProfiles("test")
class InterestReportRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestReportRepository interestReportRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private TestInitializer testInitializer;

    private User testUser;
    private MissingReport missingReport;
    private WitnessReport witnessReport;
    private ProtectingReport protectingReport;

    @BeforeEach
    void init() {
        testUser = userRepository.save(
                User.builder()
                        .name("홍길동")
                        .profileImageUrl("http://example.com/profile.png")
                        .kakaoId(123131231231L)
                        .role(Role.USER)
                        .build()
        );
        createTestReports();
    }

    @Test
    @DisplayName("관심글 저장 및 전체 조회 테스트")
    void saveAndFindAll() {
        // given
        saveAllInterestReports();

        // when
        em.flush();
        em.clear();
        List<InterestReport> all = interestReportRepository.findAll();

        // then
        assertThat(all).hasSize(3);
        assertThat(all).extracting(ir -> ir.getReport().getTag())
                .containsExactlyInAnyOrder(ReportTag.MISSING, ReportTag.WITNESS, ReportTag.PROTECTING);
    }

    @Test
    @DisplayName("관심글 존재 여부 테스트")
    void existsByReportIdAndUserId() {
        // given
        interestReportRepository.save(InterestReport.createInterestReport(testUser, missingReport));
        Long reportId = missingReport.getId();
        Long userId = testUser.getId();

        // when
        boolean exists = interestReportRepository.existsByReportIdAndUserId(reportId, userId);
        boolean notExists = interestReportRepository.existsByReportIdAndUserId(999L, userId);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("특정 유저의 관심글 중 일부 ID 조회 테스트")
    void findInterestedReportIdsByUserIdAndReportIds() {
        // given
        saveAllInterestReports();

        List<Long> allReportIds = List.of(
                missingReport.getId(),
                witnessReport.getId(),
                protectingReport.getId(),
                999L // 존재하지 않는 Report ID
        );

        // when
        List<Long> interestedIds = interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(testUser.getId(), allReportIds);

        // then
        assertThat(interestedIds).hasSize(3);
        assertThat(interestedIds)
                .containsExactlyInAnyOrder(
                        missingReport.getId(),
                        witnessReport.getId(),
                        protectingReport.getId()
                )
                .doesNotContain(999L);
    }

    private void saveAllInterestReports() {
        interestReportRepository.save(InterestReport.createInterestReport(testUser, missingReport));
        interestReportRepository.save(InterestReport.createInterestReport(testUser, witnessReport));
        interestReportRepository.save(InterestReport.createInterestReport(testUser, protectingReport));
    }

    private void createTestReports() {
        missingReport = MissingReport.createMissingReport(
                "골든 리트리버", "개", ReportTag.MISSING, LocalDate.now().minusDays(5),
                "서울시 강남구", testUser, Sex.M, "RFID123456", "3살", "황금색", "목에 빨간 목걸이", "강남역 근처",
                new BigDecimal("37.497952"), new BigDecimal("127.027619")
        );

        witnessReport = WitnessReport.createWitnessReport(
                "믹스견", "개", ReportTag.WITNESS, LocalDate.now().minusDays(3),
                "서울시 서초구", testUser, "검은색", "오른쪽 다리 절뚝임", "이영희", "서초역 2번 출구",
                new BigDecimal("37.483569"), new BigDecimal("127.032455")
        );

        protectingReport = ProtectingReport.createProtectingReport(
                "페르시안", "고양이", ReportTag.PROTECTING, LocalDate.now().minusDays(1),
                "서울시 마포구 월드컵북로 212", testUser, Sex.F, "2살", "4kg", "흰색", Neutering.Y,
                "왼쪽 귀에 상처", "마포대교 근처", "NOTICE-2024-001",
                LocalDate.now(), LocalDate.now().plusDays(14), "마포구 동물보호센터", "02-123-4567", "마포구청",
                new BigDecimal("37.483569"), new BigDecimal("127.032675")
        );

        reportRepository.save(missingReport);
        reportRepository.save(witnessReport);
        reportRepository.save(protectingReport);
    }

    @Test
    @DisplayName("3개의 관심동물이 있으면 3개가 조회된다")
    void should_ReturnThreeInterestAnimals_When_TheyExist(){
        // given
        final int size = 20;
        saveAllInterestReports();

        // when
        List<ReportProjection> result = interestReportRepository.findInterestReportsByCursor(testUser.getId(), Long.MAX_VALUE, PageRequest.of(0, size));

        // then
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("관심동물이 없으면 아무것도 조회되지 않는다")
    void should_ReturnNothing_When_InterestAnimalsDoNotExist(){
        // given
        final int size = 20;

        // when
        List<ReportProjection> result = interestReportRepository.findInterestReportsByCursor(testUser.getId(), Long.MAX_VALUE, PageRequest.of(0, size));

        // then
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("사용자와 신고글로 관심동물을 삭제하면 성공한다")
    void deleteByUserAndReport(){
        // given
        User user = testInitializer.createTestUser();
        MissingReport report = testInitializer.createTestMissingReportWithImage(user);
        testInitializer.createTestInterestReport(user, report);

        // when
        interestReportRepository.deleteByUserAndReport(user, report);

        // then
        boolean exists = interestReportRepository.existsByReportIdAndUserId(report.getId(), user.getId());
        assertThat(exists).isFalse();
    }
}
