package com.kuit.findyou.domain.information.service;

import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;

import java.util.List;

public interface AnimalShelterService {
    List<AnimalShelterResponse> getShelters(Long lastId, String type, String sido, String sigungu, Double lat, Double lng);
    List<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng);
}
