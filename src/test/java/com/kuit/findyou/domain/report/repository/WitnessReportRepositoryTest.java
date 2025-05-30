package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.WitnessReport;
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

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
class WitnessReportRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WitnessReportRepository witnessReportRepository;

    @Autowired
    private EntityManager em;

    private User testUser;

    @BeforeEach
    void init() {
        testUser = User.builder()
                .name("홍길동")
                .profileImageUrl("http://example.com/profile.png")
                .kakaoId(123131231231L)
                .role(Role.USER)
                .build();

        userRepository.save(testUser);
    }

    @Test
    @DisplayName("WitnessReport 생성 및 저장 테스트")
    void save() {
        // Given
        WitnessReport witnessReport = WitnessReport.builder()
                .breed("골든 리트리버")
                .species("개")
                .tag(ReportTag.WITNESS)
                .date(LocalDate.of(2024, 1, 20))
                .address("서울시 서초구 서초대로 456")
                .user(testUser)
                .furColor("금색")
                .significant("목줄을 하고 있었음")
                .reporterInfo("이영희 010-9876-5432")
                .landmark("서초역 1번 출구")
                .latitude(new BigDecimal("37.483569"))
                .longitude(new BigDecimal("127.032675"))
                .build();

        // When
        WitnessReport savedReport = witnessReportRepository.save(witnessReport);
        em.flush();
        em.clear();

        // Then
        WitnessReport foundReport = witnessReportRepository.findById(savedReport.getId())
                .orElseThrow();

        assertThat(foundReport).isNotNull();
        assertThat(foundReport.getBreed()).isEqualTo("골든 리트리버");
        assertThat(foundReport.getSpecies()).isEqualTo("개");
        assertThat(foundReport.getTag()).isEqualTo(ReportTag.WITNESS);
        assertThat(foundReport.getFurColor()).isEqualTo("금색");
        assertThat(foundReport.getSignificant()).isEqualTo("목줄을 하고 있었음");
        assertThat(foundReport.getReporterInfo()).isEqualTo("이영희 010-9876-5432");
        assertThat(foundReport.getLandmark()).isEqualTo("서초역 1번 출구");
        assertThat(foundReport.getLatitude()).isEqualTo(new BigDecimal("37.483569"));
        assertThat(foundReport.getLongitude()).isEqualTo(new BigDecimal("127.032675"));
    }


}