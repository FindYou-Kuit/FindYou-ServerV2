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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class ViewedReportRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ViewedReportRepository viewedReportRepository;

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
    @DisplayName("최근 본 글 저장 및 전체 조회 테스트")
    void saveAndFindAll() {
        // given
        saveAllViewedReports();

        // when
        em.flush();
        em.clear();

        List<ViewedReport> all = viewedReportRepository.findAll();

        // then
        assertThat(all).hasSize(3);
        assertThat(all).extracting(ir -> ir.getReport().getTag())
                .containsExactlyInAnyOrder(ReportTag.MISSING, ReportTag.WITNESS, ReportTag.PROTECTING);
    }

    @Test
    @DisplayName("최근 본 글 삭제 테스트")
    void deleteByUserIdAndReportId() {
        // given
        saveAllViewedReports();

        Long userId = testUser.getId();
        Long reportId = witnessReport.getId();

        ViewedReport toDelete = viewedReportRepository.findAll().stream()
                .filter(v -> v.getReport().getId().equals(reportId))
                .findFirst()
                .orElseThrow();

        // when
        viewedReportRepository.deleteByUserIdAndReportId(userId, reportId);
        em.flush();
        em.clear();

        // then
        assertThat(viewedReportRepository.findById(toDelete.getId())).isEmpty();
        assertThat(viewedReportRepository.findAll()).hasSize(2);
    }


    @Test
    @DisplayName("최근 본 글 페이징 및 정렬 테스트")
    void findByUserIdAndIdLessThanOrderByIdDesc() {
        // given
        saveAllViewedReports();

        Long userId = testUser.getId();
        Long maxId = viewedReportRepository.findAll().stream()
                .mapToLong(ViewedReport::getId)
                .max()
                .orElseThrow();

        // when
        Slice<ViewedReport> slice = viewedReportRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, maxId + 1, PageRequest.of(0, 20));

        // then
        assertThat(slice).hasSize(3);
        List<Long> ids = slice.getContent().stream().map(ViewedReport::getId).toList();
        assertThat(ids).isSortedAccordingTo((a, b) -> Long.compare(b, a)); // Desc order
    }


    private void saveAllViewedReports() {
        viewedReportRepository.save(ViewedReport.createViewedReport(testUser, missingReport));
        viewedReportRepository.save(ViewedReport.createViewedReport(testUser, witnessReport));
        viewedReportRepository.save(ViewedReport.createViewedReport(testUser, protectingReport));
    }


    private void createTestReports() {
        missingReport = MissingReport.createMissingReport(
                "골든 리트리버", "개", ReportTag.MISSING, LocalDate.now().minusDays(5),
                "서울시 강남구", testUser, Sex.M, "RFID123456", "3살", "25kg", "황금색", "목에 빨간 목걸이",
                "김철수", "010-1234-5678", "강남역 근처",
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

}