package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class InterestReportServiceTest {

    @InjectMocks
    private InterestReportServiceImpl interestReportService;

    @Mock
    private InterestReportRepository interestReportRepository;

    @Mock
    private CardFactory cardFactory;

    @Captor
    private ArgumentCaptor<List<ReportProjection>> projectionsCaptor;
    @Captor
    private ArgumentCaptor<Set<Long>> idsCaptor;
    @Captor
    private ArgumentCaptor<Long> nextLastIdCaptor;
    @Captor
    private ArgumentCaptor<Boolean> isLastCaptor;

    @DisplayName("페이지 사이즈보다 많은 관심글이 존재하면 사이즈에 맞춰서 보여준다")
    @Test
    void should_TrimInterestAnimals_When_NumberOfThemExceedsPageSize() {
        // given
        final Long userId = 1L;
        final Long lastId = Long.MAX_VALUE;
        final int size = 20;

        List<ReportProjection> projections = getReportProjections(size + 1);
        when(interestReportRepository.findInterestReportsByCursor(
                eq(userId), eq(lastId), argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == size + 1)
        )).thenReturn(projections);

        CardResponseDTO expectedResponse = new CardResponseDTO(List.of(), -1L, false);
        when(cardFactory.createCardResponse(anyList(), anySet(), anyLong(), anyBoolean()))
                .thenReturn(expectedResponse);

        // when
        CardResponseDTO actualResponse = interestReportService.retrieveInterestAnimals(userId, lastId, size);

        // then
        verify(interestReportRepository).findInterestReportsByCursor(eq(userId), eq(lastId), any(PageRequest.class));

        verify(cardFactory).createCardResponse(
                projectionsCaptor.capture(),
                idsCaptor.capture(),
                nextLastIdCaptor.capture(),
                isLastCaptor.capture()
        );
        assertThat(projectionsCaptor.getValue()).hasSize(size);
        assertThat(idsCaptor.getValue()).containsAll(
                projectionsCaptor.getValue().stream().map(ReportProjection::getReportId).toList()
        );
        assertThat(nextLastIdCaptor.getValue()).isEqualTo(projectionsCaptor.getValue().get(size - 1).getReportId());
        assertThat(isLastCaptor.getValue()).isFalse();

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @DisplayName("페이지 사이즈보다 적은 관심글이 존재하면 그대로 반환한다")
    @Test
    void should_ReturnAllOfInterestAnimals_When_NumberOfThemIsSmallerThanPageSize() {
        // given
        final Long userId = 1L;
        final Long lastId = Long.MAX_VALUE;
        final int size = 20;

        List<ReportProjection> projections = getReportProjections(size - 1);
        when(interestReportRepository.findInterestReportsByCursor(
                eq(userId), eq(lastId), argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == size + 1)
        )).thenReturn(projections);

        CardResponseDTO expectedResponse = new CardResponseDTO(List.of(), -1L, true);
        when(cardFactory.createCardResponse(anyList(), anySet(), anyLong(), anyBoolean()))
                .thenReturn(expectedResponse);

        // when
        CardResponseDTO actualResponse = interestReportService.retrieveInterestAnimals(userId, lastId, size);

        // then
        verify(cardFactory).createCardResponse(
                projectionsCaptor.capture(),
                idsCaptor.capture(),
                nextLastIdCaptor.capture(),
                isLastCaptor.capture()
        );

        assertThat(projectionsCaptor.getValue()).hasSize(projections.size());
        assertThat(idsCaptor.getValue()).containsAll(
                projectionsCaptor.getValue().stream().map(ReportProjection::getReportId).toList()
        );
        assertThat(nextLastIdCaptor.getValue()).isEqualTo(projectionsCaptor.getValue().get(projectionsCaptor.getValue().size() - 1).getReportId());
        assertThat(isLastCaptor.getValue()).isTrue();

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    private List<ReportProjection> getReportProjections(int size) {
        return LongStream.iterate(size, n -> n - 1)
                .limit(size)
                .mapToObj(i -> new ReportProjectionImpl(
                        i,
                        "image" + i + ".png",
                        "breed" + i,
                        ReportTag.PROTECTING.getValue(),
                        LocalDate.of(2025, 1, 1),
                        "city"
                ))
                .collect(Collectors.toList());
    }
}
