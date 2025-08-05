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

        String jurisdiction = (sido != null && sigungu != null) ? sido + " " + sigungu : null;

        List<AnimalShelter> results = animalShelterRepository.findWithFilter(lastId, type, jurisdiction);
        return List.of();
    }
}
