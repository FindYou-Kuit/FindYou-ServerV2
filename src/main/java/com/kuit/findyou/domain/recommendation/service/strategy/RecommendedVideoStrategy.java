package com.kuit.findyou.domain.recommendation.service.strategy;

import com.kuit.findyou.domain.recommendation.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.recommendation.repository.RecommendedVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RecommendedVideoStrategy implements RecommendedContentStrategy {

    private final RecommendedVideoRepository videoRepository;

    @Override
    public List<RecommendedContentResponse> getRecommendedContents() {
        return videoRepository.findAll().stream()
                .map(video -> new RecommendedContentResponse(
                        video.getTitle(),
                        video.getUploader(),
                        video.getUrl()
                ))
                .toList();
    }
}
