package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class WitnessReportRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WitnessReportRepository witnessReportRepository;

    @Autowired
    private ReportImageRepository reportImageRepository;

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
        WitnessReport witnessReport = WitnessReport.createWitnessReport(
                "골든 리트리버",
                "개",
                ReportTag.WITNESS,
                LocalDate.of(2024, 1, 20),
                "서울시 서초구 서초대로 456",
                testUser,
                "금색",
                "목줄을 하고 있었음",
                "이영희",
                "서초역 1번 출구",
                new BigDecimal("37.483569"),
                new BigDecimal("127.032675")
        );

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
        assertThat(foundReport.getReporterName()).isEqualTo("이영희");
        assertThat(foundReport.getLandmark()).isEqualTo("서초역 1번 출구");
        assertThat(foundReport.getLatitude()).isEqualTo(new BigDecimal("37.483569"));
        assertThat(foundReport.getLongitude()).isEqualTo(new BigDecimal("127.032675"));
    }

    @Test
    @DisplayName("findWithImagesById: 목격신고글과 이미지들을 함께 조회한다")
    void findWitnessReportWithImages() {
        // Given
        WitnessReport witnessReport = WitnessReport.createWitnessReport(
                "골든 리트리버", "개", ReportTag.WITNESS, LocalDate.of(2024, 1, 20),
                "서울시 서초구 서초대로 456", testUser, "금색", "목줄을 하고 있었음",
                "이영희", "서초역 1번 출구",
                new BigDecimal("37.483569"), new BigDecimal("127.032675")
        );
        witnessReportRepository.save(witnessReport);
        em.flush();

        ReportImage image1 = ReportImage.createReportImage("https://witness1.jpg", "uuid-w1");
        ReportImage image2 = ReportImage.createReportImage("https://witness2.jpg", "uuid-w2");

        image1.setReport(witnessReport);
        image2.setReport(witnessReport);

        reportImageRepository.save(image1);
        reportImageRepository.save(image2);
        em.flush();
        em.clear();

        // When
        WitnessReport foundReport = witnessReportRepository.findWithImagesById(witnessReport.getId())
                .orElseThrow();

        // Then
        assertThat(foundReport.getReportImages()).hasSize(2);
        assertThat(foundReport.getReportImagesUrlList())
                .containsExactlyInAnyOrder("https://witness1.jpg", "https://witness2.jpg");
    }


}