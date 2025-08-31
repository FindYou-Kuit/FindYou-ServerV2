package com.kuit.findyou.domain.information.dto;

import com.kuit.findyou.domain.information.model.AnimalCenter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "보호센터 응답 DTO")
public record AnimalCenterResponse(
        @Schema(description = "보호소/동물병원 관할구역", example = "[\"서울특별시 강남구\", \"서울특별시 서초구\"]")
        List<String> jurisdiction,

        @Schema(description = "보호소/동물병원 이름", example = "사랑이있는동물병원")
        String centerName,

        @Schema(description = "보호소/동물병원 연락처", example = "053-764-3708")
        String phoneNumber,

        @Schema(description = "보호소/동물병원 주소", example = "서울특별시 강남구 삼성로1 삼성빌딩 1층")
        String address
) {
    public static AnimalCenterResponse from(AnimalCenter  center){
        return new AnimalCenterResponse(
                List.of(center.getJurisdiction().split(",")),
                center.getName(),
                center.getPhoneNumber(),
                center.getAddress()
        );
    }
}
