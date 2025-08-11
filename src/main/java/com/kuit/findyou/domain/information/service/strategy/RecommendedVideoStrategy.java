package com.kuit.findyou.domain.information.service.strategy;

import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.repository.RecommendedVideoRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.RECOMMENDED_VIDEO_NOT_FOUND;

@RequiredArgsConstructor
@Component
public class RecommendedVideoStrategy implements RecommendedContentStrategy {

    private final RecommendedVideoRepository videoRepository;

    @Override
    public List<RecommendedContentResponse> getRecommendedContents() {
        List<RecommendedContentResponse> contents = videoRepository.findAll().stream()
                .map(video -> new RecommendedContentResponse(
                        video.getTitle(),
                        video.getUploader(),
                        video.getUrl()
                ))
                .toList();
        if (contents.isEmpty()) {
            throw new CustomException(RECOMMENDED_VIDEO_NOT_FOUND);
        }
        return contents;
    }
}
