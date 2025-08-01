package com.kuit.findyou.domain.home.scheduler;

import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.exception.CacheUpdateFailedException;
import com.kuit.findyou.domain.home.service.HomeStatisticsService;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.HOME_DATA_CACHING_FAILED;

@Slf4j
@RequiredArgsConstructor
@Component
public class HomeStatisticsUpdateScheduler {
    private final HomeStatisticsService homeStatisticsService;

    // 매 시간마다 파싱
    @Scheduled(cron = "0 0 * * * *")
    public void execute(){
        try{
            homeStatisticsService.updateTotalStatistics();
        }
        catch (CacheUpdateFailedException e){
            log.error("[excute] 캐시 업데이트 실패");
            log.error("[excute] 캐시 TTL 연장 시도");
            Optional<GetHomeResponse.TotalStatistics> cachedTotalStatistics = homeStatisticsService.getCachedTotalStatistics();
            if(cachedTotalStatistics.isEmpty()){
                log.warn("[excute] 캐시에 데이터 없어서 TTL 연장 실패");
                log.warn("[excute] 빈 통계 저장");
                homeStatisticsService.cacheTotalStatistics(GetHomeResponse.TotalStatistics.empty());
                throw new CustomException(HOME_DATA_CACHING_FAILED);
            }
            homeStatisticsService.cacheTotalStatistics(cachedTotalStatistics.get());
            log.error("[excute] 캐시 TTL 연장 성공");
        }
    }
}
