package com.kuit.findyou.domain.breed.service.breedAiDetection;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.domain.breed.util.BreedGroupingUtil;
import com.kuit.findyou.global.external.client.OpenAiClient;
import com.kuit.findyou.global.external.util.OpenAiPromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class BreedAiDetectionServiceImpl implements BreedAiDetectionService{

    private final OpenAiClient openAiClient;
    private final BreedRepository breedRepository;

    @Override
    public BreedAiDetectionResponseDTO analyzeBreedWithAi(String imageUrl) {
        List<Breed> breeds = breedRepository.findAll();

        Map<String, List<String>> breedGroup = BreedGroupingUtil.getGroupedBreedNamesBySpecies(breeds);

        String prompt = OpenAiPromptBuilder.buildBreedDetectionPrompt(breedGroup);

        return openAiClient.analyzeImage(imageUrl, prompt);
    }
}
