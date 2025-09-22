package com.kuit.findyou.domain.information.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kuit.findyou.domain.information.model.AnimalDepartment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "보호부서 조회 응답 DTO")
public record GetAnimalDepartmentsResponse (
        List<AnimalDepartmentDTO> departments,
        Long lastId,
        boolean isLast
){
    public static GetAnimalDepartmentsResponse from(List<AnimalDepartment> departments, Long lastId, boolean isLast) {
        return new GetAnimalDepartmentsResponse(
                departments.stream().map(AnimalDepartmentDTO::from).toList(),
                lastId,
                isLast
        );
    }

    @Schema(description = "보호부서 정보")
    public record AnimalDepartmentDTO(
            @JsonProperty("district") String organization, // 담당기관
            @JsonProperty("departmentName") String department,
            String phoneNumber
    ) {
        public static AnimalDepartmentDTO from(AnimalDepartment entity) {
            return new AnimalDepartmentDTO(
                    entity.getOrganization(),
                    entity.getDepartment(),
                    entity.getPhoneNumber()
            );
        }
    }
}
