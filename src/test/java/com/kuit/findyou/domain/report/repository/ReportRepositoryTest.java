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
        missingReport = MissingReport.builder()
                .breed("골든 리트리버")
                .species("개")
                .tag(ReportTag.MISSING)
                .date(LocalDate.now().minusDays(5))
                .address("서울시 강남구")
                .user(testUser)
                .sex(Sex.M)
                .rfid("RFID123456")
                .age("3살")
                .weight("25kg")
                .furColor("황금색")
                .significant("목에 빨간 목걸이")
                .reporterInfo("김철수 010-1234-5678")
                .landmark("강남역 근처")
                .latitude(new BigDecimal("37.497952"))
                .longitude(new BigDecimal("127.027619"))
                .build();

        witnessReport = WitnessReport.builder()
                .breed("믹스견")
                .species("개")
                .tag(ReportTag.WITNESS)
                .date(LocalDate.now().minusDays(3))
                .address("서울시 서초구")
                .user(testUser)
                .furColor("검은색")
                .significant("오른쪽 다리 절뚝임")
                .reporterInfo("이영희 010-9876-5432")
                .landmark("서초역 2번 출구")
                .latitude(new BigDecimal("37.483569"))
                .longitude(new BigDecimal("127.032455"))
                .build();

        protectingReport = ProtectingReport.builder()
                .breed("페르시안")
                .species("고양이")
                .tag(ReportTag.PROTECTING)
                .date(LocalDate.now().minusDays(1))
                .address("서울시 마포구")
                .user(testUser)
                .sex(Sex.F)
                .age("2살")
                .weight("4kg")
                .furColor("흰색")
                .neutering(Neutering.Y)
                .significant("왼쪽 귀에 상처")
                .foundLocation("마포대교 근처")
                .noticeNumber("NOTICE-2024-001")
                .noticeStartDate(LocalDate.now())
                .noticeEndDate(LocalDate.now().plusDays(14))
                .careName("마포구 동물보호센터")
                .careAddr("서울시 마포구 월드컵북로 212")
                .careTel("02-123-4567")
                .authority("마포구청")
                .build();

        missingReportRepository.save(missingReport);
        witnessReportRepository.save(witnessReport);
        protectingReportRepository.save(protectingReport);

        em.flush();
    }
}