package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.Sex;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.MissingReportDetailStrategy;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class MissingReportDetailStrategyTest {

    @Autowired
    private MissingReportRepository missingReportRepository;

    @Autowired private EntityManager em;

    @InjectMocks
    private MissingReportDetailStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MissingReportDetailStrategy(missingReportRepository);
    }

    @Test
    @DisplayName("getReport: MissingReport 를 ID로 조회")
    void getReport_success() {
        MissingReport report = createAndSaveReport();
        em.flush();
        em.clear();

        MissingReport result = strategy.getReport(report.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(report.getId());
    }

    @Test
    @DisplayName("getDetail: MissingReport → DTO 매핑")
    void getDetail_success() {
        MissingReport report = createAndSaveReport();
        MissingReportDetailResponseDTO dto = strategy.getDetail(report, true);

        assertThat(dto.breed()).isEqualTo(report.getBreed());
        assertThat(dto.interest()).isTrue();
        assertThat(dto.latitude()).isEqualTo(report.getLatitude().doubleValue());
    }

    private MissingReport createAndSaveReport() {
        User user = persistUser();
        MissingReport report = MissingReport.createMissingReport(
                "포메", "개", ReportTag.MISSING, LocalDate.now(), "서울", user,
                Sex.M, "RFID123", "3살", "4kg", "흰색", "특이사항",
                "김철수", "010", "강남", new BigDecimal("37"), new BigDecimal("127")
        );
        return missingReportRepository.save(report);
    }

    private User persistUser() {
        User user = User.builder()
                .name("test")
                .kakaoId(123L)
                .role(Role.USER)
                .profileImageUrl("...")
                .build();
        em.persist(user);
        return user;
    }
}
