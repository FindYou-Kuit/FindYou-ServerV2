package com.kuit.findyou.domain.home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.dto.LossInfoServiceApiResponse;
import com.kuit.findyou.domain.home.dto.RescueAnimalStatsServiceApiResponse;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.external.dto.ProtectingAnimalApiFullResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.INTERNAL_SERVER_ERROR;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class HomeStatisticsService {
    @Value("${openapi.protecting-animal.api-key}")
    private String serviceKey;
    private final RestClient rescueAnimalStatRestClient;
    private final RestClient protectingAnimalRestClient;
    private final RestClient lossAnimalInfoRestClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final ExecutorService statisticsExecutor;
    private final ObjectMapper objectMapper;
    private final String REDIS_KEY_FOR_CACHED_STATISTICS = "home:statistics";
    public HomeStatisticsService(
            @Qualifier("rescueAnimalStatsRestClient") RestClient rescueAnimalStatRestClient,
            @Qualifier("protectingAnimalRestClient") RestClient protectingAnimalRestClient,
            @Qualifier("lossAnimalInfoRestClient") RestClient lossAnimalInfoRestClient,
            RedisTemplate redisTemplate,
            ExecutorService executor,
            ObjectMapper objectMapper
    ){
        this.rescueAnimalStatRestClient = rescueAnimalStatRestClient;
        this.protectingAnimalRestClient = protectingAnimalRestClient;
        this.lossAnimalInfoRestClient = lossAnimalInfoRestClient;
        this.redisTemplate = redisTemplate;
        this.statisticsExecutor = executor;
        this.objectMapper = objectMapper;
    }

    public GetHomeResponse.TotalStatistics updateTotalStatistics() {
        log.info("[updateTotalStatistics] 캐시 업데이트 시작");
        // 모든 통계 구하기
        GetHomeResponse.Statistics recent7DaysStatistics = parseStatistics(7);
        GetHomeResponse.Statistics recent3MonthsStatistics = parseStatistics(90);
        GetHomeResponse.Statistics recent1YearStatistics = parseStatistics(365);
        GetHomeResponse.TotalStatistics totalStatistics = new GetHomeResponse.TotalStatistics(recent7DaysStatistics, recent3MonthsStatistics, recent1YearStatistics);

        // 레디스에 저장
        cacheTotalStatistics(totalStatistics);

        log.info("[updateTotalStatistics] 캐시 업데이트 완료");

        return totalStatistics;
    }

    private void cacheTotalStatistics(GetHomeResponse.TotalStatistics totalStats) {
        try{
            String json = objectMapper.writeValueAsString(totalStats);
            redisTemplate.opsForValue().set(REDIS_KEY_FOR_CACHED_STATISTICS, json, Duration.ofHours(24));
        }
        catch (JsonProcessingException e){
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<GetHomeResponse.TotalStatistics> getCachedTotalStatistics(){
        try{
            String json = redisTemplate.opsForValue().get(REDIS_KEY_FOR_CACHED_STATISTICS);
            if(json == null){
                log.info("[getCachedTotalStatistics] 캐시에 데이터 없음");
                return Optional.empty();
            }
            log.info("[getCachedTotalStatistics] 캐시에 데이터 있음");
            return Optional.of(objectMapper.readValue(json, GetHomeResponse.TotalStatistics.class));
        }
        catch (JsonProcessingException e){
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }

    private GetHomeResponse.Statistics parseStatistics(long daysOfPeriod) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysOfPeriod - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String bgnde =  startDate.format(formatter);
        String endde = endDate.format(formatter);

        CompletableFuture<String> protectingAndAdotedFuture = CompletableFuture.supplyAsync(()->{
            RescueAnimalStatsServiceApiResponse response1 = rescueAnimalStatRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("bgnde", bgnde)
                            .queryParam("endde", endde)
                            .queryParam("_type", "json")
                            .build())
                    .retrieve()
                    .body(RescueAnimalStatsServiceApiResponse.class);

            String protectingAnimalCount = "0";
            String adoptedAnimalCount = "0";

            for(RescueAnimalStatsServiceApiResponse.Item item : response1.response().body().items().item()){
                if(isProtectingAnimalTotalCount(item)) protectingAnimalCount = item.total();
                else if(isAdoptedAnimalCount(item)) adoptedAnimalCount = item.total();
            }

            return protectingAnimalCount + "," + adoptedAnimalCount;
        }, statisticsExecutor);

        CompletableFuture<String> rescuedFuture = CompletableFuture.supplyAsync(() -> {
            ProtectingAnimalApiFullResponse response2 = protectingAnimalRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/abandonmentPublic_v2")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("bgnde", bgnde)
                            .queryParam("endde", endde)
                            .queryParam("_type", "json")
                            .build())
                    .retrieve()
                    .body(ProtectingAnimalApiFullResponse.class);

            String rescuedAnimalCount = response2.response().body().totalCount();
            return rescuedAnimalCount;
        }, statisticsExecutor);

        CompletableFuture<String> reportedFuture = CompletableFuture.supplyAsync(() -> {
            LossInfoServiceApiResponse response3 = lossAnimalInfoRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("bgnde", bgnde)
                            .queryParam("endde", endde)
                            .queryParam("_type", "json")
                            .build())
                    .retrieve()
                    .body(LossInfoServiceApiResponse.class);

            String reportedAnimalCount = response3.response().body().totalCount();
            return reportedAnimalCount;
        }, statisticsExecutor);

        try{
            String[] token = protectingAndAdotedFuture.get().split(",");
            String protectingAnimalCount = token[0];
            String adoptedAnimalCount = token[1];
            String reportedAnimalCount = reportedFuture.get();
            String rescuedAnimalCount = rescuedFuture.get();

            log.info("파싱 결과 protectingAnimalCount = {} adoptedAnimalCount = {} reportedAnimalCount = {} rescuedAnimalCount = {}", protectingAnimalCount, adoptedAnimalCount, reportedAnimalCount, rescuedAnimalCount);

            return new GetHomeResponse.Statistics(rescuedAnimalCount, protectingAnimalCount, adoptedAnimalCount, reportedAnimalCount);
        } catch (ExecutionException | InterruptedException e) {
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isAdoptedAnimalCount(RescueAnimalStatsServiceApiResponse.Item item) {
        return "chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.regoin()) && "입양".equals(item.prcesssName());
    }

    private static boolean isProtectingAnimalTotalCount(RescueAnimalStatsServiceApiResponse.Item item) {
        return "chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.regoin()) && "보호중".equals(item.prcesssName());
    }
}
