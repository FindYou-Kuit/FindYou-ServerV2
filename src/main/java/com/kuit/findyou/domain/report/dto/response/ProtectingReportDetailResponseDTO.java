package com.kuit.findyou.domain.report.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ProtectingReportDetailResponseDTO(
        List<String> imageUrl,
        String breed,
        String tag,
        String age,
        String weight,
        String furColor,
        String sex,
        String neutering,
        String significant,
        String careName,
        String careAddr,
        double latitude,
        double longitude,
        String careTel,
        String foundDate,
        String foundLocation,
        String noticeDuration,
        String noticeNumber,
        String authority,
        boolean interest
) {
}
