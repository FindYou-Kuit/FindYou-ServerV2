package com.kuit.findyou.global.common.schedule;

import com.kuit.findyou.domain.information.service.volunteerWork.SyncVolunteerWorkService;
import com.kuit.findyou.domain.report.service.sync.ProtectingReportSyncService;
import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.exception.CacheUpdateFailedException;
import com.kuit.findyou.domain.home.service.HomeStatisticsService;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.HOME_STATISTICS_UPDATE_FAILED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerManager {

    private final ProtectingReportSyncService protectingReportSyncService;
    private final HomeStatisticsService homeStatisticsService;
    private final SyncVolunteerWorkService syncVolunteerWorkService;

    /**
     * 구조 동물 데이터를 매일 새벽 4시에 동기화
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void syncProtectingAnimals() {
        protectingReportSyncService.syncProtectingReports();
    }

    /**
     * 홈화면 통계 정보를 정각마다 동기화
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateHomeStatistics(){
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
                throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
            }
            homeStatisticsService.cacheTotalStatistics(cachedTotalStatistics.get());
            log.error("[excute] 캐시 TTL 연장 성공");
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void syncVolunteerWorks(){
        syncVolunteerWorkService.synchronize();
    }
}
