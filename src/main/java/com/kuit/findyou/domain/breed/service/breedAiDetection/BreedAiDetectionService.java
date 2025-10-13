package com.kuit.findyou.domain.breed.service.breedAiDetection;


import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;

public interface BreedAiDetectionService {

    BreedAiDetectionResponseDTO analyzeBreedWithAi(String base64Image);
}
