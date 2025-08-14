package com.kuit.findyou.domain.user.service.viewed_reports;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.factory.CardFactory;
import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ViewedReport;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.repository.ReportRepository;
import com.kuit.findyou.domain.report.repository.ViewedReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
class ViewedReportsRetrieveServiceImplTest {

    @Mock
    private ViewedReportRepository viewedReportRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private InterestReportRepository interestReportRepository;

    @Mock
    private CardFactory cardFactory;


    @InjectMocks
    private ViewedReportsRetrieveServiceImpl viewedReportsRetrieveService;

    @Test
    @DisplayName("최근 본 동물 리스트 조회")
    void retrieveViewedAnimals_success() {
        // given
        Long userId = 1L;
        Long lastId = 100L;

        Report report1 = mock(Report.class);
        when(report1.getId()).thenReturn(101L);
        Report report2 = mock(Report.class);
        when(report2.getId()).thenReturn(102L);


        ViewedReport vr1 = mock(ViewedReport.class);
        when(vr1.getReport()).thenReturn(report1);

        ViewedReport vr2 = mock(ViewedReport.class);
        when(vr2.getId()).thenReturn(9L); // findLastId 할 때 사용됨
        when(vr2.getReport()).thenReturn(report2);

        List<ViewedReport> viewedReports = List.of(vr1, vr2);
        SliceImpl<ViewedReport> slice = new SliceImpl<>(viewedReports, PageRequest.of(0, 20), false);

        when(viewedReportRepository.findByUserIdAndIdLessThanOrderByIdDesc(eq(userId), eq(lastId), any()))
                .thenReturn(slice);

        // 최근에 본 것
        ReportProjection projection1 = mock(ReportProjection.class);
        when(projection1.getReportId()).thenReturn(101L);

        // 이전에 본 것
        ReportProjection projection2 = mock(ReportProjection.class);
        when(projection2.getReportId()).thenReturn(102L);

        when(reportRepository.findReportProjectionsByIdIn(List.of(101L, 102L)))
                .thenReturn(List.of(projection1, projection2));

        CardResponseDTO dummyResponse = mock(CardResponseDTO.class);

        when(cardFactory.createCardResponse(anyList(), anySet(), eq(9L), eq(true)))
                .thenReturn(dummyResponse);

        when(interestReportRepository.findInterestedReportIdsByUserIdAndReportIds(eq(userId), anyList())).thenReturn(List.of(101L));

        // when
        CardResponseDTO result = viewedReportsRetrieveService.retrieveViewedAnimals(lastId, userId);

        // then
        assertThat(result).isEqualTo(dummyResponse);

        verify(viewedReportRepository).findByUserIdAndIdLessThanOrderByIdDesc(userId, lastId, PageRequest.of(0, 20));
        verify(reportRepository).findReportProjectionsByIdIn(List.of(101L, 102L));
        verify(cardFactory).createCardResponse(anyList(), anySet(), eq(9L), eq(true));

    }
}

