package com.kuit.findyou.domain.home.service;


import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.domain.home.dto.response.ProtectingAnimalCard;
import com.kuit.findyou.domain.home.dto.response.WitnessedOrMissingAnimalCard;
import com.kuit.findyou.domain.home.service.card.RetrieveHomeAnimalCardService;
import com.kuit.findyou.domain.home.service.stats.HomeStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class HomeServiceFacade {
    private final HomeStatisticsService homeStatisticsService;
    private final RetrieveHomeAnimalCardService retrieveHomeAnimalCardService;
    public GetHomeResponse getHome(Double latitude, Double longitude) {
        // 통계 정보 조회
        // 레디스에 없으면 직접 외부 서버 호출
        GetHomeResponse.TotalStatistics totalStatistics = homeStatisticsService.get();

        List<ProtectingAnimalCard> protectingAnimals = null;
        List<WitnessedOrMissingAnimalCard> witnessedOrMissingAnimals = null;

        // 좌표 값이 있으면 이를 기반으로 보호중동물과 목격실종동물을 조회
        if(coordinateExists(latitude, longitude)){
            protectingAnimals = retrieveHomeAnimalCardService.retrieveProtectingReportCards(latitude, longitude, 10);
            witnessedOrMissingAnimals = retrieveHomeAnimalCardService.retrieveWitnessedOrMissingReportCards(latitude, longitude, 10);
            return new GetHomeResponse(totalStatistics, protectingAnimals, witnessedOrMissingAnimals);
        }

        // 아니면 그냥 조회
        protectingAnimals = retrieveHomeAnimalCardService.retrieveProtectingReportCards(10);
        witnessedOrMissingAnimals = retrieveHomeAnimalCardService.retrieveWitnessedOrMissingReportCards(10);
        return new GetHomeResponse(totalStatistics, protectingAnimals, witnessedOrMissingAnimals);
    }

    private boolean coordinateExists(Double latitude, Double longitude) {
        return latitude != null && longitude != null;
    }
}
