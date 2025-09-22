package com.kuit.findyou.domain.home.service.stats;

import com.kuit.findyou.domain.home.dto.response.GetHomeResponse;
import com.kuit.findyou.domain.home.exception.CacheUpdateFailedException;

public interface HomeStatisticsService {
    GetHomeResponse.TotalStatistics get();

    GetHomeResponse.TotalStatistics update() throws CacheUpdateFailedException;

    void extendCacheExpiration();
}
