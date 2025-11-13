package com.kuit.findyou.domain.user.service.interest_report;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
public class ReportProjectionImpl implements ReportProjection {
    private final Long reportId;
    private final String thumbnailImageUrl;
    private final String breed;
    private final String tag;
    private final LocalDate date;
    private final String address;

    public Long getReportId() { return reportId; }
    public String getThumbnailImageUrl() { return thumbnailImageUrl; }
    public String getTitle() { return breed; }
    public String getTag() { return tag; }
    public LocalDate getDate() { return date; }
    public String getAddress() { return address; }
}