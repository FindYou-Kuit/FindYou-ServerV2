package com.kuit.findyou.domain.report.service.facade;

import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;
import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.model.ReportTag;
import com.kuit.findyou.domain.report.service.command.MissingReportCommandService;
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
    private final MissingReportCommandService missingReportCommandService;

    public <DTO_TYPE> DTO_TYPE getReportDetail(
            ReportTag tag,
            Long reportId,
            Long userId
    ) {
        return reportDetailService.getReportDetail(tag, reportId, userId);
    }

    public CardResponseDTO retrieveReportsWithFilters(
            ReportViewType reportViewType,
            LocalDate startDate,
            LocalDate endDate,
            String species,
            String breeds,
            String location,
            Long lastReportId,
            Long userId
    ) {
        return reportRetrieveService.retrieveReportsWithFilters(reportViewType, startDate, endDate, species, breeds, location, lastReportId, userId);
    }

    public void createMissingReport(CreateMissingReportRequest req, Long userId) {
        missingReportCommandService.createMissingReport(req, userId);
    }
}


