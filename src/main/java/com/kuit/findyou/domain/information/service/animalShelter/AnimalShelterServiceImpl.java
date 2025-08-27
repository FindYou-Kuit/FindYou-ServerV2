package com.kuit.findyou.domain.information.service.animalShelter;

import com.kuit.findyou.domain.information.dto.AnimalShelterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.model.AnimalShelter;
import com.kuit.findyou.domain.information.repository.AnimalShelterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import static com.kuit.findyou.global.common.util.CalculateDistanceUtil.calculateDistance;

@Service
@RequiredArgsConstructor
public class AnimalShelterServiceImpl implements AnimalShelterService {

    private final AnimalShelterRepository animalShelterRepository;

    @Override
    public AnimalShelterPagingResponse<AnimalShelterResponse> getShelters(Long lastId, String type, String sido, String sigungu, Double lat, Double lng, int size) {

        String hospital = "병원"; //유형 필터에 사용되는 키워드

        //관할구역 필터
        String jurisdiction = (sido != null && !sido.isBlank() && sigungu != null && !sigungu.isBlank())
                ? sido + " " + sigungu
                : null;
        List<AnimalShelter> results = animalShelterRepository.findWithFilter(lastId, type, hospital, jurisdiction, PageRequest.of(0, size + 1));
        boolean isLast = results.size() <= size;
        List<AnimalShelter> page = isLast ? results : results.subList(0, size);
        Long nextLastId = page.isEmpty() ? null : page.get(page.size() - 1).getId();

        List<AnimalShelterResponse> centers = page.stream()
                .map(AnimalShelterResponse::from)
                .toList();

        return new AnimalShelterPagingResponse<>(centers, nextLastId, isLast);
    }

    @Override
    public AnimalShelterPagingResponse<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size) {
        final double MAX_DISTANCE_KM = 3.0;

        List<AnimalShelter> nearby = animalShelterRepository.findAllWithLatLngAfterId(lastId, PageRequest.of(0, size + 1));

        List<AnimalShelter> filtered = nearby.stream()
                .filter(shelter ->
                        shelter.getLatitude() != null && shelter.getLongitude() != null &&
                                calculateDistance(lat, lng, shelter.getLatitude(), shelter.getLongitude()) <= MAX_DISTANCE_KM)
                .toList();
        boolean isLast = filtered.size() <= size;
        List<AnimalShelter> page = isLast ? filtered : filtered.subList(0, size);
        Long nextLastId = page.isEmpty() ? null : page.get(page.size() - 1).getId();

        List<AnimalShelterResponse> centers = page.stream()
                .map(AnimalShelterResponse::from)
                .toList();

        return new AnimalShelterPagingResponse<>(centers, nextLastId, isLast);

    }
}
