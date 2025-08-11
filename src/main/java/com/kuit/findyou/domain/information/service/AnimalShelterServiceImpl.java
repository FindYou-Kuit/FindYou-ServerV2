package com.kuit.findyou.domain.information.service;

import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.model.AnimalShelter;
import com.kuit.findyou.domain.information.repository.AnimalShelterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import static com.kuit.findyou.global.common.util.CalculateDistanceUtil.calculateDistance;

@Service
@RequiredArgsConstructor
public class AnimalShelterServiceImpl implements AnimalShelterService {

    private final AnimalShelterRepository animalShelterRepository;

    @Override
    public List<AnimalShelterResponse> getShelters(Long lastId, String type, String sido, String sigungu, Double lat, Double lng) {

        String hospital = "병원"; //유형 필터에 사용되는 키워드

        //관할구역 필터
        String jurisdiction = (sido != null && !sido.isBlank() && sigungu != null && !sigungu.isBlank())
                ? sido + " " + sigungu
                : null;
        List<AnimalShelter> results = animalShelterRepository.findWithFilter(lastId, type, hospital, jurisdiction);

        return results.stream()
                .map(AnimalShelterResponse::from)
                .toList();
    }

    @Override
    public List<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng) {
        final double MAX_DISTANCE_KM = 3.0;

        List<AnimalShelter> nearby = animalShelterRepository.findAllWithLatLngAfterId(lastId);

        return nearby.stream()
                .filter(shelter ->
                        shelter.getLatitude() != null && shelter.getLongitude() != null &&
                                calculateDistance(lat, lng, shelter.getLatitude(), shelter.getLongitude()) <= MAX_DISTANCE_KM)
                .map(AnimalShelterResponse::from)
                .toList();
    }

}
