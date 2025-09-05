package com.kuit.findyou.domain.report.dto.request;

import java.util.List;

public record CreateWitnessReportRequest(
        List<String> imgUrls,
        String species,
        String breed,
        String furColor,
        String foundDate,  //"yyyy.MM.dd"
        String significant,
        String location,
        String landmark
) {
}
