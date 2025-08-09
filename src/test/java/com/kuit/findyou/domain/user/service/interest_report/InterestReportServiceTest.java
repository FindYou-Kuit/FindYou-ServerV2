package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.Card;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @DisplayName("페이지 사이즈보다 많은 관심글이 존재하면 첫 조회 시에 마지막 페이지가 반환하지 않는다")
    @Test
    void should_NotReturnLastPage_When_NumberOfInterestAnimalsExceedsPageSize(){
        // given
        final Long userId = 1L;
        final Long lastId = Long.MAX_VALUE;
        final int size = 20;

        List<ReportProjection> projections = getReportProjections(size + 1);
        when(interestReportRepository.findInterestReportsByCursor(eq(userId), eq(lastId), eq(PageRequest.of(0, size + 1))))
                .thenReturn(projections);

        List<ReportProjection> takenWithSize = projections.subList(0, size);
        Set<Long> interestIds = takenWithSize.stream().map(p -> p.getReportId()).collect(Collectors.toSet());
        List<Card> cards = getCards(takenWithSize, interestIds);
        Long nextLastId = cards.get(cards.size() - 1).reportId();
        CardResponseDTO responseDTO = new CardResponseDTO(cards, nextLastId, true);
        when(cardFactory.createCardResponse(takenWithSize, interestIds, nextLastId, true)).thenReturn(responseDTO);

        // when
        CardResponseDTO response = interestReportService.retrieveInterestAnimals(userId, lastId, size);

        // then
        assertThat(response).isEqualTo(responseDTO);
        verify(interestReportRepository, times(1)).findInterestReportsByCursor(eq(userId), eq(lastId), eq(PageRequest.of(0, size + 1)));
        verify(cardFactory, times(1)).createCardResponse(eq(takenWithSize), eq(interestIds), eq(nextLastId), eq(true));
    }

    private List<Card> getCards(List<ReportProjection> takenWithSize, Set<Long> interestIds) {
        return takenWithSize.stream().map(p -> new Card(
                        p.getReportId(),
                        p.getThumbnailImageUrl(),
                        p.getTitle(),
                        p.getTag(),
                        p.getDate().toString(),
                        p.getAddress(),
                        interestIds.contains(p.getReportId())))
                .collect(Collectors.toList());
    }

    private List<ReportProjection> getReportProjections(int size) {
        List<ReportProjection> projections = LongStream.iterate(size, n -> n - 1)
                .limit(size)
                .mapToObj(i -> new ReportProjectionImpl(i, "image" + i + ".png", "breed" + i, ReportTag.PROTECTING.getValue(), LocalDate.of(2025, 1, 1), "city"))
                .collect(Collectors.toList());
        return projections;
    }

    @DisplayName("페이지 사이즈보다 적은 관심글이 존재하면 첫 조회 시에 마지막 페이지를 반환한다")
    @Test
    void should_ReturnLastPage_When_NumberOfInterestAnimalsIsSmallerThanPageSize(){
        // given
        final Long userId = 1L;
        final Long lastId = Long.MAX_VALUE;
        final int size = 20;

        List<ReportProjection> projections = getReportProjections(size - 1);
        when(interestReportRepository.findInterestReportsByCursor(userId, lastId, PageRequest.of(0, size + 1)))
                .thenReturn(projections);

        List<ReportProjection> takenWithSize = projections.subList(0, size);
        Set<Long> interestIds = takenWithSize.stream().map(p -> p.getReportId()).collect(Collectors.toSet());
        List<Card> cards = getCards(takenWithSize, interestIds);
        Long nextLastId = cards.get(cards.size() - 1).reportId();
        CardResponseDTO responseDTO = new CardResponseDTO(cards, nextLastId, true);
        when(cardFactory.createCardResponse(takenWithSize, interestIds, nextLastId, true)).thenReturn(responseDTO);

        // when
        CardResponseDTO response = interestReportService.retrieveInterestAnimals(userId, lastId, size);

        // then
        assertThat(response).isEqualTo(responseDTO);
        verify(interestReportRepository, times(1)).findInterestReportsByCursor(eq(userId), eq(lastId), eq(PageRequest.of(0, size + 1)));
        verify(cardFactory, times(1)).createCardResponse(eq(takenWithSize), eq(interestIds), eq(nextLastId), eq(true));
    }
}