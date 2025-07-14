package com.kuit.findyou.domain.report.service.retrieve;

import com.kuit.findyou.domain.report.dto.response.CardResponseDTO;
import com.kuit.findyou.domain.report.model.ReportTag;

import java.time.LocalDate;

public interface ReportRetrieveService {

    public CardResponseDTO retrieveReportsWithFilters(Long lastReportId,
                                                      ReportTag reportTag,
                                                      LocalDate startDate,
                                                      LocalDate endDate,
                                                      String species,
                                                      String breeds,
                                                      String location);


}
