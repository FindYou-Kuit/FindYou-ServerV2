package com.kuit.findyou.domain.animalProtection.dto;

import com.kuit.findyou.domain.animalProtection.model.AnimalShelter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "보호소 동물병원 응답 DTO")
public record AnimalShelterResponse(
        @Schema(description = "보호소/동물병원 관할구역", example = "[\"서울특별시 강남구\", \"서울특별시 서초구\"]")
        List<String> jurisdiction,

        @Schema(description = "보호소/동물병원 이름", example = "사랑이있는동물병원")
        String centerName,

        @Schema(description = "보호소/동물병원 연락처", example = "053-764-3708")
        String phoneNumber,

        @Schema(description = "보호소/동물병원 주소", example = "서울특별시 강남구 삼성로1 삼성빌딩 1층")
        String address
) {
    public static AnimalShelterResponse from(AnimalShelter shelter){
        return new AnimalShelterResponse(
                List.of(shelter.getJurisdiction().split(",")),
                shelter.getShelterName(),
                shelter.getPhoneNumber(),
                shelter.getAddress()
        );
    }
}
