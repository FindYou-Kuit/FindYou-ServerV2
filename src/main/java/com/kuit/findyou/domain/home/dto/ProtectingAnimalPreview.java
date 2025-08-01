package com.kuit.findyou.domain.home.dto;

import com.kuit.findyou.domain.report.dto.response.ReportProjection;
import com.kuit.findyou.domain.report.model.ReportTag;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "입양가능동물")
public record ProtectingAnimalPreview(
            @Schema(description = "신고글 식별자", example = "1")
            Long reportId,
            @Schema(description = "썸네일 이미지", example = "image.png")
            String thumbnailImageUrl,
            @Schema(description = "제목", example = "제목")
            String title,
            @Schema(description = "글 종류", example = "보호중")
            String tag,
            @Schema(description = "사건날짜", example = "2025-01-01")
            LocalDate happenDate,
            @Schema(description = "보호 장소", example = "서울시 광진구")
            String careAddress
    ){
    public static ProtectingAnimalPreview of(PreviewWithDistance dto){
        return new ProtectingAnimalPreview(dto.getReportId(), dto.getThumbnailImageUrl(), dto.getTitle(), ReportTag.valueOf(dto.getTag()).getValue(), dto.getDate(), dto.getAddress());
    }
    public static ProtectingAnimalPreview of(ReportProjection dto){
        return new ProtectingAnimalPreview(dto.getReportId(), dto.getThumbnailImageUrl(), dto.getTitle(), ReportTag.valueOf(dto.getTag()).getValue(), dto.getDate(), dto.getAddress());
    }
}