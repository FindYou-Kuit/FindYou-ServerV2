package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.request.RetrieveReportRequestDTO;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;

import java.time.LocalDate;

public interface ReportRetrieveService {

    CardResponseDTO retrieveReportsWithFilters(RetrieveReportRequestDTO request,
                                               Long userId);


}
