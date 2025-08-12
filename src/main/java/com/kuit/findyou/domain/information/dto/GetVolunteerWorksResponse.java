package com.kuit.findyou.domain.information.dto;

import com.kuit.findyou.domain.information.model.VolunteerWork;

import java.util.List;
import java.util.stream.Collectors;

public record GetVolunteerWorksResponse(
    List<VolunteerWorkDTO> volunteerWorks,
    Long lastId,
    boolean isLast
) {
    public static GetVolunteerWorksResponse from(List<VolunteerWork> takenWithSize, Long newLastId, boolean isLast) {
        return new GetVolunteerWorksResponse(takenWithSize.stream()
                .map(VolunteerWorkDTO::entityToDto)
                .collect(Collectors.toList())
                , newLastId, isLast);
    }

    public record VolunteerWorkDTO(
            String institution,
            String recruitmentPeriod,
            String address,
            String workPeriod,
            String workTime,
            String webLink
    ){
        public static VolunteerWorkDTO entityToDto(VolunteerWork entity){
            return new VolunteerWorkDTO(entity.getInstitution(),
                    entity.getRecruitmentStartAt() + " ~ " + entity.getRecruitmentEndAt(),
                    entity.getAddress(),
                    entity.getVolunteerStartAt() + " ~ " + entity.getVolunteerEndAt(),
                    entity.getVolunteerTime(),
                    entity.getWebLink());
        }
    }
}
