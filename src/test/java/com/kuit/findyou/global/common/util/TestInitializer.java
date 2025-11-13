package com.kuit.findyou.global.common.util;

import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.domain.city.model.Sido;
import com.kuit.findyou.domain.city.model.Sigungu;
import com.kuit.findyou.domain.city.repository.SidoRepository;
import com.kuit.findyou.domain.city.repository.SigunguRepository;
import com.kuit.findyou.domain.image.model.ReportImage;
import com.kuit.findyou.domain.image.repository.ReportImageRepository;
import com.kuit.findyou.domain.information.model.AnimalDepartment;
import com.kuit.findyou.domain.information.repository.AnimalDepartmentRepository;
import com.kuit.findyou.domain.information.model.AnimalCenter;
import com.kuit.findyou.domain.information.model.VolunteerWork;
import com.kuit.findyou.domain.information.repository.AnimalCenterRepository;
import com.kuit.findyou.domain.information.repository.VolunteerWorkRepository;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.report.repository.*;
import com.kuit.findyou.domain.user.constant.DefaultProfileImage;
import com.kuit.findyou.domain.user.model.Role;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

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
    private final AnimalCenterRepository animalCenterRepository;
    private final AnimalDepartmentRepository animalDepartmentRepository;
    private final VolunteerWorkRepository volunteerWorkRepository;
    private final BreedRepository breedRepository;
    private final SidoRepository sidoRepository;
    private final SigunguRepository sigunguRepository;

    private User defaultUser;

    @Transactional
    public User userWith3InterestReportsAnd2ViewedReports() {
        User testUser = createTestUser();
        defaultUser = createTestUser();

        ProtectingReport testProtectingReport = createTestProtectingReportWithImage(testUser);
        MissingReport testMissingReport = createTestMissingReportWithImage(testUser);
        WitnessReport testWitnessReport = createTestWitnessReportWithImage(testUser);

        createTestInterestReport(testUser, testProtectingReport);
        createTestInterestReport(testUser, testMissingReport);
        createTestInterestReport(testUser, testWitnessReport);

        createTestViewedReport(testUser, testProtectingReport);
        createTestViewedReport(testUser, testMissingReport);

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

        ReportImage image = ReportImage.createReportImage("https://img.com/1.png", report);
        reportImageRepository.save(image);

        return report;
    }

    public MissingReport createTestMissingReportWithImage(User user) {
        MissingReport report = MissingReport.builder()
                .breed("포메라니안")
                .species("개")
                .tag(ReportTag.MISSING)
                .date(LocalDate.of(2024, 10, 5))
                .address("서울시 강남구")
                .user(user)
                .sex(Sex.F)
                .rfid("RF12345")
                .age("3")
                .furColor("흰색")
                .significant("눈 주변 갈색 털")
                .landmark("강남역 10번 출구")
                .latitude(BigDecimal.valueOf(37.501))
                .longitude(BigDecimal.valueOf(127.025))
                .reporterName("이슬기")
                .reporterTel("010-1111-2222")
                .build();

        if (user != null) {
            user.addReport(report);
        }
        missingReportRepository.save(report);

        ReportImage image = ReportImage.createReportImage("https://img.com/missing.png", report);
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

        ReportImage image = ReportImage.createReportImage("https://img.com/witness.png", report);
        reportImageRepository.save(image);

        return report;
    }

    public InterestReport createTestInterestReport(User user, Report report) {
        InterestReport interest = InterestReport.createInterestReport(user, report);
        return interestReportRepository.save(interest);
    }

    public ViewedReport createTestViewedReport(User user, Report report) {
        ViewedReport viewedReport = ViewedReport.createViewedReport(user, report);
        return viewedReportRepository.save(viewedReport);
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

    private void createTestAnimalCenters() {
        AnimalCenter center1 = AnimalCenter.builder().name("서울시보호소").jurisdiction("서울특별시 강남구").phoneNumber("02-123-4567")
                .address("서울시 강남구 테헤란로 1길").latitude(37.5).longitude(127.1).build();

        AnimalCenter center2 = AnimalCenter.builder().name("행복동물병원").jurisdiction("서울특별시 강남구,서울특별시 서초구").phoneNumber("02-999-9999")
                .address("서울시 강남구 봉은사로 3길").latitude(37.51).longitude(127.11).build();

        AnimalCenter center3 = AnimalCenter.builder().name("부산동물보호소").jurisdiction("부산광역시 해운대구").phoneNumber("051-123-4567")
                .address("부산시 해운대구 해운대로 55").latitude(35.1).longitude(129.1).build();

        animalCenterRepository.saveAll(List.of(center1, center2, center3));
    }

    public User setupAnimalCenterTestData() {
        User centerUser = createTestUser();
        defaultUser = centerUser;
        createTestAnimalCenters();
        return centerUser;
    }

    public void createTestVolunteerWorks(int number) {
        IntStream.rangeClosed(1, number).forEach(i -> {
            VolunteerWork volunteerWork = VolunteerWork.builder()
                    .institution("보호센터" + i)
                    .recruitmentStartDate(LocalDate.of(2025, 1, 1))
                    .recruitmentEndDate(LocalDate.of(2025, 1, 2))
                    .address("서울시")
                    .volunteerStartAt(LocalDateTime.of(2025, 1, 3, 5, 0))
                    .volunteerEndAt(LocalDateTime.of(2025, 1, 3, 6, 0))
                    .webLink("www.web.link")
                    .registerNumber(String.valueOf(i))
                    .runId((long) i)
                    .build();

            volunteerWorkRepository.save(volunteerWork);
        });
    }

    public void createTestAnimalDepartments(String organization, int count) {
        for (int i = 1; i <= count; i++) {
            animalDepartmentRepository.save(
                    AnimalDepartment.builder()
                            .organization(organization)
                            .department("테스트부서" + i)
                            .phoneNumber("02-0000-" + String.format("%03d", i))
                            .build()
            );
        }
    }

    public void createTestDepartment(String organization, String department, String phoneNumber) {
        animalDepartmentRepository.save(
                AnimalDepartment.builder()
                        .organization(organization)
                        .department(department)
                        .phoneNumber(phoneNumber)
                        .build()
        );
    }

    public void createTestReports(User user) {
        MissingReport missingReport = MissingReport.createMissingReport(
                "골든 리트리버",
                "개",
                ReportTag.MISSING,
                LocalDate.now().minusDays(5),
                "서울시 강남구",
                user,
                Sex.M,
                "RFID123456",
                "3",
                "황금색",
                "목에 빨간 목걸이",
                "김철수",
                new BigDecimal("37.497952"),
                new BigDecimal("127.027619")
        );


        WitnessReport witnessReport = WitnessReport.createWitnessReport(
                "믹스견",
                "개",
                ReportTag.WITNESS,
                LocalDate.now().minusDays(3),
                "서울시 서초구",
                user,
                "검은색",
                "오른쪽 다리 절뚝임",
                "이영희",
                "서초역 2번 출구",
                new BigDecimal("37.483569"),
                new BigDecimal("127.032455")
        );


        ProtectingReport protectingReport = ProtectingReport.createProtectingReport(
                "페르시안",
                "고양이",
                ReportTag.PROTECTING,
                LocalDate.now().minusDays(1),
                "서울시 마포구 월드컵북로 212",
                user,
                Sex.F,
                "2",
                "4",
                "흰색",
                Neutering.Y,
                "왼쪽 귀에 상처",
                "마포대교 근처",
                "NOTICE-2024-001",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "마포구 동물보호센터",
                "02-123-4567",
                "마포구청",
                new BigDecimal("37.483569"),
                new BigDecimal("127.032675")
        );


        missingReportRepository.save(missingReport);
        witnessReportRepository.save(witnessReport);
        protectingReportRepository.save(protectingReport);
    }

    public Report createReportByLatLngAndTag(User testUser, Double lat, Double lng, ReportTag tag) {
        if (tag == ReportTag.MISSING) {
            MissingReport missingReport = MissingReport.createMissingReport(
                    "골든 리트리버",
                    "개",
                    ReportTag.MISSING,
                    LocalDate.now().minusDays(5),
                    "서울시 강남구",
                    testUser,
                    Sex.M,
                    "RFID123456",
                    "3",
                    "황금색",
                    "목에 빨간 목걸이",
                    "김철수",
                    new BigDecimal(lat),
                    new BigDecimal(lng)
            );
            missingReportRepository.save(missingReport);
            return missingReport;
        } else if (tag == ReportTag.WITNESS) {
            WitnessReport witnessReport = WitnessReport.createWitnessReport(
                    "믹스견",
                    "개",
                    ReportTag.WITNESS,
                    LocalDate.now().minusDays(3),
                    "서울시 서초구",
                    testUser,
                    "검은색",
                    "오른쪽 다리 절뚝임",
                    "이영희",
                    "서초역 2번 출구",
                    new BigDecimal(lat),
                    new BigDecimal(lng)
            );
            witnessReportRepository.save(witnessReport);
            return witnessReport;
        } else if (tag == ReportTag.PROTECTING) {
            ProtectingReport protectingReport = ProtectingReport.createProtectingReport(
                    "페르시안",
                    "고양이",
                    ReportTag.PROTECTING,
                    LocalDate.now().minusDays(1),
                    "서울시 마포구 월드컵북로 212",
                    testUser,
                    Sex.F,
                    "2",
                    "4",
                    "흰색",
                    Neutering.Y,
                    "왼쪽 귀에 상처",
                    "마포대교 근처",
                    "NOTICE-2024-001",
                    LocalDate.now(),
                    LocalDate.now().plusDays(14),
                    "마포구 동물보호센터",
                    "02-123-4567",
                    "마포구청",
                    new BigDecimal(lat),
                    new BigDecimal(lng)
            );
            protectingReportRepository.save(protectingReport);
            return protectingReport;
        }
        return null;
    }

    public User userWith3Reports() {
        User writer = createTestUser();

        createTestMissingReportWithImage(writer);
        createTestWitnessReportWithImage(writer);
        createTestWitnessReportWithImage(writer);

        return writer;
    }

    @Transactional
    public void createTestBreeds() {
        List<Breed> breeds = List.of(
                Breed.builder().name("진돗개").species("강아지").build(),
                Breed.builder().name("포메라니안").species("강아지").build(),
                Breed.builder().name("코리안 숏헤어").species("고양이").build(),
                Breed.builder().name("스코티시 폴드").species("고양이").build(),
                Breed.builder().name("기타축종").species("기타").build()
        );
        breedRepository.saveAll(breeds);
    }

    @Transactional
    public void createTestCities() {
        // 서울
        Sido seoul = sidoRepository.save(
                Sido.builder()
                        .name("서울특별시")
                        .build()
        );
        sigunguRepository.save(Sigungu.builder().name("강남구").sido(seoul).build());
        sigunguRepository.save(Sigungu.builder().name("송파구").sido(seoul).build());

        // 부산
        Sido busan = sidoRepository.save(
                Sido.builder()
                        .name("부산광역시")
                        .build()
        );
        sigunguRepository.save(Sigungu.builder().name("해운대구").sido(busan).build());
    }

    public User createUserWithDefaultProfileImage(DefaultProfileImage img) {
        User user = createTestUser();
        user.changeProfileImage(img.getName());
        return userRepository.save(user);
    }

    public User createUserWithUploadedProfileImage(String imageUrl) {
        User user = User.builder()
                .name("홍길동")
                .role(Role.USER)
                .deviceId("device-uploaded")
                .profileImageUrl(imageUrl)
                .build();

        return userRepository.save(user);
    }

    public User createTestGuest() {
        User user = User.builder()
                .name("게스트")
                .profileImageUrl("http://example.com/profile.png")
                .role(Role.GUEST)
                .build();

        return userRepository.save(user);
    }
}
