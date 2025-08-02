package com.kuit.findyou.domain.recommendation.service;

import com.kuit.findyou.domain.recommendation.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.recommendation.dto.ContentType;
import com.kuit.findyou.domain.recommendation.service.strategy.RecommendedContentStrategy;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class RecommendedContentService {

    private final Map<ContentType, RecommendedContentStrategy> strategies;

    public List<RecommendedContentResponse> getContents(ContentType type) {
        log.info("[추천 콘텐츠 조회 요청] type = {}", type);

        RecommendedContentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            new CustomException(STRATEGY_NOT_FOUND);
        }

        return strategy.getRecommendedContents();
    }
}