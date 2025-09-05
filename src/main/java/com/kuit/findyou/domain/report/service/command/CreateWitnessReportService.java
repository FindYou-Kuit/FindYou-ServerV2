package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.report.dto.request.CreateWitnessReportRequest;

public interface CreateWitnessReportService {
    void createWitnessReport(CreateWitnessReportRequest req, Long userId);
}
