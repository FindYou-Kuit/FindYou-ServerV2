package com.kuit.findyou.domain.home.service.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.domain.home.repository.CacheSnapshotRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeStatsCacheSnapshotService {
    @Value("${findyou.cache.home-stats-key}")
    private String REDIS_CACHE_KEY = "home:statistics";
    private final CacheSnapshotRepository cacheSnapshotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(GetHomeResponse.TotalStatistics stats) {
        try{
            String json = objectMapper.writeValueAsString(stats);
            // DB에도 캐싱
            Optional<String> cache = cacheSnapshotRepository.find(REDIS_CACHE_KEY);
            if(cache.isPresent()){
                cacheSnapshotRepository.delete(REDIS_CACHE_KEY);
            }
            cacheSnapshotRepository.insert(REDIS_CACHE_KEY, json);
        }
        catch (JsonProcessingException e){
            log.error("[getCachedTotalStatistics] json 역직렬화 오류");
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }


    public Optional<GetHomeResponse.TotalStatistics> find() {
        try{
            Optional<String> json = cacheSnapshotRepository.find(REDIS_CACHE_KEY);
            if(json.isEmpty()){
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json.get(), GetHomeResponse.TotalStatistics.class));
        }
        catch (JsonProcessingException e){
            log.error("[getCachedTotalStatistics] json 역직렬화 오류");
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }
}
