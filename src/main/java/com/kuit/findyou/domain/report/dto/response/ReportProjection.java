package com.kuit.findyou.domain.report.dto.response;

import java.time.LocalDate;

public interface ReportProjection {
    Long getReportId();
    String getThumbnailImageUrl();
    String getTitle();
    String getTag();
    LocalDate getDate();
    String getAddress();
}
