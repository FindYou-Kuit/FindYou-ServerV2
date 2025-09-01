package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;

public interface CreateMissingReportService {
    void createMissingReport(CreateMissingReportRequest req, Long userId);
}
