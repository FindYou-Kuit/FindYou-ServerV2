package com.kuit.findyou.domain.information.service.facade;

import com.kuit.findyou.domain.information.dto.*;
import com.kuit.findyou.domain.information.service.animalShelter.AnimalCenterService;
import com.kuit.findyou.domain.information.service.recommended.RecommendedContentService;
import com.kuit.findyou.domain.information.service.volunteerWork.VolunteerWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InformationServiceFacade {

    private final AnimalCenterService animalCenterService;
    private final RecommendedContentService contentService;
    private final VolunteerWorkService volunteerWorkService;

    public AnimalShelterPagingResponse<AnimalShelterResponse> getCenters(Long lastId, String district, int size) {
        return animalCenterService.getCenters(lastId, district, size);
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