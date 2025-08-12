package com.kuit.findyou.domain.information.repository;

import com.kuit.findyou.domain.information.model.VolunteerWork;
import com.kuit.findyou.global.common.util.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(DatabaseCleaner.class)
@ActiveProfiles("test")
class VolunteerWorkRepositoryTest {
    @Autowired
    private VolunteerWorkRepository volunteerWorkRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDatabase(){
        databaseCleaner.execute();
    }

    @DisplayName("페이지 크기보다 많은 봉사활동이 존재하면 페이지 크기만큼 잘라서 반환한다.")
    @Test
    void should_TrimVolunteerWorks_When_NumberOfThemExceedsPageSize(){
        // given
        final int size = 20;
        final long lastId = Long.MAX_VALUE;
        final int excess = 4;

        IntStream.rangeClosed(1, size + excess).forEach(i -> {
            VolunteerWork volunteerWork = getVolunteerWork(i);
            volunteerWorkRepository.save(volunteerWork);
        });

        // when
        List<VolunteerWork> volunteerWorks = volunteerWorkRepository.findAllByIdLessThanOrderByIdDesc(lastId, PageRequest.of(0, size));

        // then
        assertThat(volunteerWorks).hasSize(size);

        List<Long> ids = volunteerWorks.stream().map(VolunteerWork::getId).toList();
        assertThat(ids).containsExactlyElementsOf(
                LongStream.iterate(size + excess, i -> i - 1).limit(size).boxed().toList()
        );
    }

    private static VolunteerWork getVolunteerWork(int i) {
        return VolunteerWork.builder()
                .institution("보호센터" + i)
                .recruitmentStartDate(LocalDate.of(2025, 1, 1))
                .recruitmentEndDate(LocalDate.of(2025, 1, 2))
                .address("서울시")
                .volunteerStartDate(LocalDate.of(2025, 1, 3))
                .volunteerEndDate(LocalDate.of(2025, 1, 4))
                .volunteerStartTime("05:00")
                .volunteerEndTime("06:00")
                .webLink("www.web.link")
                .build();
    }

    @DisplayName("페이지 크기보다 적은 봉사활동이 존재하면 모두 반환한다.")
    @Test
    void should_ReturnAllVolunteerWorks_When_NumberOfThemIsLessThanPageSize(){
        // given
        final int size = 20;
        final long lastId = Long.MAX_VALUE;
        final int shortfall = 4;

        IntStream.rangeClosed(1, size - shortfall).forEach(i -> {
            VolunteerWork volunteerWork = getVolunteerWork(i);
            volunteerWorkRepository.save(volunteerWork);
        });

        // when
        List<VolunteerWork> volunteerWorks = volunteerWorkRepository.findAllByIdLessThanOrderByIdDesc(lastId, PageRequest.of(0, size));

        // then
        assertThat(volunteerWorks).hasSize(size - shortfall);

        List<Long> ids = volunteerWorks.stream().map(VolunteerWork::getId).toList();
        assertThat(ids).containsExactlyElementsOf(
                LongStream.iterate(size - shortfall, i -> i - 1).limit(size - shortfall).boxed().toList()
        );
    }
}