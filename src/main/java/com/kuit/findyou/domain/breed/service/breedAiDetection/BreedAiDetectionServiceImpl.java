package com.kuit.findyou.domain.breed.service.breedAiDetection;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.domain.breed.util.BreedGroupingUtil;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.OpenAiClient;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import com.kuit.findyou.global.external.util.OpenAiPromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BREED_ANALYSIS_FAILED;

@RequiredArgsConstructor
@Service
public class BreedAiDetectionServiceImpl implements BreedAiDetectionService{

    private final OpenAiClient openAiClient;
    private final BreedRepository breedRepository;

    @Override
    public BreedAiDetectionResponseDTO analyzeBreedWithAi(String imageUrl) {
        try {
            List<Breed> breeds = breedRepository.findAll(); // 실패 시 DataAccessException 등 자동 전파됨

            Map<String, List<String>> breedGroup = BreedGroupingUtil.getGroupedBreedNamesBySpecies(breeds);

            String prompt = OpenAiPromptBuilder.buildBreedDetectionPrompt(breedGroup);

            return openAiClient.analyzeImage(imageUrl, prompt);
        } catch (OpenAiClientException e) {
            throw new CustomException(BREED_ANALYSIS_FAILED);
        }
    }

}
