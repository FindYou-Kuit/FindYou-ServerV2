package com.kuit.findyou.domain.report.service.facade;

import com.kuit.findyou.domain.report.dto.request.CreateWitnessReportRequest;
import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;
import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.request.RetrieveReportRequestDTO;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.service.command.CreateWitnessReportService;
import com.kuit.findyou.domain.report.service.command.CreateMissingReportService;
import com.kuit.findyou.domain.report.service.command.DeleteReportService;
import com.kuit.findyou.domain.report.service.detail.ReportDetailService;
import com.kuit.findyou.domain.report.service.retrieve.ReportRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReportServiceFacade {

    private final ReportDetailService reportDetailService;
    private final ReportRetrieveService reportRetrieveService;
    private final CreateWitnessReportService createWitnessReportService;
    private final CreateMissingReportService createMissingReportService;
    private final DeleteReportService deleteReportService;

    public <DTO_TYPE> DTO_TYPE getReportDetail(
            ReportTag tag,
            Long reportId,
            Long userId
    ) {
        return reportDetailService.getReportDetail(tag, reportId, userId);
    }

    public CardResponseDTO retrieveReportsWithFilters(
            RetrieveReportRequestDTO request,
            Long userId
    ) {
        return reportRetrieveService.retrieveReportsWithFilters(request, userId);
    }

    public void createMissingReport(CreateMissingReportRequest req, Long userId) {
        createMissingReportService.createMissingReport(req, userId);
    }

    public void createWitnessReport(CreateWitnessReportRequest req, Long userId) {
        createWitnessReportService.createWitnessReport(req, userId);
    }

    public void deleteReport(Long reportId, Long userId) {
        deleteReportService.deleteReport(reportId, userId);
    }
}


