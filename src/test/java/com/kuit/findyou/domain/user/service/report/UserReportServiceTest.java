package com.kuit.findyou.domain.user.service.report;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserReportServiceTest {
    @InjectMocks
    private UserReportServiceImpl userReportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private InterestReportRepository interestReportRepository;

    @Mock
    private CardFactory cardFactory;

    @DisplayName("신고글이 있으면 페이지를 응답한다")
    @Test
    void shouldReturnCursorPage_WhenUserReportsExist(){
        // given
        final long userId = 1L;
        final long lastId = 20L;
        final int size = 20;
        final long rpId1 = 1L;

        ReportProjection mockRp1 = mock(ReportProjection.class);
        when(mockRp1.getReportId()).thenReturn(rpId1);

        SliceImpl<ReportProjection> slice = new SliceImpl<>(List.of(mockRp1), PageRequest.of(0, size), false);
        when(reportRepository.findUserReportsByCursor(anyLong(), anyLong(), any())).thenReturn(slice);

        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(anyLong(), anyList())).thenReturn(List.of(rpId1));

        CardResponseDTO response = mock(CardResponseDTO.class);
        when(cardFactory.createCardResponse(anyList(), anySet(), anyLong(), anyBoolean())).thenReturn(response);

        // when
        CardResponseDTO result = userReportService.retrieveUserReports(userId, lastId, size);

        // then
        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Pageable> pageableCap = ArgumentCaptor.forClass(Pageable.class);
        verify(reportRepository).findUserReportsByCursor(eq(userId), eq(lastId), pageableCap.capture());
        assertThat(pageableCap.getValue().getPageSize()).isEqualTo(size);

        ArgumentCaptor<List<Long>> idsCap = ArgumentCaptor.forClass(List.class);
        verify(interestReportRepository).findInterestedReportIdsByUserIdAndReportIds(eq(userId), idsCap.capture());
        assertThat(idsCap.getValue()).containsExactly(rpId1);

        verify(cardFactory).createCardResponse(
                argThat(list -> list.size() == 1 && list.get(0).getReportId().equals(rpId1)),
                argThat((Set<Long> s) -> s.size() == 1 && s.contains(rpId1)),
                eq(rpId1),
                eq(true)
        );
    }

    @DisplayName("신고글이 없으면 빈 페이지를 응답한다")
    @Test
    void shouldReturnEmptyPage_WhenNoUserReportExists() {
        // given
        final long userId = 1L;
        final long lastId = 20L;
        final int size = 20;

        SliceImpl<ReportProjection> slice = new SliceImpl<>(List.of(), PageRequest.of(0, size), false);
        when(reportRepository.findUserReportsByCursor(anyLong(), anyLong(), any())).thenReturn(slice);

        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(anyLong(), anyList())).thenReturn(List.of());

        CardResponseDTO response = mock(CardResponseDTO.class);
        when(cardFactory.createCardResponse(anyList(), anySet(), anyLong(), anyBoolean())).thenReturn(response);

        // when
        CardResponseDTO result = userReportService.retrieveUserReports(userId, lastId, size);

        // then
        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Pageable> pageableCap = ArgumentCaptor.forClass(Pageable.class);
        verify(reportRepository).findUserReportsByCursor(eq(userId), eq(lastId), pageableCap.capture());
        assertThat(pageableCap.getValue().getPageSize()).isEqualTo(size);

        ArgumentCaptor<List<Long>> idsCap = ArgumentCaptor.forClass(List.class);
        verify(interestReportRepository).findInterestedReportIdsByUserIdAndReportIds(eq(userId), idsCap.capture());
        assertThat(idsCap.getValue()).hasSize(0);

        verify(cardFactory).createCardResponse(
                argThat(list -> list.size() == 0),
                argThat((Set<Long> s) -> s.size() == 0),
                eq(-1L),
                eq(true)
        );
    }

    @DisplayName("신고글이 페이지 사이즈보다 많으면 다음 페이지가 존재한다")
    @Test
    void shouldHaveNextPage_WhenNumberOfUserReportsIsLargerThanPageSize(){
        // given
        final long userId = 1L;
        final long lastId = 20L;
        final int size = 10;

        List<ReportProjection> mockRps = LongStream.iterate(lastId - 1, n -> n - 1)
                .limit(size)
                .mapToObj(i -> {
                    ReportProjection mockRp = mock(ReportProjection.class);
                    when(mockRp.getReportId()).thenReturn(i);
                    return mockRp;
                }).collect(Collectors.toList());

        SliceImpl<ReportProjection> slice = new SliceImpl<>(mockRps, PageRequest.of(0, size), true);
        when(reportRepository.findUserReportsByCursor(anyLong(), anyLong(), any())).thenReturn(slice);

        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(anyLong(), anyList())).thenReturn(List.of());

        CardResponseDTO response = mock(CardResponseDTO.class);
        when(cardFactory.createCardResponse(anyList(), anySet(), anyLong(), anyBoolean())).thenReturn(response);

        // when
        CardResponseDTO result = userReportService.retrieveUserReports(userId, lastId, size);

        // then
        assertThat(result).isEqualTo(response);

        ArgumentCaptor<Pageable> pageableCap = ArgumentCaptor.forClass(Pageable.class);
        verify(reportRepository).findUserReportsByCursor(eq(userId), eq(lastId), pageableCap.capture());
        assertThat(pageableCap.getValue().getPageSize()).isEqualTo(size);

        ArgumentCaptor<List<Long>> idsCap = ArgumentCaptor.forClass(List.class);
        verify(interestReportRepository).findInterestedReportIdsByUserIdAndReportIds(eq(userId), idsCap.capture());
        assertThat(idsCap.getValue()).hasSize(size);

        verify(cardFactory).createCardResponse(
                argThat(list -> list.size() == size),
                argThat((Set<Long> s) -> s.size() == 0),
                eq(lastId - size),
                eq(false)
        );
    }
}