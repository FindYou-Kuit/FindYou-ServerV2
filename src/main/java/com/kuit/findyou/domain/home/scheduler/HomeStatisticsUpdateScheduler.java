package com.kuit.findyou.domain.home.scheduler;

import com.kuit.findyou.domain.home.service.HomeStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class HomeStatisticsUpdateScheduler {
    private final HomeStatisticsService homeStatisticsParsingService;

    /*
    매 시간마다 파싱
     */
    @Scheduled(cron = "0 0 * * * *")
    public void execute(){
        homeStatisticsParsingService.updateTotalStatistics();
    }
}
