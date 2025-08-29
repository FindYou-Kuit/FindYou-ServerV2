package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.report.dto.request.CreateMissingReportRequest;

public interface MissingReportCommandService {
    void createMissingReport(CreateMissingReportRequest req, Long userId);
}
