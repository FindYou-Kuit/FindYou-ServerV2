package com.kuit.findyou.domain.breed.service.facade;

import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.breed.service.breedQuery.BreedQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BreedServiceFacade {

    private final BreedQueryService breedQueryService;

    public BreedListResponseDTO getBreedList() {
        return breedQueryService.getBreedList();
    }
}
