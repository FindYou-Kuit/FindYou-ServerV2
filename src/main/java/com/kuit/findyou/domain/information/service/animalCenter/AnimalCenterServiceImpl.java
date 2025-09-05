package com.kuit.findyou.domain.information.service.animalCenter;

import com.kuit.findyou.domain.information.dto.AnimalCenterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalCenterResponse;
import com.kuit.findyou.domain.information.model.AnimalCenter;
import com.kuit.findyou.domain.information.repository.AnimalCenterRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.INVALID_CURSOR;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.INVALID_SIZE;
import static com.kuit.findyou.global.common.util.CalculateDistanceUtil.calculateDistance;

@Service
@RequiredArgsConstructor
public class AnimalCenterServiceImpl implements AnimalCenterService {

    private final AnimalCenterRepository animalCenterRepository;

    @Override
    public AnimalCenterPagingResponse<AnimalCenterResponse> getCenters(Long lastId, String district, int size) {

        //관할구역 필터
        String jurisdiction =  (district != null && !district.isBlank())
                ? district.trim()
                : null;
        List<AnimalCenter> results = animalCenterRepository.findWithFilter(lastId, jurisdiction, PageRequest.of(0, size + 1));
        boolean isLast = results.size() <= size;
        List<AnimalCenter> page = isLast ? results : results.subList(0, size);
        Long nextLastId = page.isEmpty() ? null : page.get(page.size() - 1).getId();

        List<AnimalCenterResponse> centers = page.stream()
                .map(AnimalCenterResponse::from)
                .toList();

        return new AnimalCenterPagingResponse<>(centers, nextLastId, isLast);
    }

    @Override
    public AnimalCenterPagingResponse<AnimalCenterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size) {
        final double MAX_DISTANCE_KM = 3.0;

        List<AnimalCenter> nearby = animalCenterRepository.findAllWithLatLngAfterId(lastId, PageRequest.of(0, size + 1));

        List<AnimalCenter> filtered = nearby.stream()
                .filter(center ->
                        center.getLatitude() != null && center.getLongitude() != null &&
                                calculateDistance(lat, lng, center.getLatitude(), center.getLongitude()) <= MAX_DISTANCE_KM)
                .toList();
        boolean isLast = filtered.size() <= size;
        List<AnimalCenter> page = isLast ? filtered : filtered.subList(0, size);
        Long nextLastId = page.isEmpty() ? null : page.get(page.size() - 1).getId();

        List<AnimalCenterResponse> centers = page.stream()
                .map(AnimalCenterResponse::from)
                .toList();

        return new AnimalCenterPagingResponse<>(centers, nextLastId, isLast);

    }
}
