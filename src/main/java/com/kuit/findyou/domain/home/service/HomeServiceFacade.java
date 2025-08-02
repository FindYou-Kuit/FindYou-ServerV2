package com.kuit.findyou.domain.home.service;


import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.dto.ProtectingAnimalPreview;
import com.kuit.findyou.domain.home.dto.WitnessedOrMissingAnimalPreview;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;

@Slf4j
@RequiredArgsConstructor
@Service
public class HomeServiceFacade {
    private final HomeStatisticsService homeStatisticsService;
    private final RetrieveHomeSectionService retrieveHomeSectionService;
    public GetHomeResponse getHome(Double latitude, Double longitude) {
        // 통계 정보 조회
        // 레디스에 없으면 직접 외부 서버 호출
        GetHomeResponse.TotalStatistics totalStatistics = homeStatisticsService.getCachedTotalStatistics()
                .orElseGet(() -> homeStatisticsService.updateAndGet()); // lazy evaluation이라서 필요할 때만 실행됨

        List<ProtectingAnimalPreview> protectingAnimals = null;
        List<WitnessedOrMissingAnimalPreview> witnessedOrMissingAnimals = null;

        // 좌표 값이 있으면 이를 기반으로 보호중동물과 목격실종동물을 조회
        if(coordinateExists(latitude, longitude)){
            protectingAnimals = retrieveHomeSectionService.retrieveProtectingReportPreviews(latitude, longitude, 10);
            witnessedOrMissingAnimals = retrieveHomeSectionService.retrieveWitnessedOrMissingReportPreviews(latitude, longitude, 10);
            return new GetHomeResponse(totalStatistics, protectingAnimals, witnessedOrMissingAnimals);
        }

        // 아니면 그냥 조회
        protectingAnimals = retrieveHomeSectionService.retrieveProtectingReportPreviews(10);
        witnessedOrMissingAnimals = retrieveHomeSectionService.retrieveWitnessedOrMissingReportPreviews(10);
        return new GetHomeResponse(totalStatistics, protectingAnimals, witnessedOrMissingAnimals);
    }

    private boolean coordinateExists(Double latitude, Double longitude) {
        return latitude != null && longitude != null;
    }
}
