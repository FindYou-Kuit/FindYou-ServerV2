package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.request.ReportViewType;
import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;

import java.time.LocalDate;

public interface ReportRetrieveService {

    CardResponseDTO retrieveReportsWithFilters(ReportViewType reportViewType,
                                               LocalDate startDate,
                                               LocalDate endDate,
                                               String species,
                                               String breeds,
                                               String location,
                                               Long lastReportId,
                                               Long userId);


}
