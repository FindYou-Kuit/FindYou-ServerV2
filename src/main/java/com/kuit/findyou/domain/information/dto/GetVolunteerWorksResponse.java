package com.kuit.findyou.domain.information.dto;

import com.kuit.findyou.domain.information.model.VolunteerWork;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "봉사활동 조회 응답 DTO")
public record GetVolunteerWorksResponse(
    List<VolunteerWorkDTO> volunteerWorks,
    @Schema(description = "마지막 요소의 식별자. 다음 요청 시 필요함", example = "10")
    Long lastId,
    @Schema(description = "마지막 페이지 여부", example = "true")
    boolean isLast
) {
    public static GetVolunteerWorksResponse from(List<VolunteerWork> takenWithSize, Long newLastId, boolean isLast) {
        return new GetVolunteerWorksResponse(takenWithSize.stream()
                .map(VolunteerWorkDTO::entityToDto)
                .collect(Collectors.toList())
                , newLastId, isLast);
    }

    @Schema(description = "봉사활동 정보")
    public record VolunteerWorkDTO(
            @Schema(description = "봉사 기관", example = "서울 동물보호소")
            String institution,
            @Schema(description = "모집 기간", example = "2025.01.01 ~ 2025.01.05")
            String recruitmentPeriod,
            @Schema(description = "봉사 장소 주소", example = "서울시 광진구 건국대")
            String address,
            @Schema(description = "활동 기간", example = "2025.01.01 ~ 2025.01.05")
            String workPeriod,
            @Schema(description = "활동 시간", example = "05:00 ~ 06:00")
            String workTime,
            @Schema(description = "웹뷰 링크", example = "www.web.link")
            String webLink
    ){
        public static VolunteerWorkDTO entityToDto(VolunteerWork entity){
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            return new VolunteerWorkDTO(entity.getInstitution(),
                    entity.getRecruitmentStartDate().format(dateFormatter) + " ~ " + entity.getRecruitmentEndDate().format(dateFormatter),
                    entity.getAddress(),
                    entity.getVolunteerStartAt().format(dateFormatter) + " ~ " + entity.getVolunteerEndAt().format(dateFormatter),
                    entity.getVolunteerStartAt().format(timeFormatter) + " ~ " + entity.getVolunteerEndAt().format(timeFormatter),
                    entity.getWebLink());
        }
    }
}
