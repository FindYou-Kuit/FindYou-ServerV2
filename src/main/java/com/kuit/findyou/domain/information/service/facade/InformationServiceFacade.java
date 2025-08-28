package com.kuit.findyou.domain.information.service.facade;

import com.kuit.findyou.domain.information.dto.AnimalShelterPagingResponse;
import com.kuit.findyou.domain.information.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.information.dto.GetVolunteerWorksResponse;
import com.kuit.findyou.domain.information.dto.ContentType;
import com.kuit.findyou.domain.information.dto.RecommendedContentResponse;
import com.kuit.findyou.domain.information.service.animalShelter.AnimalCenterService;
import com.kuit.findyou.domain.information.service.recommended.RecommendedContentServiceImpl;
import com.kuit.findyou.domain.information.service.volunteerWork.VolunteerWorkService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InformationServiceFacade {

    private final AnimalCenterService animalCenterService;
    private final RecommendedContentServiceImpl contentService;
    private final VolunteerWorkService volunteerWorkService;

    public AnimalShelterPagingResponse<AnimalShelterResponse> getCenters(Long lastId, String sido, String sigungu, int size) {
        return animalCenterService.getCenters(lastId, sido, sigungu, size);
    }

    public AnimalShelterPagingResponse<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size) {
        return animalCenterService.getNearbyCenters(lastId, lat, lng, size);
    }

    public List<RecommendedContentResponse> getRecommendedContents(ContentType type) {
        return contentService.getContents(type);
    }

    public GetVolunteerWorksResponse getVolunteerWorks(Long lastId) {
        return volunteerWorkService.getVolunteerWorksByCursor(lastId, 20);
    }
}