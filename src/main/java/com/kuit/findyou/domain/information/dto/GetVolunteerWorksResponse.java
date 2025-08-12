package com.kuit.findyou.domain.information.dto;

import java.util.List;

public record GetVolunteerWorksResponse(
    List<VolunteerWorkDTO> volunteerWorks,
    Long lastId,
    boolean isLast
) {
    public record VolunteerWorkDTO(
            String institution,
            String recruitmentPeriod,
            String address,
            String workPeriod,
            String workTime,
            String webLink
    ){ }
}
