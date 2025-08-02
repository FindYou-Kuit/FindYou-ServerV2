package com.kuit.findyou.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;


public interface PreviewWithDistance {
    Long getReportId();
    String getThumbnailImageUrl();
    String getTitle();
    String getTag();
    LocalDate getDate();
    String getAddress();
    Double getDistance();
}
