package com.kuit.findyou.domain.report.dto.response;

import java.util.List;

public record WitnessReportDetailResponseDTO(
        List<String> imageUrls,
        String breed,
        String tag,
        String furColor,
        String significant,
        String witnessLocation,
        String witnessAddress,
        double latitude,
        double longitude,
        String reporterInfo,
        String witnessDate,
        boolean interest
) {
}
