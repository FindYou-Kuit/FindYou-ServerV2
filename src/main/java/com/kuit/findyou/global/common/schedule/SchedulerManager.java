package com.kuit.findyou.global.common.schedule;

import com.kuit.findyou.global.external.client.ProtectingAnimalApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerManager {

    private final ProtectingAnimalApiClient protectingAnimalApiClient;

    /**
     * 구조 동물 데이터를 매일 새벽 4시에 동기화
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void syncProtectingAnimals() {
        protectingAnimalApiClient.storeProtectingReports();
    }
}
