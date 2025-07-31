package com.kuit.findyou.domain.breed.util;

import com.kuit.findyou.domain.breed.model.Breed;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BreedGroupingUtil {

    static public Map<String, List<String>> getGroupedBreedNamesBySpecies(List<Breed> breeds) {
        // species 별로 Map<String, List<String>> 으로 그룹핑
        return breeds.stream()
                .collect(Collectors.groupingBy(
                        Breed::getSpecies,
                        Collectors.mapping(Breed::getName, Collectors.toList())
                ));
    }
}
