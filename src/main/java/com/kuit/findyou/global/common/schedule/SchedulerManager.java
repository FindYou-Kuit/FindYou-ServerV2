package com.kuit.findyou.global.common.schedule;

import com.kuit.findyou.domain.information.service.volunteerWork.SyncVolunteerWorkService;
import com.kuit.findyou.domain.report.service.sync.MissingReportSyncService;
import com.kuit.findyou.domain.report.service.sync.ProtectingReportSyncService;
import com.kuit.findyou.domain.home.exception.CacheUpdateFailedException;
import com.kuit.findyou.domain.home.service.stats.HomeStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerManager {

    private final ProtectingReportSyncService protectingReportSyncService;
    private final MissingReportSyncService missingReportSyncService;
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
     * 구조 동물 데이터를 매일 새벽 4시 30에 동기화
     */
    @Scheduled(cron = "0 30 4 * * *")
    public void syncMissingAnimals() {
        missingReportSyncService.syncMissingReports();
    }

    /**
     * 홈화면 통계 정보를 정각마다 동기화
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateHomeStatistics() {
        try {
            homeStatisticsService.update();
        } catch (CacheUpdateFailedException e) {
            log.warn("[updateHomeStatistics] 통계 캐시 업데이트 실패 -> 캐싱된 통계 TTL 연장");
            homeStatisticsService.extendCacheExpiration();
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void syncVolunteerWorks() {
        syncVolunteerWorkService.synchronize();
    }
}
