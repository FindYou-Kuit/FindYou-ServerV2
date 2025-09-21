package com.kuit.findyou.domain.home.service;

import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.dto.ProtectingAndAdoptedAnimalCount;
import com.kuit.findyou.domain.home.exception.CacheUpdateFailedException;
import com.kuit.findyou.global.common.util.DirectExecutorService;
import com.kuit.findyou.global.external.client.AnimalStatsApiClient;
import com.kuit.findyou.global.external.client.LossAnimalApiClient;
import com.kuit.findyou.global.external.client.ProtectingAnimalApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeStatisticsServiceTest {
    private HomeStatisticsService homeStatisticsService;
    private AnimalStatsApiClient animalStatsApiClient = mock(AnimalStatsApiClient.class);
    private ProtectingAnimalApiClient protectingAnimalApiClient = mock(ProtectingAnimalApiClient.class);
    private LossAnimalApiClient lossAnimalApiClient = mock(LossAnimalApiClient.class);
    private CacheHomeStatsService cacheHomeStatsService = mock(CacheHomeStatsService.class);
    private HomeStatsCacheSnapshotService homeStatsCacheSnapshotService = mock(HomeStatsCacheSnapshotService.class);

    @BeforeEach
    void setUp(){
        homeStatisticsService = new HomeStatisticsService(
                animalStatsApiClient,
                protectingAnimalApiClient,
                lossAnimalApiClient,
                cacheHomeStatsService,
                homeStatsCacheSnapshotService,
                new DirectExecutorService() // 동기 실행기 주입
        );
    }

    @DisplayName("캐시에 통계가 있으면 이를 반환한다")
    @Test
    void should_ReturnCachedStats_When_ItExists(){
        // given
        GetHomeResponse.TotalStatistics mockStats = mock(GetHomeResponse.TotalStatistics.class);
        when(cacheHomeStatsService.getCachedTotalStatistics()).thenReturn(mockStats);

        // when
        GetHomeResponse.TotalStatistics result = homeStatisticsService.get();

        // then
        assertThat(result).isEqualTo(mockStats);
    }

    @DisplayName("캐시에 데이터가 없으면 DB에 있는 데이터를 반환한다")
    @Test
    void should_ReturnPersistentStats_When_NoCachedStatsExists(){
        // given
        when(cacheHomeStatsService.getCachedTotalStatistics()).thenReturn(null);
        GetHomeResponse.TotalStatistics mockStats = mock(GetHomeResponse.TotalStatistics.class);
        when(homeStatsCacheSnapshotService.find()).thenReturn(Optional.of(mockStats));

        // when
        GetHomeResponse.TotalStatistics result = homeStatisticsService.get();

        // then
        assertThat(result).isEqualTo(mockStats);
    }

    @DisplayName("DB에도 데이터가 없다면 비어 있는 통계를 반환한다")
    @Test
    void should_ReturnEmptyStats_When_CachedStatsAndPersistentStatsDontExist(){
        // given
        when(cacheHomeStatsService.getCachedTotalStatistics()).thenReturn(null);
        when(homeStatsCacheSnapshotService.find()).thenReturn(Optional.empty());

        // when
        GetHomeResponse.TotalStatistics result = homeStatisticsService.get();

        // then
        assertThat(result).isEqualTo(GetHomeResponse.TotalStatistics.empty()); // record 타입은 equals가 값을 기준으로 동일한지 판단
    }

    @DisplayName("모든 외부 API 클라이언트가 정상적으로 동작하면 캐시 업데이트에 성공한다")
    @Test
    void should_SucceedUpdating_WhenAllApiClientsReturnNormalResponse() throws CacheUpdateFailedException {
        // given
        GetHomeResponse.Statistics stats = new GetHomeResponse.Statistics("5", "3", "2", "7");
        GetHomeResponse.TotalStatistics totalStats = new GetHomeResponse.TotalStatistics(stats, stats, stats);
        when(animalStatsApiClient.fetchProtectingAndAdoptedAnimalCount(any(), any()))
                .thenReturn(new ProtectingAndAdoptedAnimalCount("3", "2"));
        when(protectingAnimalApiClient.fetchRescuedAnimalCount(any(), any())).thenReturn("5");
        when(lossAnimalApiClient.fetchReportedAnimalCount(any(), any())).thenReturn("7");

        // when
        GetHomeResponse.TotalStatistics result = homeStatisticsService.update();

        // then
        verify(cacheHomeStatsService).cacheTotalStatistics(eq(totalStats));
        verify(homeStatsCacheSnapshotService).save(eq(totalStats));
        assertThat(result).isEqualTo(totalStats);
    }

    @DisplayName("외부 API 클라이언트 중 하나라도 예외를 발생시키면 예외를 반환한다")
    @Test
    void should_ThrowCacheUpdateFailedException_WhenSomeOfApiClientThrowException() {
        // given
        when(animalStatsApiClient.fetchProtectingAndAdoptedAnimalCount(anyString(), anyString()))
                .thenThrow(new RuntimeException("downstream boom"));

        // when / then
        assertThatThrownBy(() -> homeStatisticsService.update())
                .isInstanceOf(CacheUpdateFailedException.class);

        verify(cacheHomeStatsService, never()).cacheTotalStatistics(any());
        verify(homeStatsCacheSnapshotService, never()).save(any());
    }

    @DisplayName("캐시된 통계 데이터가 있으면 이 값을 캐시를 연장한다")
    @Test
    void should_extendExpirationOfCachedStats_WhenItExists(){
        // given
        GetHomeResponse.TotalStatistics totalStats = mock(GetHomeResponse.TotalStatistics.class);
        when(cacheHomeStatsService.getCachedTotalStatistics()).thenReturn(totalStats);

        // when
        homeStatisticsService.extendCacheExpiration();

        // then
        verify(cacheHomeStatsService).cacheTotalStatistics(eq(totalStats));
    }

    @DisplayName("캐시된 통계 데이터가 없으면 DB에 저장된 값을 캐시에 저장한다")
    @Test
    void should_cachePersistentStats_WhenNoCachedStatsExists(){
        // given
        GetHomeResponse.TotalStatistics totalStats = mock(GetHomeResponse.TotalStatistics.class);
        when(cacheHomeStatsService.getCachedTotalStatistics()).thenReturn(null);
        when(homeStatsCacheSnapshotService.find()).thenReturn(Optional.of(totalStats));

        // when
        homeStatisticsService.extendCacheExpiration();

        // then
        verify(cacheHomeStatsService).cacheTotalStatistics(eq(totalStats));
    }

    @DisplayName("캐시된 통계와 DB에 저장된 통계가 없으면 빈 통계를 캐시 저장한다")
    @Test
    void should_cacheEmptyStats_WhenCachedStatsAndPersistentStatsDontExist(){
        // given
        GetHomeResponse.TotalStatistics totalStats = mock(GetHomeResponse.TotalStatistics.class);
        when(cacheHomeStatsService.getCachedTotalStatistics()).thenReturn(null);
        when(homeStatsCacheSnapshotService.find()).thenReturn(Optional.empty());

        // when
        homeStatisticsService.extendCacheExpiration();

        // then
        verify(cacheHomeStatsService).cacheTotalStatistics(eq(GetHomeResponse.TotalStatistics.empty()));
    }
}