package com.kuit.findyou.domain.recommendation.service.strategy;

import com.kuit.findyou.domain.recommendation.dto.ContentType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RecommendedContentStrategyConfig {

    @Bean
    public Map<ContentType, RecommendedContentStrategy> strategyMap(
            RecommendedVideoStrategy videoStrategy,
            RecommendedNewsStrategy articleStrategy
    ) {
        return Map.of(
                ContentType.VIDEO, videoStrategy,
                ContentType.ARTICLE, articleStrategy
        );
    }
}
