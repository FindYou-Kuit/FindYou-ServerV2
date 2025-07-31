package com.kuit.findyou.domain.home.dto;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;

import java.time.LocalDate;

public record ProtectingAnimalPreview(
            Long reportId,
            String thumbnailImageUrl,
            String title,
            String tag,
            LocalDate happenDate,
            String careAddress
    ){
    public static ProtectingAnimalPreview of(PreviewWithDistance dto){
        return new ProtectingAnimalPreview(dto.getReportId(), dto.getThumbnailImageUrl(), dto.getTitle(), dto.getTag(), dto.getDate(), dto.getAddress());
    }
    public static ProtectingAnimalPreview of(ReportProjection dto){
        return new ProtectingAnimalPreview(dto.getReportId(), dto.getThumbnailImageUrl(), dto.getTitle(), dto.getTag(), dto.getDate(), dto.getAddress());
    }
}