package com.kuit.findyou.domain.report.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportProjection {
    Long getReportId();
    String getThumbnailImageUrl();
    String getTitle();
    String getTag();
    LocalDate getDate();
    LocalDateTime getCreatedAt();
    String getAddress();
}
