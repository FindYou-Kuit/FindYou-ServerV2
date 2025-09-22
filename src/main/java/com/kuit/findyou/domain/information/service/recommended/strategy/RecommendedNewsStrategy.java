package com.kuit.findyou.domain.information.service.recommended.strategy;

import com.kuit.findyou.domain.information.dto.response.RecommendedContentResponse;
import com.kuit.findyou.domain.information.repository.RecommendedNewsRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.RECOMMENDED_NEWS_NOT_FOUND;

@RequiredArgsConstructor
@Component
public class RecommendedNewsStrategy implements RecommendedContentStrategy {

    private final RecommendedNewsRepository newsRepository;

    @Override
    public List<RecommendedContentResponse> getRecommendedContents() {
        List<RecommendedContentResponse> contents = newsRepository.findAll().stream()
                .map(news -> new RecommendedContentResponse(
                        news.getTitle(),
                        news.getUploader(),
                        news.getUrl()
                ))
                .toList();
        if (contents.isEmpty()) {
            throw new CustomException(RECOMMENDED_NEWS_NOT_FOUND);
        }
        return contents;
    }
}
