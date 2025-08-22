package com.kuit.findyou.domain.information.recommended.service.strategy;

import com.kuit.findyou.domain.information.recommended.dto.ContentType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RecommendedContentStrategyConfig {

    @Bean
    public Map<ContentType, RecommendedContentStrategy> strategyMap(
            RecommendedVideoStrategy videoStrategy,
            RecommendedNewsStrategy newsStrategy
    ) {
        return Map.of(
                ContentType.VIDEO, videoStrategy,
                ContentType.NEWS, newsStrategy
        );
    }
}
