package com.kuit.findyou.domain.report.repository;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.user.model.User;
import com.kuit.findyou.domain.user.repository.UserRepository;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({ TestInitializer.class, DatabaseCleaner.class })
@Transactional
@ActiveProfiles("test")
class ReportRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MissingReportRepository missingReportRepository;

    @Autowired
    private WitnessReportRepository witnessReportRepository;

    @Autowired
    private ProtectingReportRepository protectingReportRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private TestInitializer testInitializer;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void init() {
        databaseCleaner.execute();
    }

    @Test
    @DisplayName("모든 Report 조회 테스트 (다형성)")
    void findAllReportsTest() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);

        // When
        List<Report> allReports = reportRepository.findAll();

        // Then
        assertThat(allReports).hasSize(3);
        assertThat(allReports.get(0)).isInstanceOf(MissingReport.class);
        assertThat(allReports.get(1)).isInstanceOf(WitnessReport.class);
        assertThat(allReports.get(2)).isInstanceOf(ProtectingReport.class);
    }

    @Test
    @DisplayName("태그별 조회 (다형성)")
    void findReportsByTagTest() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);

        // when & then

        List<Report> missingReports = reportRepository.findByTag(ReportTag.MISSING);
        assertThat(missingReports).hasSize(1);
        assertThat(missingReports.get(0)).isInstanceOf(MissingReport.class);

        List<Report> witnessReports = reportRepository.findByTag(ReportTag.WITNESS);
        assertThat(witnessReports).hasSize(1);
        assertThat(witnessReports.get(0)).isInstanceOf(WitnessReport.class);

        List<Report> protectingReports = reportRepository.findByTag(ReportTag.PROTECTING);
        assertThat(protectingReports).hasSize(1);
        assertThat(protectingReports.get(0)).isInstanceOf(ProtectingReport.class);

    }

    @Test
    @DisplayName("부모 Repository로 Report 삭제 테스트 (다형성)")
    void deleteReportByParentRepositoryTest() {
        // Given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);

        List<Report> allReports = reportRepository.findAll();
        assertThat(allReports).hasSize(3);

        Report reportToDelete = allReports.get(0); // MissingReport
        Long reportId = reportToDelete.getId();

        // When - 부모 Repository로 삭제
        reportRepository.deleteById(reportId);
        em.flush();
        em.clear();

        // Then
        List<Report> remainingReports = reportRepository.findAll();
        assertThat(remainingReports).hasSize(2);
        assertThat(reportRepository.findById(reportId)).isEmpty();

        // 삭제된 것이 MissingReport였는지 확인
        List<Report> missingReports = reportRepository.findByTag(ReportTag.MISSING);
        assertThat(missingReports).isEmpty();
    }

    @Test
    @DisplayName("모든 Report 삭제 테스트")
    void deleteAllReportsTest() {
        // Given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);
        assertThat(reportRepository.findAll()).hasSize(3);

        // When
        reportRepository.deleteAll();
        em.flush();
        em.clear();

        // Then
        assertThat(reportRepository.findAll()).isEmpty();
        assertThat(missingReportRepository.findAll()).isEmpty();
        assertThat(witnessReportRepository.findAll()).isEmpty();
        assertThat(protectingReportRepository.findAll()).isEmpty();

        // User는 여전히 존재해야 함
        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    @DisplayName("필터 조건에 따른 ReportProjection 조회 테스트")
    void findReportsWithFiltersTest() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);

        List<ReportTag> tags = List.of(ReportTag.MISSING, ReportTag.WITNESS, ReportTag.PROTECTING);
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(1);
        String species = "개";
        List<String> breedList = List.of("골든 리트리버", "믹스견");
        String address = "서울";
        Long lastReportId = Long.MAX_VALUE; // 가장 큰 값부터 조회 시작

        // when
        Slice<ReportProjection> result = reportRepository.findReportsWithFilters(
                tags, startDate, endDate, species, breedList, address, lastReportId,
                PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent()).hasSize(2); // 골든 리트리버 + 믹스견
        List<String> titles = result.getContent().stream()
                .map(ReportProjection::getTitle)
                .toList();

        assertThat(titles).containsExactlyInAnyOrder("골든 리트리버", "믹스견");
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("태그 필터 없이 전체 조회")
    void findAllReportsWithoutTagFilter() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);

        // when
        Slice<ReportProjection> result = reportRepository.findReportsWithFilters(
                null, null, null, null, null, null, Long.MAX_VALUE, PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("신고 동물 조회 - MISSING, WITNESS 만 조회")
    void retrieveReportingAnimals() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);
        List<ReportTag> tags = List.of(ReportTag.MISSING, ReportTag.WITNESS);

        // when
        Slice<ReportProjection> result = reportRepository.findReportsWithFilters(
                tags, null, null, null, null, null, Long.MAX_VALUE, PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allSatisfy(projection ->
                assertThat(projection.getTag()).isIn(ReportTag.MISSING.toString(), ReportTag.WITNESS.toString())
        );
    }

    @Test
    @DisplayName("구조 동물 조회 - PROTECTING 만 조회")
    void retrieveProtectingAnimals() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);
        List<ReportTag> tags = List.of(ReportTag.PROTECTING);

        // when
        Slice<ReportProjection> result = reportRepository.findReportsWithFilters(
                tags, null, null, null, null, null, Long.MAX_VALUE, PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).allSatisfy(projection ->
                assertThat(projection.getTag()).isEqualTo(ReportTag.PROTECTING.toString())
        );
    }

    @Test
    @DisplayName("startDate 이후만 조회")
    void filterByStartDate() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);
        LocalDate start = LocalDate.now().minusDays(2);

        // when
        Slice<ReportProjection> result = reportRepository.findReportsWithFilters(
                null, start, null, null, null, null, Long.MAX_VALUE, PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("페르시안");
    }

    @Test
    @DisplayName("species = '고양이' 필터")
    void filterBySpecies() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);

        // when
        Slice<ReportProjection> result = reportRepository.findReportsWithFilters(
                null, null, null, "고양이", null, null, Long.MAX_VALUE, PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("페르시안");
    }

    @Test
    @DisplayName("breedList = [믹스견] 필터")
    void filterByBreed() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);

        // when
        Slice<ReportProjection> result = reportRepository.findReportsWithFilters(
                null, null, null, null, List.of("믹스견"), null, Long.MAX_VALUE, PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("믹스견");
    }

    @Test
    @DisplayName("address = '서초구' 필터")
    void filterByAddress() {
        // given
        User user = testInitializer.createTestUser();
        testInitializer.createTestReports(user);

        // when
        Slice<ReportProjection> result = reportRepository.findReportsWithFilters(
                null, null, null, null, null, "서초구", Long.MAX_VALUE, PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAddress()).contains("서초구");
    }


    @Test
    @DisplayName("위도 경도를 이용하여 거리 순으로 신고글 조회에 성공한다")
    void should_ReturnReportsSortedByDistance_When_GivenLatLngAndTags(){
        // given
        User user = testInitializer.createTestUser();
        Report reportA = testInitializer.createReportByLatLngAndTag(user,37.5665, 126.9780, ReportTag.PROTECTING); // 서울
        Report reportB = testInitializer.createReportByLatLngAndTag(user,35.1796, 129.0756, ReportTag.PROTECTING); // 부산
        Report reportC = testInitializer.createReportByLatLngAndTag(user,33.4996, 126.5312, ReportTag.PROTECTING); // 제주
        em.flush();

        Double searchLat = 37.5665; // 기준점은 서울
        Double searchLng = 126.9780;
        List<ReportTag> tags = List.of(ReportTag.PROTECTING);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<ReportProjection> results = reportRepository.findNearestReports(searchLat, searchLng, tags, pageable);

        // then
        assertThat(results).hasSize(3);

        List<Long> sortedIds = results.stream().map(ReportProjection::getReportId).toList();
        assertThat(sortedIds).containsExactly(reportA.getId(), reportB.getId(), reportC.getId()); // 거리 순 정렬 검증
    }

    @DisplayName("사용자가 신고한 글이 있으면 반환한다")
    @Test
    void shouldReturnUserReports_WhenUserHasThem(){
        // given
        User user = testInitializer.createTestUser();
        final int size = 20;
        final long lastId = Long.MAX_VALUE;
        IntStream.rangeClosed(1, size + 1).forEach(i -> {
            testInitializer.createTestWitnessReportWithImage(user);
        });

        // when
        Slice<ReportProjection> slices = reportRepository.findUserReportsByCursor(user.getId(), lastId, PageRequest.of(0, size));

        // then
        assertThat(slices.getContent()).hasSize(size);
        assertThat(slices.hasNext()).isTrue();
        ReportProjection firstReport = slices.getContent().get(0);
        assertThat(firstReport.getReportId()).isEqualTo(21L);
        ReportProjection lastReport = slices.getContent().get(slices.getNumberOfElements() - 1);
        assertThat(lastReport.getReportId()).isEqualTo(2L);
    }
}