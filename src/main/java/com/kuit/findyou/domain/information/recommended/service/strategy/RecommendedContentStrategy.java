package com.kuit.findyou.domain.information.recommended.service.strategy;

import com.kuit.findyou.domain.information.recommended.dto.RecommendedContentResponse;

import java.util.List;

public interface RecommendedContentStrategy {
    List<RecommendedContentResponse> getRecommendedContents();
}
