package com.kuit.findyou.domain.information.service.recommended.strategy;

import com.kuit.findyou.domain.information.dto.response.RecommendedContentResponse;

import java.util.List;

public interface RecommendedContentStrategy {
    List<RecommendedContentResponse> getRecommendedContents();
}
