package com.kuit.findyou.domain.breed.service.breedQuery;

import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.model.Species;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.kuit.findyou.domain.breed.model.Species.*;

@Service
@RequiredArgsConstructor
public class BreedQueryServiceImpl implements BreedQueryService {

    private final BreedRepository breedRepository;

    @Override
    public BreedListResponseDTO getBreedList() {
        List<String> dogBreedList = breedRepository.findBreedsBySpecies(DOG.getValue())
                .stream()
                .map(Breed::getName)
                .toList();

        List<String> catBreedList = breedRepository.findBreedsBySpecies(CAT.getValue())
                .stream()
                .map(Breed::getName)
                .toList();

        List<String> etcBreedList = breedRepository.findBreedsBySpecies(ETC.getValue())
                .stream()
                .map(Breed::getName)
                .toList();


        return new BreedListResponseDTO(dogBreedList, catBreedList, etcBreedList);
    }
}
