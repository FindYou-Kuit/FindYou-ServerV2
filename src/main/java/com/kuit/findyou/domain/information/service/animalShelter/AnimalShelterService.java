package com.kuit.findyou.domain.information.service.animalShelter;

import com.kuit.findyou.domain.information.dto.AnimalShelterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;

public interface AnimalShelterService {
    AnimalShelterPagingResponse<AnimalShelterResponse> getShelters(Long lastId, String type, String sido, String sigungu, Double lat, Double lng, int size);
    AnimalShelterPagingResponse<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size);
}
