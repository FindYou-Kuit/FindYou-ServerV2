package com.kuit.findyou.domain.information.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdateVolunteerWorkRequest(
        String institution,
        LocalDate recruitmentStartDate,
        LocalDate recruitmentEndDate,
        String address,
        LocalDateTime volunteerStartAt,
        LocalDateTime volunteerEndAt,
        String webLink,
        String registerNumber,
        Long runId
) {
}