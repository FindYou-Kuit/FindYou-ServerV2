package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
class ReportRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MissingReportRepository missingReportRepository;

    @Autowired
    private WitnessReportRepository witnessReportRepository;

    @Autowired
    private ProtectingReportRepository protectingReportRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EntityManager em;

    private User testUser;
    private MissingReport missingReport;
    private WitnessReport witnessReport;
    private ProtectingReport protectingReport;


    @BeforeEach
    void init() {
        testUser = User.builder()
                .name("홍길동")
                .profileImageUrl("http://example.com/profile.png")
                .kakaoId(123131231231L)
                .role(Role.USER)
                .build();

        userRepository.save(testUser);

        createTestReports();
    }

    @Test
    @DisplayName("모든 Report 조회 테스트 (다형성)")
    void findAllReportsTest() {
        // When
        List<Report> allReports = reportRepository.findAll();

        // Then
        assertThat(allReports).hasSize(3);
        assertThat(allReports.get(0)).isInstanceOf(MissingReport.class);
        assertThat(allReports.get(1)).isInstanceOf(WitnessReport.class);
        assertThat(allReports.get(2)).isInstanceOf(ProtectingReport.class);
    }

    @Test
    @DisplayName("태그별 조회 (다형성)")
    void findReportsByTagTest() {
        List<Report> missingReports = reportRepository.findByTag(ReportTag.MISSING);
        assertThat(missingReports).hasSize(1);
        assertThat(missingReports.get(0)).isInstanceOf(MissingReport.class);

        List<Report> witnessReports = reportRepository.findByTag(ReportTag.WITNESS);
        assertThat(witnessReports).hasSize(1);
        assertThat(witnessReports.get(0)).isInstanceOf(WitnessReport.class);

        List<Report> protectingReports = reportRepository.findByTag(ReportTag.PROTECTING);
        assertThat(protectingReports).hasSize(1);
        assertThat(protectingReports.get(0)).isInstanceOf(ProtectingReport.class);

    }

    @Test
    @DisplayName("부모 Repository로 Report 삭제 테스트 (다형성)")
    void deleteReportByParentRepositoryTest() {
        // Given
        List<Report> allReports = reportRepository.findAll();
        assertThat(allReports).hasSize(3);

        Report reportToDelete = allReports.get(0); // MissingReport
        Long reportId = reportToDelete.getId();

        // When - 부모 Repository로 삭제
        reportRepository.deleteById(reportId);
        em.flush();
        em.clear();

        // Then
        List<Report> remainingReports = reportRepository.findAll();
        assertThat(remainingReports).hasSize(2);
        assertThat(reportRepository.findById(reportId)).isEmpty();

        // 삭제된 것이 MissingReport였는지 확인
        List<Report> missingReports = reportRepository.findByTag(ReportTag.MISSING);
        assertThat(missingReports).isEmpty();
    }

    @Test
    @DisplayName("모든 Report 삭제 테스트")
    void deleteAllReportsTest() {
        // Given
        assertThat(reportRepository.findAll()).hasSize(3);

        // When
        reportRepository.deleteAll();
        em.flush();
        em.clear();

        // Then
        assertThat(reportRepository.findAll()).isEmpty();
        assertThat(missingReportRepository.findAll()).isEmpty();
        assertThat(witnessReportRepository.findAll()).isEmpty();
        assertThat(protectingReportRepository.findAll()).isEmpty();

        // User는 여전히 존재해야 함
        assertThat(userRepository.findById(testUser.getId())).isPresent();
    }


    private void createTestReports() {
        missingReport = MissingReport.createMissingReport(
                "골든 리트리버",
                "개",
                ReportTag.MISSING,
                LocalDate.now().minusDays(5),
                "서울시 강남구",
                testUser,
                Sex.M,
                "RFID123456",
                "3살",
                "25kg",
                "황금색",
                "목에 빨간 목걸이",
                "김철수 010-1234-5678",
                "강남역 근처",
                new BigDecimal("37.497952"),
                new BigDecimal("127.027619")
        );


        witnessReport = WitnessReport.createWitnessReport(
                "믹스견",
                "개",
                ReportTag.WITNESS,
                LocalDate.now().minusDays(3),
                "서울시 서초구",
                testUser,
                "검은색",
                "오른쪽 다리 절뚝임",
                "이영희 010-9876-5432",
                "서초역 2번 출구",
                new BigDecimal("37.483569"),
                new BigDecimal("127.032455")
        );


        protectingReport = ProtectingReport.createProtectingReport(
                "페르시안",
                "고양이",
                ReportTag.PROTECTING,
                LocalDate.now().minusDays(1),
                "서울시 마포구",
                testUser,
                Sex.F,
                "2살",
                "4kg",
                "흰색",
                Neutering.Y,
                "왼쪽 귀에 상처",
                "마포대교 근처",
                "NOTICE-2024-001",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "마포구 동물보호센터",
                "서울시 마포구 월드컵북로 212",
                "02-123-4567",
                "마포구청"
        );


        missingReportRepository.save(missingReport);
        witnessReportRepository.save(witnessReport);
        protectingReportRepository.save(protectingReport);

        em.flush();
    }
}