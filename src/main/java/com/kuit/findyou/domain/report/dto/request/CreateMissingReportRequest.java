package com.kuit.findyou.domain.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateMissingReportRequest (
        List<String> imgUrls,
        String species,
        String breed,
        String age,
        String sex,
        String rfid,
        String furColor,
        String missingDate,  //"yyyy.MM.dd"
        String significant,
        String location,
        Double latitude,
        Double longitude,
        String landmark
){ }
