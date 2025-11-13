package com.kuit.findyou.domain.home.service.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.common.service.CacheSnapshotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeCacheSnapshotService {
    private String HOME_STATS_CACHE_KEY = "home:statistics";
    private final CacheSnapshotService cacheSnapshotService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveHomeStats(GetHomeResponse.TotalStatistics stats) {
        try {
            String json = objectMapper.writeValueAsString(stats);
            cacheSnapshotService.saveJsonCache(HOME_STATS_CACHE_KEY, json);
        }
        catch (JsonProcessingException e){
            log.error("[getCachedTotalStatistics] json 역직렬화 오류");
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<GetHomeResponse.TotalStatistics> findHomeStats() {
        try{
            Optional<String> json = cacheSnapshotService.findJsonCache(HOME_STATS_CACHE_KEY);
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
