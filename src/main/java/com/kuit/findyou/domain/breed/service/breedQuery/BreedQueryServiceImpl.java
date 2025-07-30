package com.kuit.findyou.domain.breed.service.breedQuery;

import com.kuit.findyou.domain.breed.dto.response.BreedDTO;
import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BreedQueryServiceImpl implements BreedQueryService {

    private final BreedRepository breedRepository;

    @Override
    public BreedListResponseDTO getBreedList() {
        return new BreedListResponseDTO(breedRepository.findAll().stream()
                .map(breed -> new BreedDTO(breed.getName(), breed.getSpecies()))
                .toList());
    }
}
