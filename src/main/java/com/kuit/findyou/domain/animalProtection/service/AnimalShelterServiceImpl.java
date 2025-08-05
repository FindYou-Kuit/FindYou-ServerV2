package com.kuit.findyou.domain.animalProtection.service;

import com.kuit.findyou.domain.animalProtection.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.animalProtection.model.AnimalShelter;
import com.kuit.findyou.domain.animalProtection.repository.AnimalShelterRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class AnimalShelterServiceImpl implements AnimalShelterService {

    private final AnimalShelterRepository animalShelterRepository;

    @Override
    public List<AnimalShelterResponse> getShelters(Long userId, Long lastId, String type, String sido, String sigungu, Double lat, Double lng) {

        if (lastId == null || type == null) {
            throw new CustomException(BAD_REQUEST);
        }
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
    public List<AnimalShelterResponse> getNearbyCenters(Long userId, Long lastId, double lat, double lng) {
        final double MAX_DISTANCE_KM = 3.0;

        List<AnimalShelter> nearby = animalShelterRepository.findAllWithLatLngAfterId(lastId);

        return nearby.stream()
                .filter(shelter ->
                        shelter.getLatitude() != null && shelter.getLongitude() != null &&
                                calculateDistance(lat, lng, shelter.getLatitude(), shelter.getLongitude()) <= MAX_DISTANCE_KM)
                .map(AnimalShelterResponse::from)
                .toList();
    }

    //거리 계산을 위한 함수
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
