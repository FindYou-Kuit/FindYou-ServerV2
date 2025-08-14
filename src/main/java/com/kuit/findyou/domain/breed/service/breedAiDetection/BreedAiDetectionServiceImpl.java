package com.kuit.findyou.domain.breed.service.breedAiDetection;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.domain.breed.util.BreedGroupingUtil;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.OpenAiClient;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import com.kuit.findyou.global.external.exception.OpenAiParsingException;
import com.kuit.findyou.global.external.util.OpenAiParser;
import com.kuit.findyou.global.external.util.OpenAiPromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BREED_ANALYSIS_FAILED;

@Slf4j
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
            String aiResponse =  openAiClient.analyzeImage(imageUrl, prompt);

            String species = OpenAiParser.parseSpecies(aiResponse);
            String breed = OpenAiParser.parseBreed(aiResponse, species, breedGroup);
            List<String> colors = OpenAiParser.parseColors(aiResponse);

            return new BreedAiDetectionResponseDTO(species, breed, colors);
        } catch (OpenAiClientException | OpenAiParsingException e) {
            log.warn("품종 판별 실패: {}", e.getMessage());
            throw new CustomException(BREED_ANALYSIS_FAILED);
        }
    }

}
