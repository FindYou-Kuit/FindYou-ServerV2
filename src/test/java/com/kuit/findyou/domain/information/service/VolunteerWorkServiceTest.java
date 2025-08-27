package com.kuit.findyou.domain.information.service;

import com.kuit.findyou.domain.information.dto.GetVolunteerWorksResponse;
import com.kuit.findyou.domain.information.model.VolunteerWork;
import com.kuit.findyou.domain.information.repository.VolunteerWorkRepository;
import com.kuit.findyou.domain.information.service.animalShelter.VolunteerWorkServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VolunteerWorkServiceTest {
    @InjectMocks
    private VolunteerWorkServiceImpl volunteerWorkService;
    @Mock
    private VolunteerWorkRepository volunteerWorkRepository;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @DisplayName("남아 있는 봉사활동이 페이지 크기보다 많으면 마지막 페이지가 아니다")
    @Test
    void should_ReturnNonLastPage_When_NumberOfRemainsIsMoreThanPageSize(){
        // given
        final int size = 20;
        final long lastId = 30L;
        List<VolunteerWork> volunteerWorks = LongStream.iterate(lastId - 1, n -> n - 1).limit(size + 1).mapToObj(i -> getVolunteerWork(i)).collect(Collectors.toList());
        when(volunteerWorkRepository.findAllByIdLessThanOrderByIdDesc(anyLong(), any(Pageable.class))).thenReturn(volunteerWorks);

        // when
        GetVolunteerWorksResponse response = volunteerWorkService.getVolunteerWorksByCursor(lastId, size);

        // then
        verify(volunteerWorkRepository).findAllByIdLessThanOrderByIdDesc(eq(lastId), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(response.volunteerWorks()).hasSize(size);
        assertThat(response.lastId()).isEqualTo(lastId - size);
        assertThat(response.isLast()).isFalse();

    }
    private static VolunteerWork getVolunteerWork(long i) {
        return VolunteerWork.builder()
                .id(i)
                .institution("보호센터" + i)
                .recruitmentStartDate(LocalDate.of(2025, 1, 1))
                .recruitmentEndDate(LocalDate.of(2025, 1, 2))
                .address("서울시")
                .volunteerStartAt(LocalDateTime.of(2025, 1, 3, 5, 0))
                .volunteerEndAt(LocalDateTime.of(2025, 1, 4, 6, 0))
                .webLink("www.web.link")
                .build();
    }

    @DisplayName("남아 있는 봉사활동이 페이지 크기보다 적으면 마지막 페이지를 반환한다")
    @Test
    void should_ReturnLastPage_When_NumberOfRemainsIsLessThanPageSize(){
        // given
        final int size = 20;
        final long lastId = 10L;
        List<VolunteerWork> volunteerWorks = LongStream.iterate(lastId - 1, n -> n - 1).limit(lastId - 1).mapToObj(i -> getVolunteerWork(i)).collect(Collectors.toList());
        when(volunteerWorkRepository.findAllByIdLessThanOrderByIdDesc(anyLong(), any(Pageable.class))).thenReturn(volunteerWorks);

        // when
        GetVolunteerWorksResponse response = volunteerWorkService.getVolunteerWorksByCursor(lastId, size);

        // then
        verify(volunteerWorkRepository).findAllByIdLessThanOrderByIdDesc(eq(lastId), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(size + 1);

        assertThat(response.volunteerWorks()).hasSize((int)lastId - 1);
        assertThat(response.lastId()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }
}