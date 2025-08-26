package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.response.Card;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
class ReportRetrieveServiceImplTest {

    @Mock
    private CardFactory cardFactory;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private InterestReportRepository interestReportRepository;

    @InjectMocks
    private ReportRetrieveServiceImpl reportRetrieveService;

    @Test
    @DisplayName("필터 조건에 따라 보고서 리스트를 조회하고 관심 여부를 포함한 결과 반환")
    void retrieveReportsWithFilters_success() {
        // given
        Long userId = 1L;
        Long reportId = 100L;

        ReportProjection projection = mock(ReportProjection.class); // stub 없이 생성
        List<ReportProjection> projections = List.of(projection);
        Slice<ReportProjection> reportSlice = new SliceImpl<>(projections, PageRequest.of(0, 20), false);

        when(reportRepository.findReportsWithFilters(
                any(), any(), any(), any(), any(), any(), anyLong(), any()
        )).thenReturn(reportSlice);

        // ✅ createCardResponse 결과 mock 처리
        Card mockCard = new Card(
                reportId,
                "http://example.com/image.jpg",
                "골든 리트리버",
                "MISSING",
                "2025-07-10",
                "서울시 강남구",
                true
        );
        CardResponseDTO mockResponse = new CardResponseDTO(List.of(mockCard), reportId, true);

        when(cardFactory.createCardResponse(
                eq(projections), anySet() , anyLong(), eq(true)
        )).thenReturn(mockResponse);

        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(eq(userId), anyList())).thenReturn(List.of(reportId));

        // when
        CardResponseDTO result = reportRetrieveService.retrieveReportsWithFilters(
                ReportViewType.REPORTING,
                null, null,
                null, null, null,
                Long.MAX_VALUE,
                userId
        );

        // then
        assertThat(result).isEqualTo(mockResponse);

        verify(reportRepository).findReportsWithFilters(
                eq(List.of(ReportTag.MISSING, ReportTag.WITNESS)),
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(Long.MAX_VALUE),
                eq(PageRequest.of(0, 20))
        );

        verify(cardFactory).createCardResponse(
                eq(projections), anySet(), anyLong(), eq(true)
        );

