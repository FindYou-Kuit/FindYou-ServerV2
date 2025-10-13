package com.kuit.findyou.domain.breed.service.breedAiDetection;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.domain.breed.util.BreedGroupingUtil;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.OpenAiClient;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import com.kuit.findyou.global.external.exception.OpenAiResponseValidatingException;
import com.kuit.findyou.global.external.util.OpenAiPromptBuilder;
import com.kuit.findyou.global.external.util.OpenAiResponseValidator;
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
    public BreedAiDetectionResponseDTO analyzeBreedWithAi(String base64Image) {
        try {
            List<Breed> breeds = breedRepository.findAll(); // 실패 시 DataAccessException 등 자동 전파됨

            Map<String, List<String>> breedGroup = BreedGroupingUtil.getGroupedBreedNamesBySpecies(breeds);

            String prompt = OpenAiPromptBuilder.buildBreedDetectionPrompt(breedGroup);

            return OpenAiResponseValidator.validateOpenAiResponse(openAiClient.analyzeImage(base64Image, prompt), breedGroup);
        } catch (OpenAiClientException | OpenAiResponseValidatingException e) {
            log.warn("품종 판별 실패: {}", e.getMessage());
            throw new CustomException(BREED_ANALYSIS_FAILED);
        }
    }

}
