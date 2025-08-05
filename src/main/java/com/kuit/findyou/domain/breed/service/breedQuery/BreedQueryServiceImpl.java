package com.kuit.findyou.domain.breed.service.breedQuery;

import com.kuit.findyou.domain.breed.dto.response.BreedListResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.model.Species;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.domain.breed.util.BreedGroupingUtil;
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
        List<Breed> breeds = breedRepository.findAll();

        Map<String, List<String>> breedGroup = BreedGroupingUtil.getGroupedBreedNamesBySpecies(breeds);

        List<String> dogBreeds = breedGroup.getOrDefault(DOG.getValue(), List.of());
        List<String> catBreeds = breedGroup.getOrDefault(CAT.getValue(), List.of());
        List<String> etcBreeds = breedGroup.getOrDefault(ETC.getValue(), List.of());

        return new BreedListResponseDTO(dogBreeds, catBreeds, etcBreeds);
    }

}
