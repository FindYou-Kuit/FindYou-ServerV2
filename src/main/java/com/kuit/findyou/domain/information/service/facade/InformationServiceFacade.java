package com.kuit.findyou.domain.information.service.facade;

import com.kuit.findyou.domain.information.dto.AnimalShelterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.dto.GetVolunteerWorksResponse;
import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.service.animalShelter.AnimalShelterService;
import com.kuit.findyou.domain.information.service.recommended.RecommendedContentServiceImpl;
import com.kuit.findyou.domain.information.service.animalShelter.VolunteerWorkService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InformationServiceFacade {

    private final AnimalShelterService animalShelterService;
    private final RecommendedContentServiceImpl contentService;
    private final VolunteerWorkService volunteerWorkService;

    public AnimalShelterPagingResponse<AnimalShelterResponse> getShelters(Long lastId, String type, String sido, String sigungu, Double lat, Double lng, int size) {
        return animalShelterService.getShelters(lastId, type, sido, sigungu, lat, lng, size);
    }

    public AnimalShelterPagingResponse<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size) {
        return animalShelterService.getNearbyCenters(lastId, lat, lng, size);
    }

    public List<RecommendedContentResponse> getRecommendedContents(ContentType type) {
        return contentService.getContents(type);
    }

    public GetVolunteerWorksResponse getVolunteerWorks(Long lastId) {
        return volunteerWorkService.getVolunteerWorksByCursor(lastId, 20);
    }
}