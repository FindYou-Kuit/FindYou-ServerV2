package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.MissingReport;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.model.Sex;
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
class MissingReportRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MissingReportRepository missingReportRepository;

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
    @DisplayName("MissingReport 생성 및 저장 테스트")
    void save() {
        // Given
        MissingReport missingReport = MissingReport.builder()
                .breed("포메라니안")
                .species("개")
                .tag(ReportTag.MISSING)
                .date(LocalDate.of(2024, 1, 15))
                .address("서울시 강남구 테헤란로 123")
                .user(testUser)
                .sex(Sex.M)
                .rfid("RFID123456789")
                .age("3살")
                .weight("3.5kg")
                .furColor("흰색")
                .significant("왼쪽 귀에 검은 점이 있음")
                .reporterInfo("김철수 010-1234-5678")
                .landmark("강남역 2번 출구 근처")
                .latitude(new BigDecimal("37.498095"))
                .longitude(new BigDecimal("127.027610"))
                .build();

        // When
        MissingReport savedReport = missingReportRepository.save(missingReport);
        em.flush();
        em.clear();

        // Then
        MissingReport foundReport = missingReportRepository.findById(savedReport.getId())
                .orElseThrow();

        assertThat(foundReport).isNotNull();
        assertThat(foundReport.getBreed()).isEqualTo("포메라니안");
        assertThat(foundReport.getSpecies()).isEqualTo("개");
        assertThat(foundReport.getTag()).isEqualTo(ReportTag.MISSING);
        assertThat(foundReport.getSex()).isEqualTo(Sex.M);
        assertThat(foundReport.getRfid()).isEqualTo("RFID123456789");
        assertThat(foundReport.getAge()).isEqualTo("3살");
        assertThat(foundReport.getWeight()).isEqualTo("3.5kg");
        assertThat(foundReport.getFurColor()).isEqualTo("흰색");
        assertThat(foundReport.getSignificant()).isEqualTo("왼쪽 귀에 검은 점이 있음");
        assertThat(foundReport.getReporterInfo()).isEqualTo("김철수 010-1234-5678");
        assertThat(foundReport.getLandmark()).isEqualTo("강남역 2번 출구 근처");
        assertThat(foundReport.getLatitude()).isEqualTo(new BigDecimal("37.498095"));
        assertThat(foundReport.getLongitude()).isEqualTo(new BigDecimal("127.027610"));
    }


}