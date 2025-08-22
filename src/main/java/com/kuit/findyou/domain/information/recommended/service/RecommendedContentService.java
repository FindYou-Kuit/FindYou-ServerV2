package com.kuit.findyou.domain.information.recommended.service;

import com.kuit.findyou.domain.information.recommended.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.recommended.dto.ContentType;
import com.kuit.findyou.domain.information.recommended.service.strategy.RecommendedContentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class RecommendedContentService {

    private final Map<ContentType, RecommendedContentStrategy> strategies;

    public List<RecommendedContentResponse> getContents(ContentType type) {
        log.info("[추천 콘텐츠 조회 요청] type = {}", type);

        RecommendedContentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("해당 콘텐츠 타입의 처리 전략이 존재하지 않습니다.");
        }

        return strategy.getRecommendedContents();
    }
}