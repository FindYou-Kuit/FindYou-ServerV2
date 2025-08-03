package com.kuit.findyou.domain.home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.dto.LossInfoServiceApiResponse;
import com.kuit.findyou.domain.home.dto.RescueAnimalStatsServiceApiResponse;
import com.kuit.findyou.domain.home.exception.CacheUpdateFailedException;
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

    public GetHomeResponse.TotalStatistics updateAndGet(){
        try {
            return updateTotalStatistics();
        } catch (CacheUpdateFailedException e) {
            cacheTotalStatistics(GetHomeResponse.TotalStatistics.empty());
            return GetHomeResponse.TotalStatistics.empty();
        }
    }

    public GetHomeResponse.TotalStatistics updateTotalStatistics() throws CacheUpdateFailedException {
        log.info("[updateTotalStatistics] 캐시 업데이트 작업 시작");

        // 모든 통계 구하기
        CompletableFuture<GetHomeResponse.Statistics> recent7DaysFuture = parseStatisticsAsync(7);
        CompletableFuture<GetHomeResponse.Statistics> recent3MonthsFuture = parseStatisticsAsync(90);
        CompletableFuture<GetHomeResponse.Statistics> recent1YearFuture = parseStatisticsAsync(365);

        try{
            GetHomeResponse.Statistics recent7DaysStatistics = recent7DaysFuture.get();
            GetHomeResponse.Statistics recent3MonthsStatistics = recent3MonthsFuture.get();
            GetHomeResponse.Statistics recent1YearStatistics = recent1YearFuture.get();
            GetHomeResponse.TotalStatistics totalStatistics = new GetHomeResponse.TotalStatistics(recent7DaysStatistics, recent3MonthsStatistics, recent1YearStatistics);

            // 레디스에 저장
            cacheTotalStatistics(totalStatistics);

            log.info("[updateTotalStatistics] 캐시 업데이트 작업 완료");

            return totalStatistics;

        } catch (ExecutionException | InterruptedException e) {
            log.error("[updateTotalStatistics] 예외 발생", e);
            throw new CacheUpdateFailedException();
        }
    }

    private CompletableFuture<GetHomeResponse.Statistics> parseStatisticsAsync(int dayOfPeriod) {
        return CompletableFuture.supplyAsync(() -> parseStatistics(dayOfPeriod), statisticsExecutor);
    }

    public void cacheTotalStatistics(GetHomeResponse.TotalStatistics totalStats) {
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
            log.info("[getCachedTotalStatistics] 캐시에 데이터 있음 json = {}", json);
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

        CompletableFuture<String> protectingAndAdoptedFuture = parseProtectingAndAdoptedAnimalCountAsync(bgnde, endde);
        CompletableFuture<String> rescuedFuture = parseRescuedAnimalCountAsync(bgnde, endde);
        CompletableFuture<String> reportedFuture = parseReportedAnimalCountAsync(bgnde, endde);

        try{
            String[] token = protectingAndAdoptedFuture.get().split(",");
            String protectingAnimalCount = token[0];
            String adoptedAnimalCount = token[1];
            String reportedAnimalCount = reportedFuture.get();
            String rescuedAnimalCount = rescuedFuture.get();

            log.info("파싱 결과 protectingAnimalCount = {} adoptedAnimalCount = {} lostAnimalCount = {} rescuedAnimalCount = {}", protectingAnimalCount, adoptedAnimalCount, reportedAnimalCount, rescuedAnimalCount);

            return new GetHomeResponse.Statistics(rescuedAnimalCount, protectingAnimalCount, adoptedAnimalCount, reportedAnimalCount);
        } catch (ExecutionException | InterruptedException e) {
            log.error("[parseStatistics] 예외 발생", e);
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }

    private CompletableFuture<String> parseReportedAnimalCountAsync(String bgnde, String endde) {
        return CompletableFuture.supplyAsync(() -> parseReportedAnimalCount(bgnde, endde), statisticsExecutor);
    }

    private CompletableFuture<String> parseRescuedAnimalCountAsync(String bgnde, String endde) {
        return CompletableFuture.supplyAsync(()-> parseRescuedAnimalCount(bgnde, endde), statisticsExecutor);
    }

    private CompletableFuture<String> parseProtectingAndAdoptedAnimalCountAsync(String bgnde, String endde) {
        return CompletableFuture.supplyAsync(()-> parseProtectingAndAdoptedAnimalCount(bgnde, endde), statisticsExecutor);
    }

    private String parseReportedAnimalCount(String bgnde, String endde) {
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
    }

    private String parseRescuedAnimalCount(String bgnde, String endde) {
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
    }

    private String parseProtectingAndAdoptedAnimalCount(String bgnde, String endde) {
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
    }

    private static boolean isAdoptedAnimalCount(RescueAnimalStatsServiceApiResponse.Item item) {
        return "chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.regoin()) && "입양".equals(item.prcesssName());
    }

    private static boolean isProtectingAnimalTotalCount(RescueAnimalStatsServiceApiResponse.Item item) {
        return "chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.regoin()) && "보호중".equals(item.prcesssName());
    }
}
