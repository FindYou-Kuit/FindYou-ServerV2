package com.kuit.findyou.domain.information.service.facade;

import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.service.AnimalShelterService;
import com.kuit.findyou.domain.information.service.RecommendedContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InformationServiceFacade {

    private final AnimalShelterService animalShelterService;
    private final RecommendedContentService contentService;

    public List<AnimalShelterResponse> getShelters(Long lastId, String type, String sido, String sigungu, Double lat, Double lng) {
        return animalShelterService.getShelters(lastId, type, sido, sigungu, lat, lng);
    }

    public List<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng) {
        return animalShelterService.getNearbyCenters(lastId, lat, lng);
    }

    public List<RecommendedContentResponse> getRecommendedContents(ContentType type) {
        return contentService.getContents(type);
    }
}