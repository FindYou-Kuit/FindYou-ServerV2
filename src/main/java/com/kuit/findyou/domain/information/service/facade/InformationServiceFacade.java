package com.kuit.findyou.domain.information.service.facade;

import com.kuit.findyou.domain.information.dto.*;
import com.kuit.findyou.domain.information.service.AnimalDepartmentService;

import com.kuit.findyou.domain.information.service.animalCenter.AnimalCenterService;
import com.kuit.findyou.domain.information.service.recommended.RecommendedContentService;
import com.kuit.findyou.domain.information.service.volunteerWork.VolunteerWorkService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InformationServiceFacade {


    private final AnimalDepartmentService animalDepartmentService;
    private final AnimalCenterService animalCenterService;
    private final RecommendedContentService contentService;
    private final VolunteerWorkService volunteerWorkService;

    public AnimalCenterPagingResponse<AnimalCenterResponse> getCenters(Long lastId, String district, int size) {
        return animalCenterService.getCenters(lastId, district, size);
    }

    public AnimalCenterPagingResponse<AnimalCenterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size) {
        return animalCenterService.getNearbyCenters(lastId, lat, lng, size);
    }

    public GetAnimalDepartmentsResponse getDepartments(Long lastId, String district) {
        return animalDepartmentService.getDepartments(lastId, 20, district);
    }

    public List<RecommendedContentResponse> getRecommendedContents(ContentType type) {
        return contentService.getContents(type);
    }

    public GetVolunteerWorksResponse getVolunteerWorks(Long lastId) {
        return volunteerWorkService.getVolunteerWorksByCursor(lastId, 20);
    }
}