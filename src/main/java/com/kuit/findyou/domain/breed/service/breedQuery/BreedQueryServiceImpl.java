package com.kuit.findyou.domain.breed.service.breedQuery;

import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.model.Species;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kuit.findyou.domain.breed.model.Species.*;

@Service
@RequiredArgsConstructor
public class BreedQueryServiceImpl implements BreedQueryService {

    private final BreedRepository breedRepository;

    @Override
    public BreedListResponseDTO getBreedList() {
        List<Breed> breeds = breedRepository.findAll(); // 단일 쿼리

        // species 별로 Map<String, List<String>> 으로 그룹핑
        Map<String, List<String>> grouped = breeds.stream()
                .collect(Collectors.groupingBy(
                        Breed::getSpecies,
                        Collectors.mapping(Breed::getName, Collectors.toList())
                ));

        List<String> dogBreeds = grouped.getOrDefault(DOG.getValue(), List.of());
        List<String> catBreeds = grouped.getOrDefault(CAT.getValue(), List.of());
        List<String> etcBreeds = grouped.getOrDefault(ETC.getValue(), List.of());

        return new BreedListResponseDTO(dogBreeds, catBreeds, etcBreeds);
    }

}
