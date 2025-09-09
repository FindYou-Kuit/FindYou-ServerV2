package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.AnimalDepartment;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import com.kuit.findyou.global.common.util.TestInitializer;
import com.kuit.findyou.global.config.TestDatabaseConfig;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Transactional
@Import({DatabaseCleaner.class, TestInitializer.class, TestDatabaseConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AnimalDepartmentRepositoryTest {

    @Autowired
    private AnimalDepartmentRepository repository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestInitializer testInitializer;

    @BeforeEach
    void cleanDatabase(){
        databaseCleaner.execute();
    }


    @DisplayName("페이지 크기보다 많은 보호부서가 존재하면 id ASC 기준으로 페이지 크기만큼 잘라서 반환")
    @Test
    void should_TrimDepartments_When_NumberOfThemExceedsPageSize() {
        // given
        final int size = 20;
        final long lastId = 0L;
        final int excess = 4;

        IntStream.rangeClosed(1, size + excess).forEach(i ->
                testInitializer.createTestDepartment("서울특별시 광진구", "부서" + i, "02-000-" + i));

        // when
        List<AnimalDepartment> rows = repository
                .findAllByIdGreaterThanOrderByIdAsc(lastId, PageRequest.of(0, size));

        // then
        assertThat(rows).hasSize(size);

        List<Long> ids = rows.stream().map(AnimalDepartment::getId).toList();
        assertThat(ids).containsExactlyElementsOf(
                LongStream.rangeClosed(1, size).boxed().toList()
        );
    }


    @DisplayName("페이지 크기보다 적은 보호부서가 존재하면 모두 반환")
    @Test
    void should_ReturnAllDepartments_When_NumberOfThemIsLessThanPageSize() {
        // given
        final int size = 20;
        final long lastId = 0L;
        final int shortfall = 4;

        IntStream.rangeClosed(1, size - shortfall).forEach(i ->
                testInitializer.createTestDepartment("서울특별시 송파구", "부서" + i, "02-1000-" + i));

        // when
        List<AnimalDepartment> rows = repository
                .findAllByIdGreaterThanOrderByIdAsc(lastId, PageRequest.of(0, size));

        // then
        assertThat(rows).hasSize(size - shortfall);

        List<Long> ids = rows.stream().map(AnimalDepartment::getId).toList();
        assertThat(ids).containsExactlyElementsOf(
                LongStream.rangeClosed(1, size - shortfall).boxed().toList()
        );
    }

    @DisplayName("정확하게 일치하는 organization 검색")
    @Test
    void should_ReturnExactMatch_IgnoringCase() {
        // given
        testInitializer.createTestDepartment("서울특별시 송파구", "관광체육과", "02-1111-1111");
        testInitializer.createTestDepartment("서울특별시 강남구", "반려동물복지과", "02-2222-2222");

        // when
        List<AnimalDepartment> hit = repository
                .findAllByOrganizationEqualsAndIdGreaterThanOrderByIdAsc(
                        "서울특별시 송파구", 0L, PageRequest.of(0, 10));

        // then
        assertThat(hit).hasSize(1);
        assertThat(hit).extracting("organization")
                .containsExactly("서울특별시 송파구");
    }

    @DisplayName("AND 토큰(시도 + 시군구 모두 포함)으로 검색 시 강남구는 제외되고 송파구만 반환")
    @Test
    void should_ReturnOnlyRowsThatContain_BothSidoAndSigungu() {
        // given
        testInitializer.createTestDepartment("서울특별시 송파구", "관광체육과", "02-1111-1111");
        testInitializer.createTestDepartment("송파구 서울특별시", "동물보호과", "02-1111-2222"); // 순서 바뀌어도 포함
        testInitializer.createTestDepartment("서울특별시 강남구", "반려동물복지과", "02-2222-2222"); // 제외 대상

        // when
        List<AnimalDepartment> rows = repository
                .findAllByOrganizationContainingAndOrganizationContainingAndIdGreaterThanOrderByIdAsc(
                        "서울특별시", "송파구", 0L, PageRequest.of(0, 10));

        // then
        assertThat(rows).hasSize(2);
        assertThat(rows).extracting("organization")
                .containsExactly("서울특별시 송파구", "송파구 서울특별시");
    }

    @DisplayName("substring 검색은 연속 문자열이 일치하는 행만 반환")
    @Test
    void should_ReturnRowsBySubstring_When_PhraseMatches() {
        // given
        testInitializer.createTestDepartment("서울특별시 송파구", "관광체육과", "02-1111-1111");
        testInitializer.createTestDepartment("송파구 서울특별시", "동물보호과", "02-1111-2222"); // 순서가 달라서 아래 phrase엔 미매칭

        // when
        var phrase = repository
                .findAllByOrganizationContainingAndIdGreaterThanOrderByIdAsc(
                        "서울특별시 송파구", 0L, PageRequest.of(0, 10));

        // then
        assertThat(phrase).hasSize(1);
        assertThat(phrase).extracting("organization")
                .containsExactly("서울특별시 송파구");
    }
}