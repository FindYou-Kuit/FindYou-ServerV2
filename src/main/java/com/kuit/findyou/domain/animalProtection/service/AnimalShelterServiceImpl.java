package com.kuit.findyou.domain.animalProtection.service;

import com.kuit.findyou.domain.animalProtection.dto.AnimalShelterResponse;
import com.kuit.findyou.domain.animalProtection.model.AnimalShelter;
import com.kuit.findyou.domain.animalProtection.repository.AnimalShelterRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class AnimalShelterServiceImpl implements AnimalShelterService {

    private final AnimalShelterRepository animalShelterRepository;

    @Override
    public List<AnimalShelterResponse> getShelters(Long userId, Long lastId, String type, String sido, String sigungu, Double lat, Double lng) {

        if (lastId == null || type == null) {
            throw new CustomException(BAD_REQUEST);
        }
        String hospital = "병원"; //유형 필터에 사용되는 키워드

        //관할구역 필터
        String jurisdiction = (sido != null && !sido.isBlank() && sigungu != null && !sigungu.isBlank())
                ? sido + " " + sigungu
                : null;
        List<AnimalShelter> results = animalShelterRepository.findWithFilter(lastId, type, hospital, jurisdiction);

        return results.stream()
                .map(AnimalShelterResponse::from)
                .toList();
    }
}
