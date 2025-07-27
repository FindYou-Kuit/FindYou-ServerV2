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

    @InjectMocks
    private ReportRetrieveServiceImpl reportRetrieveService;

    @BeforeEach
    void setUp() {
        reportRetrieveService = new ReportRetrieveServiceImpl(reportRepository, cardFactory);
    }

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
                eq(projections), eq(userId), anyLong(), eq(true)
        )).thenReturn(mockResponse);

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
                eq(projections), eq(userId), anyLong(), eq(true)
        );

        verify(cardFactory, times(1)).createCardResponse(any(), anyLong(), anyLong(), anyBoolean());

    }

}
