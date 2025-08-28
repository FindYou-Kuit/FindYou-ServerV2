package com.kuit.findyou.domain.information.service.facade;

import com.kuit.findyou.domain.information.dto.*;
import com.kuit.findyou.domain.information.service.AnimalDepartmentService;
import com.kuit.findyou.domain.information.service.AnimalShelterService;
import com.kuit.findyou.domain.information.service.RecommendedContentService;
import com.kuit.findyou.domain.information.service.VolunteerWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InformationServiceFacade {

    private final AnimalShelterService animalShelterService;
    private final AnimalDepartmentService animalDepartmentService;
    private final RecommendedContentService contentService;
    private final VolunteerWorkService volunteerWorkService;

    public List<AnimalShelterResponse> getShelters(Long lastId, String type, String sido, String sigungu, Double lat, Double lng, int size) {
        return animalShelterService.getShelters(lastId, type, sido, sigungu, lat, lng, size);
    }

    public List<AnimalShelterResponse> getNearbyCenters(Long lastId, double lat, double lng, int size) {
        return animalShelterService.getNearbyCenters(lastId, lat, lng, size);
    }

    public GetAnimalDepartmentsResponse getDepartments(Long lastId, int size, String district) {
        return animalDepartmentService.getDepartments(lastId, size, district);
    }

    public List<RecommendedContentResponse> getRecommendedContents(ContentType type) {
        return contentService.getContents(type);
    }

    public GetVolunteerWorksResponse getVolunteerWorks(Long lastId) {
        return volunteerWorkService.getVolunteerWorksByCursor(lastId, 20);
    }
}