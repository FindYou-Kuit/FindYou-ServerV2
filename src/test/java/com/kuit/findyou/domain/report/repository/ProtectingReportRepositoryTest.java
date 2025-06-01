package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.model.Neutering;
import com.kuit.findyou.domain.report.model.ProtectingReport;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
class ProtectingReportRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProtectingReportRepository protectingReportRepository;

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
    @DisplayName("ProtectingReport 저장 테스트")
    void save() {
        // Given
        ProtectingReport protectingReport = ProtectingReport.createProtectingReport(
                "믹스견",
                "개",
                ReportTag.PROTECTING,
                LocalDate.of(2024, 1, 25),
                "서울시 마포구 홍대입구로 789",
                testUser,
                Sex.F,
                "추정 2살",
                "5kg",
                "갈색",
                Neutering.Y,
                "오른쪽 다리를 절뚝거림",
                "홍대입구역 9번 출구 앞",
                "NOTICE2024001",
                LocalDate.of(2024, 1, 25),
                LocalDate.of(2024, 2, 25),
                "서울시 동물보호센터",
                "서울시 중랑구 용마산로 560",
                "02-2290-8840",
                "서울시 마포구청"
        );


        // When
        ProtectingReport savedReport = protectingReportRepository.save(protectingReport);
        em.flush();
        em.clear();

        // Then
        ProtectingReport foundReport = protectingReportRepository.findById(savedReport.getId())
                .orElseThrow();

        assertThat(foundReport).isNotNull();
        assertThat(foundReport.getBreed()).isEqualTo("믹스견");
        assertThat(foundReport.getSpecies()).isEqualTo("개");
        assertThat(foundReport.getTag()).isEqualTo(ReportTag.PROTECTING);
        assertThat(foundReport.getSex()).isEqualTo(Sex.F);
        assertThat(foundReport.getAge()).isEqualTo("추정 2살");
        assertThat(foundReport.getWeight()).isEqualTo("5kg");
        assertThat(foundReport.getFurColor()).isEqualTo("갈색");
        assertThat(foundReport.getNeutering()).isEqualTo(Neutering.Y);
        assertThat(foundReport.getSignificant()).isEqualTo("오른쪽 다리를 절뚝거림");
        assertThat(foundReport.getFoundLocation()).isEqualTo("홍대입구역 9번 출구 앞");
        assertThat(foundReport.getNoticeNumber()).isEqualTo("NOTICE2024001");
        assertThat(foundReport.getNoticeStartDate()).isEqualTo(LocalDate.of(2024, 1, 25));
        assertThat(foundReport.getNoticeEndDate()).isEqualTo(LocalDate.of(2024, 2, 25));
        assertThat(foundReport.getCareName()).isEqualTo("서울시 동물보호센터");
        assertThat(foundReport.getCareAddr()).isEqualTo("서울시 중랑구 용마산로 560");
        assertThat(foundReport.getCareTel()).isEqualTo("02-2290-8840");
        assertThat(foundReport.getAuthority()).isEqualTo("서울시 마포구청");
    }

}