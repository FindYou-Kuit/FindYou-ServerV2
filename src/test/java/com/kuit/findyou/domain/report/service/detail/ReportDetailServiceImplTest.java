package com.kuit.findyou.domain.report.service.detail;

import com.kuit.findyou.domain.report.dto.response.MissingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.dto.response.ProtectingReportDetailResponseDTO;
import com.kuit.findyou.domain.report.dto.response.WitnessReportDetailResponseDTO;
import com.kuit.findyou.domain.report.model.*;
import com.kuit.findyou.domain.report.repository.InterestReportRepository;
import com.kuit.findyou.domain.report.strategy.ReportDetailStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
class ReportDetailServiceImplTest {

    @Mock
    private InterestReportRepository interestReportRepository;

    @Mock
    private ReportDetailStrategy<ProtectingReport, ProtectingReportDetailResponseDTO> protectingStrategy;

    @Mock
    private ReportDetailStrategy<MissingReport, MissingReportDetailResponseDTO> missingStrategy;

    @Mock
    private ReportDetailStrategy<WitnessReport, WitnessReportDetailResponseDTO> witnessStrategy;

    private ReportDetailServiceImpl reportDetailService;

    @BeforeEach
    void setUp() {
        Map<ReportTag, ReportDetailStrategy<? extends Report, ?>> strategies = Map.of(
                ReportTag.PROTECTING, protectingStrategy,
                ReportTag.MISSING, missingStrategy,
                ReportTag.WITNESS, witnessStrategy
        );

        reportDetailService = new ReportDetailServiceImpl(interestReportRepository, strategies);
    }

    @Test
    @DisplayName("getReportDetail : ProtectingReport 상세 조회 성공")
    void getProtectingReportDetail_success() {
        // given
        Long reportId = 1L;
        Long userId = 1L;

        ProtectingReport dummyReport = mock(ProtectingReport.class);
        ProtectingReportDetailResponseDTO dummyDto = mock(ProtectingReportDetailResponseDTO.class);

        when(dummyReport.getId()).thenReturn(reportId);
        when(protectingStrategy.getReport(reportId)).thenReturn(dummyReport);
        when(interestReportRepository.existsByReportIdAndUserId(reportId, userId)).thenReturn(true);
        when(protectingStrategy.getDetail(dummyReport, true)).thenReturn(dummyDto);

        // when
        ProtectingReportDetailResponseDTO result = reportDetailService.getReportDetail(
                ReportTag.PROTECTING, reportId, userId);

        // then
        assertThat(result).isEqualTo(dummyDto);

        verify(protectingStrategy).getReport(reportId);
        verify(interestReportRepository).existsByReportIdAndUserId(reportId, userId);
        verify(protectingStrategy).getDetail(dummyReport, true);
    }

    @Test
    @DisplayName("getReportDetail : MissingReport 상세 조회 성공")
    void getMissingReportDetail_success() {
        // given
        Long reportId = 10L;
        Long userId = 3L;

        MissingReport dummyReport = mock(MissingReport.class);
        MissingReportDetailResponseDTO dummyDto = mock(MissingReportDetailResponseDTO.class);

        when(dummyReport.getId()).thenReturn(reportId);
        when(missingStrategy.getReport(reportId)).thenReturn(dummyReport);
        when(interestReportRepository.existsByReportIdAndUserId(reportId, userId)).thenReturn(false);
        when(missingStrategy.getDetail(dummyReport, false)).thenReturn(dummyDto);

        // when
        MissingReportDetailResponseDTO result = reportDetailService.getReportDetail(
                ReportTag.MISSING, reportId, userId);

        // then
        assertThat(result).isEqualTo(dummyDto);

        verify(missingStrategy).getReport(reportId);
        verify(interestReportRepository).existsByReportIdAndUserId(reportId, userId);
        verify(missingStrategy).getDetail(dummyReport, false);
    }

    @Test
    @DisplayName("getReportDetail : WitnessReport 상세 조회 성공")
    void getWitnessReportDetail_success() {
        // given
        Long reportId = 99L;
        Long userId = 7L;

        WitnessReport dummyReport = mock(WitnessReport.class);
        WitnessReportDetailResponseDTO dummyDto = mock(WitnessReportDetailResponseDTO.class);

        when(dummyReport.getId()).thenReturn(reportId);
        when(witnessStrategy.getReport(reportId)).thenReturn(dummyReport);
        when(interestReportRepository.existsByReportIdAndUserId(reportId, userId)).thenReturn(true);
        when(witnessStrategy.getDetail(dummyReport, true)).thenReturn(dummyDto);

        // when
        WitnessReportDetailResponseDTO result = reportDetailService.getReportDetail(
                ReportTag.WITNESS, reportId, userId);

        // then
        assertThat(result).isEqualTo(dummyDto);

        verify(witnessStrategy).getReport(reportId);
        verify(interestReportRepository).existsByReportIdAndUserId(reportId, userId);
        verify(witnessStrategy).getDetail(dummyReport, true);
    }


}