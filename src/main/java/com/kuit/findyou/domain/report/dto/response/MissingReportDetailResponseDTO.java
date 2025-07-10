package com.kuit.findyou.domain.report.dto.response;

import lombok.Builder;

import java.util.List;

public record MissingReportDetailResponseDTO(
        List<String> imageUrls,
        String breed,
        String tag,
        String age,
        String sex,
        String missingDate,
        String rfid,
        String significant,
        String missingLocation,
        String missingAddress,
        double latitude,
        double longitude,
        String reporterName,
        String reporterTel,
        boolean interest
) {
}
