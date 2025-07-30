package com.kuit.findyou.domain.recommendation.service.strategy;

import com.kuit.findyou.domain.recommendation.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.recommendation.repository.RecommendedArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RecommendedArticleStrategy implements RecommendedContentStrategy {

    private final RecommendedArticleRepository articleRepository;

    @Override
    public List<RecommendedContentResponse> getRecommendedContents() {
        return articleRepository.findAll().stream()
                .map(article -> new RecommendedContentResponse(
                        article.getTitle(),
                        article.getSource(), // uploader 역할
                        article.getUrl()
                ))
                .toList();
    }
}
