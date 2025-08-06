package com.kuit.findyou.domain.recommendation.service.strategy;

import com.kuit.findyou.domain.recommendation.dto.RecommendedContentResponse;

import java.util.List;

public interface RecommendedContentStrategy {
    List<RecommendedContentResponse> getRecommendedContents();
}
