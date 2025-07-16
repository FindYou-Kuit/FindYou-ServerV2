package com.kuit.findyou.global.common.util;

import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.MissingReportRepository;
import com.kuit.findyou.domain.report.repository.ProtectingReportRepository;
import com.kuit.findyou.domain.report.repository.WitnessReportRepository;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TestInitializer {

    private final UserRepository userRepository;
    private final ProtectingReportRepository protectingReportRepository;
    private final MissingReportRepository missingReportRepository;
    private final WitnessReportRepository witnessReportRepository;
    private final ReportImageRepository reportImageRepository;
    private final InterestReportRepository interestReportRepository;

    @Transactional
    public void initializeReportControllerTestData() {
        User testUser = createTestUser();

        ProtectingReport testProtectingReport = createTestProtectingReportWithImage(testUser);
        MissingReport testMissingReport = createTestMissingReportWithImage(testUser);
        WitnessReport testWitnessReport = createTestWitnessReportWithImage(testUser);

        createTestInterestReport(testUser, testProtectingReport);
        createTestInterestReport(testUser, testMissingReport);
        createTestInterestReport(testUser, testWitnessReport);
    }

    private User createTestUser() {
        User user = User.builder()
                .name("홍길동")
                .profileImageUrl("http://example.com/profile.png")
                .kakaoId(123456789L)
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    private ProtectingReport createTestProtectingReportWithImage(User user) {
        ProtectingReport report = ProtectingReport.createProtectingReport(
                "믹스견", "개", ReportTag.PROTECTING,
                LocalDate.now(), "서울", user,
                Sex.M, "2살", "5kg",
                "갈색", Neutering.Y,
                "절뚝거림", "홍대",
                "NOTICE123", LocalDate.now(),
                LocalDate.now().plusDays(10), "광진보호소",
                "02", "관청",
                BigDecimal.valueOf(37.0), BigDecimal.valueOf(127.0)
        );
        protectingReportRepository.save(report);

        ReportImage image = ReportImage.createReportImage("https://img.com/1.png", "uuid-1");
        image.setReport(report);
        reportImageRepository.save(image);

        return report;
    }

    private MissingReport createTestMissingReportWithImage(User user) {
        MissingReport report = MissingReport.createMissingReport(
                "포메라니안", "개", ReportTag.MISSING, LocalDate.of(2024, 10, 5),
                "서울시 강남구", user, Sex.F, "RF12345", "3살",
                "3kg", "흰색", "눈 주변 갈색 털",
                "이슬기", "010-1111-2222", "강남역 10번 출구",
                BigDecimal.valueOf(37.501), BigDecimal.valueOf(127.025)
        );
        missingReportRepository.save(report);

        ReportImage image = ReportImage.createReportImage("https://img.com/missing.png", "uuid-m");
        image.setReport(report);
        reportImageRepository.save(image);

        return report;
    }

    private WitnessReport createTestWitnessReportWithImage(User user) {
        WitnessReport report = WitnessReport.createWitnessReport(
                "진돗개", "개", ReportTag.WITNESS, LocalDate.of(2024, 8, 10),
                "부산시 해운대구", user, "하얀 털", "목줄 없음",
                "신성훈", "해변가",
                BigDecimal.valueOf(35.158), BigDecimal.valueOf(129.16)
        );
        witnessReportRepository.save(report);

        ReportImage image = ReportImage.createReportImage("https://img.com/witness.png", "uuid-w");
        image.setReport(report);
        reportImageRepository.save(image);

        return report;
    }

    private void createTestInterestReport(User user, Report report) {
        InterestReport interest = InterestReport.createInterestReport(user, report);
        interestReportRepository.save(interest);
    }
}
