package com.kuit.findyou.domain.report.service.command;

import com.kuit.findyou.domain.report.model.Report;

public interface DeleteReportService {
    void deleteReport(Long reportId, Long userId);
}
