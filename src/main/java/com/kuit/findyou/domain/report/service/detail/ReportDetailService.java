package com.kuit.findyou.domain.report.service.detail;

import com.kuit.findyou.domain.report.model.Report;
import com.kuit.findyou.domain.report.model.ReportTag;

public interface ReportDetailService {

    <REPORT_TYPE extends Report, DTO_TYPE> DTO_TYPE getReportDetail(ReportTag tag, Long reportId, Long userId);
}
