package com.kuit.findyou.domain.animalProtection.service;

import com.kuit.findyou.domain.animalProtection.dto.AnimalShelterResponse;

import java.util.List;

public interface AnimalShelterService {
    List<AnimalShelterResponse> getShelters(Long userId, Long lastId, String type, String sido, String sigungu, Double lat, Double lng);
}