        verify(cardFactory, times(1)).createCardResponse(any(), anySet(), anyLong(), anyBoolean());

    }

    @Test
    @DisplayName("ALL 조회: tags=null, 빈 Slice -> lastId=-1, lastPage=false")
    void retrieveReports_ALL_emptySlice() {
        // given
        Slice<ReportProjection> empty = new SliceImpl<>(List.of(), PageRequest.of(0,20), true); // hasNext=true
        when(reportRepository.findReportsWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), anyLong(), any()
        )).thenReturn(empty);
        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(anyLong(), anyList()))
                .thenReturn(List.of()); // 관심 없음

        CardResponseDTO expected = new CardResponseDTO(List.of(), -1L, false); // lastPage=false (hasNext=true)
        when(cardFactory.createCardResponse(eq(List.of()), eq(Set.of()), eq(-1L), eq(false)))
                .thenReturn(expected);

        // when
        CardResponseDTO result = reportRetrieveService.retrieveReportsWithFilters(
                ReportViewType.ALL, null, null, null, null, null, 0L, 1L
        );

        // then
        assertThat(result).isEqualTo(expected);
        verify(reportRepository).findReportsWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0L), eq(PageRequest.of(0,20))
        );
    }

    @Test
    @DisplayName("PROTECTING 조회: tags=[PROTECTING], hasNext=false -> lastPage=true")
    void retrieveReports_PROTECTING_hasNextFalse() {
        ReportProjection p1 = mock(ReportProjection.class);
        when(p1.getReportId()).thenReturn(10L);
        Slice<ReportProjection> slice = new SliceImpl<>(List.of(p1), PageRequest.of(0,20), false);

        when(reportRepository.findReportsWithFilters(
                eq(List.of(ReportTag.PROTECTING)), any(), any(), any(), any(), any(), anyLong(), any()
        )).thenReturn(slice);
        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(anyLong(), anyList()))
                .thenReturn(List.of()); // 관심 없음

        CardResponseDTO expected = new CardResponseDTO(List.of(), 10L, true);
        when(cardFactory.createCardResponse(eq(List.of(p1)), eq(Set.of()), eq(10L), eq(true)))
                .thenReturn(expected);

        CardResponseDTO res = reportRetrieveService.retrieveReportsWithFilters(
                ReportViewType.PROTECTING, null,null,null,null,null, 0L, 1L
        );

        assertThat(res).isEqualTo(expected);
        verify(reportRepository).findReportsWithFilters(
                eq(List.of(ReportTag.PROTECTING)),
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0L), eq(PageRequest.of(0,20))
        );
    }

    @Test
    @DisplayName("breeds 파싱: '치와와,  진돗개 , , 포메라니안' -> ['치와와','진돗개','포메라니안']")
    void retrieveReports_breedsParsing() {
        ReportProjection p1 = mock(ReportProjection.class);
        when(p1.getReportId()).thenReturn(30L);
        Slice<ReportProjection> slice = new SliceImpl<>(List.of(p1), PageRequest.of(0,20), true);

        when(reportRepository.findReportsWithFilters(
                eq(List.of(ReportTag.MISSING, ReportTag.WITNESS)),
                any(), any(), eq("강아지"),
                eq(List.of("치와와","진돗개","포메라니안")),
                eq("서울"), anyLong(), any()
        )).thenReturn(slice);

        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(anyLong(), anyList()))
                .thenReturn(List.of(30L));

        // hasNext=true → lastPage=false
        CardResponseDTO expected = new CardResponseDTO(List.of(), 30L, false);
        when(cardFactory.createCardResponse(eq(List.of(p1)), eq(Set.of(30L)), eq(30L), eq(false)))
                .thenReturn(expected);

        CardResponseDTO res = reportRetrieveService.retrieveReportsWithFilters(
                ReportViewType.REPORTING,
                LocalDate.of(2025,8,1), LocalDate.of(2025,8,31),
                "강아지",
                "치와와,  진돗개 , , 포메라니안",
                "서울",
                999L, 7L
        );

        assertThat(res).isEqualTo(expected);
        verify(reportRepository).findReportsWithFilters(
                eq(List.of(ReportTag.MISSING, ReportTag.WITNESS)),
                eq(LocalDate.of(2025,8,1)), eq(LocalDate.of(2025,8,31)),
                eq("강아지"),
                eq(List.of("치와와","진돗개","포메라니안")),
                eq("서울"),
                eq(999L),
                eq(PageRequest.of(0,20))
        );
    }

    @Test
    @DisplayName("breeds blank -> null 로 전달")
    void retrieveReports_breedsBlank_becomesNull() {
        Slice<ReportProjection> slice = new SliceImpl<>(List.of(), PageRequest.of(0,20), false);
        when(reportRepository.findReportsWithFilters(
                any(), any(), any(), any(), isNull(), any(), anyLong(), any()
        )).thenReturn(slice);

        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(anyLong(), anyList()))
                .thenReturn(List.of());

        CardResponseDTO expected = new CardResponseDTO(List.of(), -1L, true);
        when(cardFactory.createCardResponse(eq(List.of()), eq(Set.of()), eq(-1L), eq(true)))
                .thenReturn(expected);

        CardResponseDTO res = reportRetrieveService.retrieveReportsWithFilters(
                ReportViewType.REPORTING, null,null, null, "   ", null, 0L, 1L
        );

        assertThat(res).isEqualTo(expected);
        verify(reportRepository).findReportsWithFilters(
                eq(List.of(ReportTag.MISSING, ReportTag.WITNESS)),
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0L), eq(PageRequest.of(0,20))
        );
    }

    @Test
    @DisplayName("관심 보고서가 없으면 빈 Set 전달")
    void retrieveReports_noInterests() {
        ReportProjection p = mock(ReportProjection.class);
        when(p.getReportId()).thenReturn(5L);
        Slice<ReportProjection> slice = new SliceImpl<>(List.of(p), PageRequest.of(0,20), false);

        when(reportRepository.findReportsWithFilters(any(), any(), any(), any(), any(), any(), anyLong(), any()))
                .thenReturn(slice);

        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(anyLong(), anyList()))
                .thenReturn(List.of());

        CardResponseDTO expected = new CardResponseDTO(List.of(), 5L, true);
        when(cardFactory.createCardResponse(eq(List.of(p)), eq(Set.of()), eq(5L), eq(true)))
                .thenReturn(expected);

        CardResponseDTO res = reportRetrieveService.retrieveReportsWithFilters(
                ReportViewType.ALL, null,null, null,null,null, 0L, 77L
        );

        assertThat(res).isEqualTo(expected);
    }




}
