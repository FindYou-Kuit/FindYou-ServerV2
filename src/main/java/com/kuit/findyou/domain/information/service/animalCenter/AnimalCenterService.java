package com.kuit.findyou.domain.information.service.animalCenter;

import com.kuit.findyou.domain.information.dto.response.AnimalCenterPagingResponse;
import com.kuit.findyou.domain.information.dto.response.AnimalCenterResponse;

public interface AnimalCenterService {
    AnimalCenterPagingResponse<AnimalCenterResponse> getCenters(Long lastId, String district, int size);
    AnimalCenterPagingResponse<AnimalCenterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size);
}
