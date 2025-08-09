package com.kuit.findyou.domain.information.service.facade;

import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.service.RecommendedContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecommendedContentFacade {

    private final RecommendedContentService contentService;

    public List<RecommendedContentResponse> getRecommendedContents(ContentType type) {
        return contentService.getContents(type);
    }
}