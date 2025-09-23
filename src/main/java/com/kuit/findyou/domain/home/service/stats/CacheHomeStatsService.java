package com.kuit.findyou.domain.home.service.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheHomeStatsService {
    @Value("${findyou.cache.home-stats-key}")
    private String REDIS_CACHE_KEY = "home:statistics";
    @Value("${findyou.cache.home-stats-ttl}")
    private Duration HOME_STATS_TTL = Duration.ofHours(24);
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    public void cacheTotalStatistics(GetHomeResponse.TotalStatistics totalStats) {
        try{
            // 레디스에 캐싱
            String json = objectMapper.writeValueAsString(totalStats);
            redisTemplate.opsForValue().set(REDIS_CACHE_KEY, json, HOME_STATS_TTL);
        }
        catch (JsonProcessingException e){
            log.error("[getCachedTotalStatistics] json 역직렬화 오류");
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }

    public GetHomeResponse.TotalStatistics getCachedTotalStatistics(){
        try{
            String json = redisTemplate.opsForValue().get(REDIS_CACHE_KEY);
            if(json == null){
                log.info("[getCachedTotalStatistics] 캐시에 데이터 없음");
                return null;
            }
            log.info("[getCachedTotalStatistics] 캐시에 데이터 있음 json = {}", json);
            return objectMapper.readValue(json, GetHomeResponse.TotalStatistics.class);
        }
        catch (JsonProcessingException e){
            log.error("[getCachedTotalStatistics] json 역직렬화 오류");
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }
}
