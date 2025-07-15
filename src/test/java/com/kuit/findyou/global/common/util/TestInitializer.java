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

    public void initializeReportControllerTestData() {
        // 1. 사용자 생성 및 저장
        User user = User.builder()
                .name("홍길동")
                .profileImageUrl("http://example.com/profile.png")
                .kakaoId(123456789L)
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // 2. 보호 글 생성 및 저장
        ProtectingReport protectingReport = ProtectingReport.createProtectingReport(
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
        protectingReportRepository.save(protectingReport);

        // 3. 실종 글 생성 및 저장
        MissingReport missingReport = MissingReport.createMissingReport(
                "포메라니안", "개", ReportTag.MISSING, LocalDate.of(2024, 10, 5),
                "서울시 강남구", user, Sex.F, "RF12345", "3살",
                "3kg", "흰색", "눈 주변 갈색 털",
                "이슬기", "010-1111-2222", "강남역 10번 출구",
                BigDecimal.valueOf(37.501), BigDecimal.valueOf(127.025)
        );
        missingReportRepository.save(missingReport);

        // 4. 목격 글 생성 및 저장
        WitnessReport witnessReport = WitnessReport.createWitnessReport(
                "진돗개", "개", ReportTag.WITNESS, LocalDate.of(2024, 8, 10),
                "부산시 해운대구", user, "하얀 털", "목줄 없음",
                "신성훈", "해변가",
                BigDecimal.valueOf(35.158), BigDecimal.valueOf(129.16)
        );
        witnessReportRepository.save(witnessReport);

        // 5. 이미지 생성 및 저장
        ReportImage image1 = ReportImage.createReportImage("https://img.com/1.png", "uuid-1");
        image1.setReport(protectingReport);
        reportImageRepository.save(image1);

        ReportImage image2 = ReportImage.createReportImage("https://img.com/missing.png", "uuid-m");
        image2.setReport(missingReport);
        reportImageRepository.save(image2);

        ReportImage image3 = ReportImage.createReportImage("https://img.com/witness.png", "uuid-w");
        image3.setReport(witnessReport);
        reportImageRepository.save(image3);

        // 6. 관심 글 생성 및 저장
        InterestReport interest1 = InterestReport.createInterestReport(user, protectingReport);
        interestReportRepository.save(interest1);

        InterestReport interest2 = InterestReport.createInterestReport(user, missingReport);
        interestReportRepository.save(interest2);

        InterestReport interest3 = InterestReport.createInterestReport(user, witnessReport);
        interestReportRepository.save(interest3);
    }

}

