package com.kuit.findyou.domain.information.service.animalCenter;

import com.kuit.findyou.domain.information.dto.AnimalCenterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalCenterResponse;

public interface AnimalCenterService {
    AnimalCenterPagingResponse<AnimalCenterResponse> getCenters(Long lastId, String district, int size);
    AnimalCenterPagingResponse<AnimalCenterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size);
}
