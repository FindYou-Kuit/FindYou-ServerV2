package com.kuit.findyou.domain.report.strategy;

import com.kuit.findyou.domain.report.model.Report;

public interface ReportDetailStrategy<REPORT_TYPE extends Report, DTO_TYPE> {
    DTO_TYPE getDetail(REPORT_TYPE report);
}
