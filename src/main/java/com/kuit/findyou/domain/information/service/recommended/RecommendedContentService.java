package com.kuit.findyou.domain.information.service.recommended;

import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.dto.response.RecommendedContentResponse;

import java.util.List;

public interface RecommendedContentService {
    List<RecommendedContentResponse> getContents(ContentType type);
}
