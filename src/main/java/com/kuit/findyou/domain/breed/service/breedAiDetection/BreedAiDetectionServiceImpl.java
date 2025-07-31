package com.kuit.findyou.domain.breed.service.breedAiDetection;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.global.external.client.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BreedAiDetectionServiceImpl implements BreedAiDetectionService{

    private final OpenAiClient openAiClient;

    @Override
    public BreedAiDetectionResponseDTO analyzeBreedWithAi(String imageUrl) {
        return openAiClient.analyzeImage(imageUrl);
    }
}
