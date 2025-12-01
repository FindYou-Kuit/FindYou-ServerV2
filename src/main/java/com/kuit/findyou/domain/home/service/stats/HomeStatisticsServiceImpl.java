package com.kuit.findyou.domain.home.service.stats;

import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.global.external.dto.ProtectingAndAdoptedAnimalCount;
import com.kuit.findyou.domain.home.exception.CacheUpdateFailedException;
import com.kuit.findyou.global.external.client.AnimalStatsApiClient;
import com.kuit.findyou.global.external.client.LossAnimalApiClient;
import com.kuit.findyou.global.external.client.ProtectingAnimalApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeStatisticsServiceImpl implements HomeStatisticsService{
    @Value("${findyou.home-stats.parsing-timeout-sec}")
    private long PARSING_TIMEOUT_SEC;
    private final AnimalStatsApiClient animalStatsApiClient;
    private final ProtectingAnimalApiClient protectingAnimalApiClient;
    private final LossAnimalApiClient lossAnimalApiClient;
    private final CacheHomeStatsService cacheHomeStatsService;
    private final HomeCacheSnapshotService homeCacheSnapshotService;
    private final ExecutorService statisticsExecutor;

    public GetHomeResponse.TotalStatistics get() {
        GetHomeResponse.TotalStatistics cached = cacheHomeStatsService.getCachedTotalStatistics();
        if(cached != null){
            return cached;
        }
        return homeCacheSnapshotService.findHomeStats()
                .orElse(GetHomeResponse.TotalStatistics.empty());
    }

    public GetHomeResponse.TotalStatistics update() throws CacheUpdateFailedException {
        log.info("[update] 캐시랑 스냅샷 갱신 시작");

        // 모든 통계 구하기
        CompletableFuture<GetHomeResponse.Statistics> recent7DaysFuture = fetchStatisticsAsync(7);
        CompletableFuture<GetHomeResponse.Statistics> recent3MonthsFuture = fetchStatisticsAsync(90);
        CompletableFuture<GetHomeResponse.Statistics> recent1YearFuture = fetchStatisticsAsync(365);

        try{
            CompletableFuture.allOf(recent7DaysFuture, recent3MonthsFuture, recent1YearFuture).join();
            GetHomeResponse.TotalStatistics result = new GetHomeResponse.TotalStatistics(recent7DaysFuture.join(), recent3MonthsFuture.join(), recent1YearFuture.join());

            // 레디스와 DB에 저장
            cacheHomeStatsService.cacheTotalStatistics(result);
            homeCacheSnapshotService.saveHomeStats(result);

            log.info("[update] 캐시랑 스냅샷 저장 완료");
            return result;

        } catch (Exception e) {
            log.error("[update] 예외 발생", e);
            throw new CacheUpdateFailedException();
        }
    }

    public void extendCacheExpiration() {
        GetHomeResponse.TotalStatistics cachedTotalStatistics = cacheHomeStatsService.getCachedTotalStatistics();
        if(cachedTotalStatistics == null){
            // 레디스 캐시에 통계 데이터가 없으면 DB의 내용을 연장하도록 시도
            Optional<GetHomeResponse.TotalStatistics> snapshot = homeCacheSnapshotService.findHomeStats();
            if(snapshot.isPresent()){
                log.info("[extendCacheExpiration] DB에 있는 홈 통계 스냅샷을 연장");
                cachedTotalStatistics = snapshot.get();
            }
            else{
                log.error("[extendCacheExpiration] DB에 홈 통계 스냅샷 없음 -> 비어 있는 통계 정보를 캐싱");
                cachedTotalStatistics = GetHomeResponse.TotalStatistics.empty();
            }
        }
        cacheHomeStatsService.cacheTotalStatistics(cachedTotalStatistics);
    }

    private CompletableFuture<GetHomeResponse.Statistics> fetchStatisticsAsync(int days){
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String bgnde =  startDate.format(formatter);
        String endde = endDate.format(formatter);

        CompletableFuture<ProtectingAndAdoptedAnimalCount> pna =
                CompletableFuture.supplyAsync(() -> animalStatsApiClient.fetchProtectingAndAdoptedAnimalCount(bgnde, endde), statisticsExecutor)
                        .orTimeout(PARSING_TIMEOUT_SEC, TimeUnit.SECONDS);

        CompletableFuture<String> rescued =
                CompletableFuture.supplyAsync(() -> protectingAnimalApiClient.fetchRescuedAnimalCount(bgnde, endde), statisticsExecutor)
                        .orTimeout(PARSING_TIMEOUT_SEC, TimeUnit.SECONDS);

        CompletableFuture<String> reported =
                CompletableFuture.supplyAsync(() -> lossAnimalApiClient.fetchReportedAnimalCount(bgnde, endde), statisticsExecutor)
                        .orTimeout(PARSING_TIMEOUT_SEC, TimeUnit.SECONDS);

        return CompletableFuture.allOf(pna, rescued, reported)
                .thenApply(v -> {
                    ProtectingAndAdoptedAnimalCount count = pna.join();
                    return new GetHomeResponse.Statistics(
                            rescued.join(), count.protectingAnimalCount(), count.adoptedAnimalCount(), reported.join()
                    );
                });
    }
}
