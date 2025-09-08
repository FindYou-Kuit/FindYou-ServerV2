package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.Neutering;
import com.kuit.findyou.domain.report.model.ProtectingReport;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.Sex;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.ProtectingReportDetailStrategy;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.config.TestDatabaseConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProtectingReportDetailStrategyTest {

    @Autowired
    private ProtectingReportRepository protectingReportRepository;
    @Autowired private EntityManager em;

    private ProtectingReportDetailStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ProtectingReportDetailStrategy(protectingReportRepository);
    }

    @Test
    @DisplayName("getReport: ProtectingReport 를 ID로 조회")
    void getReport_success() {
        ProtectingReport report = createAndSaveReport();
        em.flush(); em.clear();

        ProtectingReport result = strategy.getReport(report.getId());

        assertThat(result).isNotNull();
        assertThat(result.getBreed()).isEqualTo(report.getBreed());
    }

    @Test
    @DisplayName("getDetail: ProtectingReport → DTO 매핑")
    void getDetail_success() {
        ProtectingReport report = createAndSaveReport();
        ProtectingReportDetailResponseDTO dto = strategy.getDetail(report, false);

        assertThat(dto.breed()).isEqualTo(report.getBreed());
        assertThat(dto.interest()).isFalse();
        assertThat(dto.careName()).isEqualTo(report.getCareName());
    }

    private ProtectingReport createAndSaveReport() {
        User user = persistUser();
        ProtectingReport report = ProtectingReport.createProtectingReport(
                "믹스", "개", ReportTag.PROTECTING, LocalDate.now(), "서울", user,
                Sex.F, "2살", "5kg", "갈색", Neutering.Y, "절뚝거림",
                "홍대", "NOTICE123", LocalDate.now(), LocalDate.now().plusDays(10),
                "센터", "02", "관청", new BigDecimal("37"), new BigDecimal("127")
        );
        return protectingReportRepository.save(report);
    }

    private User persistUser() {
        User user = User.builder().name("user").kakaoId(456L).role(Role.USER).profileImageUrl("img").build();
        em.persist(user);
        return user;
    }
}
