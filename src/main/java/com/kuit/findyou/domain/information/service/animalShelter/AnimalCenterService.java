package com.kuit.findyou.domain.information.service.animalShelter;

import com.kuit.findyou.domain.information.dto.AnimalShelterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;

public interface AnimalCenterService {
    AnimalShelterPagingResponse<AnimalShelterResponse> getCenters(Long lastId,  String district, int size);
    AnimalShelterPagingResponse<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size);
}
