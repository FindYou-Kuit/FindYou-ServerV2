package com.kuit.findyou.domain.animalProtection.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Animal Shelter", description = "보호소 및 동물병원 조회 API")
@RequestMapping("api/v2/informations/shelters-and-hospitals")
@RequiredArgsConstructor
public class AnimalShelterController {

}
