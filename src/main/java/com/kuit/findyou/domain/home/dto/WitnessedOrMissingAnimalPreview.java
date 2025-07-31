package com.kuit.findyou.domain.home.dto;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;

import java.time.LocalDate;

public record WitnessedOrMissingAnimalPreview(
            Long reportId,
            String thumbnailImageUrl,
            String title,
            String tag,
            LocalDate happenDate,
            String careAddress
    ){
    public static WitnessedOrMissingAnimalPreview of(PreviewWithDistance dto){
        return new WitnessedOrMissingAnimalPreview(dto.getReportId(), dto.getThumbnailImageUrl(), dto.getTitle(), dto.getTag(), dto.getDate(), dto.getAddress());
    }
    public static WitnessedOrMissingAnimalPreview of(ReportProjection dto){
        return new WitnessedOrMissingAnimalPreview(dto.getReportId(), dto.getThumbnailImageUrl(), dto.getTitle(), dto.getTag(), dto.getDate(), dto.getAddress());
    }
}