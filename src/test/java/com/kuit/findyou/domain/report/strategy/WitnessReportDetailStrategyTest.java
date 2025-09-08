package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.dto.response.WitnessReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.WitnessReport;
import com.kuit.findyou.domain.report.repository.WitnessReportRepository;
import com.kuit.findyou.domain.report.service.detail.strategy.WitnessReportDetailStrategy;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.global.config.TestDatabaseConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WitnessReportDetailStrategyTest {

    @Autowired
    private WitnessReportRepository witnessReportRepository;
    @Autowired private EntityManager em;

    private WitnessReportDetailStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new WitnessReportDetailStrategy(witnessReportRepository);
    }

    @Test
    void getReport_success() {
        WitnessReport report = createAndSaveReport();
        em.flush(); em.clear();

        WitnessReport result = strategy.getReport(report.getId());

        assertThat(result).isNotNull();
        assertThat(result.getTag()).isEqualTo(ReportTag.WITNESS);
    }

    @Test
    void getDetail_success() {
        WitnessReport report = createAndSaveReport();
        WitnessReportDetailResponseDTO dto = strategy.getDetail(report, true);

        assertThat(dto.furColor()).isEqualTo(report.getFurColor());
        assertThat(dto.interest()).isTrue();
    }

    private WitnessReport createAndSaveReport() {
        User user = persistUser();
        WitnessReport report = WitnessReport.createWitnessReport(
                "골든", "개", ReportTag.WITNESS, LocalDate.now(), "서울", user,
                "금색", "목줄 착용", "이영희", "서초역", new BigDecimal("37"), new BigDecimal("127")
        );
        return witnessReportRepository.save(report);
    }

    private User persistUser() {
        User user = User.builder().name("witness-user").kakaoId(789L).role(Role.USER).profileImageUrl("img").build();
        em.persist(user);
        return user;
    }
}
