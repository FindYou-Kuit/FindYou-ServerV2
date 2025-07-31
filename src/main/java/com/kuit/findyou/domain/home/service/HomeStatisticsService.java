package com.kuit.findyou.domain.home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.GetHomeResponse;
import com.kuit.findyou.domain.home.dto.LossInfoServiceApiResponse;
import com.kuit.findyou.domain.home.dto.RescueAnimalStatsServiceApiResponse;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.external.dto.ProtectingAnimalApiFullResponse;
import com.kuit.findyou.global.common.external.properties.ProtectingAnimalApiProperties;
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

@Slf4j
@Service
public class HomeStatisticsService {
    @Value("${openapi.protecting-animal.api-key}")
    private String serviceKey;
    private final ProtectingAnimalApiProperties protectingAnimalApiProperties;
    private final RestClient protectingAnimalRestClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String REDIS_KEY_FOR_CACHED_STATISTICS = "home:statistics";
    public HomeStatisticsService(
            ProtectingAnimalApiProperties protectingAnimalApiProperties,
            @Qualifier("protectingAnimalRestClient") RestClient protectingAnimalRestClient,
            RedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ){
        this.protectingAnimalApiProperties = protectingAnimalApiProperties;
        this.protectingAnimalRestClient = protectingAnimalRestClient;
        this.redisTemplate = redisTemplate;
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

        RestClient restClient = RestClient.create();
        RescueAnimalStatsServiceApiResponse response1 = restClient.get()
                .uri("https://apis.data.go.kr/1543061/rescueAnimalStatsService/rescueAnimalStats?serviceKey=" + serviceKey+ "&bgnde="+ bgnde+ "&endde=" + endde+ "&_type=json")
                .retrieve()
                .body(RescueAnimalStatsServiceApiResponse.class);

        String protectingAnimalCount = "0";
        String adoptedAnimalCount = "0";
        for(RescueAnimalStatsServiceApiResponse.Item item : response1.response().body().items().item()){
            if("chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.regoin()) && "보호중".equals(item.prcesssName())){
                protectingAnimalCount = item.total();
            }
            else if("chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.regoin()) && "입양".equals(item.prcesssName())){
                adoptedAnimalCount = item.total();
            }
        }

        log.info("protectingAnimalCount = {} adoptedAnimalCount = {}", protectingAnimalCount, adoptedAnimalCount);

        ProtectingAnimalApiFullResponse response2 = protectingAnimalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/abandonmentPublic_v2")
                        .queryParam("serviceKey", protectingAnimalApiProperties.apiKey())
                        .queryParam("bgnde", bgnde)
                        .queryParam("endde", endde)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .body(ProtectingAnimalApiFullResponse.class);

        String rescuedAnimalCount = response2.response().body().totalCount();

        log.info("rescuedAnimalCount = {}", rescuedAnimalCount);


        restClient = RestClient.create();
        LossInfoServiceApiResponse response3 = restClient.get()
                .uri("https://apis.data.go.kr/1543061/lossInfoService/lossInfo?serviceKey=" + serviceKey + "&bgnde=" + bgnde + "&endde=" + endde + "&_type=json")
                .retrieve()
                .body(LossInfoServiceApiResponse.class);

        String reportedAnimalCount = response3.response().body().totalCount();

        log.info("reportedAnimalCount = {}", reportedAnimalCount);

        GetHomeResponse.Statistics statistics = new GetHomeResponse.Statistics(reportedAnimalCount, protectingAnimalCount, adoptedAnimalCount, reportedAnimalCount);
        return statistics;
    }
}
