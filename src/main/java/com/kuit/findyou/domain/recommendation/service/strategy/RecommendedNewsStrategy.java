package com.kuit.findyou.domain.recommendation.service.strategy;

import com.kuit.findyou.domain.recommendation.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.recommendation.repository.RecommendedNewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RecommendedNewsStrategy implements RecommendedContentStrategy {

    private final RecommendedNewsRepository newsRepository;

    @Override
    public List<RecommendedContentResponse> getRecommendedContents() {
        return newsRepository.findAll().stream()
                .map(news -> new RecommendedContentResponse(
                        news.getTitle(),
                        news.getUploader(),
                        news.getUrl()
                ))
                .toList();
    }
}
