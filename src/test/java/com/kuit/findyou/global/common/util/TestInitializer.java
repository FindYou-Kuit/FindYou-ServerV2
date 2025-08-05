package com.kuit.findyou.global.common.util;

import com.kuit.findyou.domain.information.model.AnimalShelter;
import com.kuit.findyou.domain.information.repository.AnimalShelterRepository;
import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.report.repository.*;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestInitializer {

    private final UserRepository userRepository;
    private final ProtectingReportRepository protectingReportRepository;
    private final MissingReportRepository missingReportRepository;
    private final WitnessReportRepository witnessReportRepository;
    private final ReportImageRepository reportImageRepository;
    private final InterestReportRepository interestReportRepository;
    private final ViewedReportRepository viewedReportRepository;
    private final AnimalShelterRepository animalShelterRepository;

    private User reportWriter;
    private User defaultUser;

    @Transactional
    public User userWith3InterestReportsAnd2ViewedReports() {
        User testUser = createTestUser();

        User shelterUser = createTestUser();
        defaultUser = shelterUser;

        ProtectingReport testProtectingReport = createTestProtectingReportWithImage(testUser);
        MissingReport testMissingReport = createTestMissingReportWithImage(testUser);
        WitnessReport testWitnessReport = createTestWitnessReportWithImage(testUser);

        createTestInterestReport(testUser, testProtectingReport);
        createTestInterestReport(testUser, testMissingReport);
        createTestInterestReport(testUser, testWitnessReport);

        createTestViewedReport(testUser, testProtectingReport);
        createTestViewedReport(testUser, testMissingReport);
        createTestAnimalShelters();
        return testUser;
    }

    public User createTestUser() {
        User user = User.builder()
                .name("홍길동")
                .profileImageUrl("http://example.com/profile.png")
                .kakaoId(123456789L)
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public ProtectingReport createTestProtectingReportWithImage(User user) {
        ProtectingReport report = ProtectingReport.createProtectingReport(
                "믹스견", "개", ReportTag.PROTECTING,
                LocalDate.now(), "서울", user,
                Sex.M, "2", "5",
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

    public MissingReport createTestMissingReportWithImage(User user) {
        MissingReport report = MissingReport.createMissingReport(
                "포메라니안", "개", ReportTag.MISSING, LocalDate.of(2024, 10, 5),
                "서울시 강남구", user, Sex.F, "RF12345", "3",
                "3", "흰색", "눈 주변 갈색 털",
                "이슬기", "010-1111-2222", "강남역 10번 출구",
                BigDecimal.valueOf(37.501), BigDecimal.valueOf(127.025)
        );
        missingReportRepository.save(report);

        ReportImage image = ReportImage.createReportImage("https://img.com/missing.png", "uuid-m");
        image.setReport(report);
        reportImageRepository.save(image);

        return report;
    }

    public WitnessReport createTestWitnessReportWithImage(User user) {
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

    public void createTestInterestReport(User user, Report report) {
        InterestReport interest = InterestReport.createInterestReport(user, report);
        interestReportRepository.save(interest);
    }

    public void createTestViewedReport(User user, Report report) {
        ViewedReport viewedReport = ViewedReport.createViewedReport(user, report);
        viewedReportRepository.save(viewedReport);
    }

    public User userWith3InterestAnimals() {
        User testUser = createTestUser();
        User writer = createTestUser();

        ProtectingReport testProtectingReport = createTestProtectingReportWithImage(writer);
        MissingReport testMissingReport = createTestMissingReportWithImage(writer);
        WitnessReport testWitnessReport = createTestWitnessReportWithImage(writer);

        createTestInterestReport(testUser, testProtectingReport);
        createTestInterestReport(testUser, testMissingReport);
        createTestInterestReport(testUser, testWitnessReport);

        return testUser;
    }

    private void createTestAnimalShelters() {
        AnimalShelter shelter1 = AnimalShelter.builder().shelterName("서울시보호소").jurisdiction("서울특별시 강남구").phoneNumber("02-123-4567")
                .address("서울시 강남구 테헤란로 1길").latitude(37.5).longitude(127.1).build();

        AnimalShelter hospital1 = AnimalShelter.builder().shelterName("행복동물병원").jurisdiction("서울특별시 강남구").phoneNumber("02-999-9999")
                .address("서울시 강남구 봉은사로 3길").latitude(37.51).longitude(127.11).build();

        AnimalShelter shelter2 = AnimalShelter.builder().shelterName("부산동물보호소").jurisdiction("부산광역시 해운대구").phoneNumber("051-123-4567")
                .address("부산시 해운대구 해운대로 55").latitude(35.1).longitude(129.1).build();

        animalShelterRepository.saveAll(List.of(shelter1, hospital1, shelter2));
    }
    public User getDefaultUser() {return this.defaultUser;}
}
