package com.kuit.findyou.global.external.client;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.model.Species;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.dto.OpenAiResponse;
import com.kuit.findyou.global.external.util.OpenAiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kuit.findyou.domain.breed.model.Species.*;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BREED_ANALYSIS_FAILED;

@Component
@Slf4j
public class OpenAiClient {

    private final RestClient openAiRestClient;

    public OpenAiClient(@Qualifier("openAiRestClient") RestClient openAiRestClient, BreedRepository breedRepository) {
        this.openAiRestClient = openAiRestClient;
    }

    public BreedAiDetectionResponseDTO analyzeImage(String imageUrl, String prompt) {
        try {

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o",
                    "max_tokens", 50,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "image_url",
                                                    "image_url", Map.of("url", imageUrl)
                                            ),
                                            Map.of(
                                                    "type", "text",
                                                    "text", prompt
                                            )
                                    )
                            )
                    )
            );

            OpenAiResponse response = openAiRestClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                log.warn("OpenAI Vision API 응답이 비어있습니다.");
                throw new CustomException(BREED_ANALYSIS_FAILED);
            }

            String content = response.choices().get(0).message().content();
            log.info("OpenAI Vision API 응답: {}", content);

            String species = OpenAiParser.parseSpecies(content);
            String breed = OpenAiParser.parseBreed(content);
            List<String> colors = OpenAiParser.parseColors(content);

            return new BreedAiDetectionResponseDTO(species, breed, colors);

        } catch (Exception e) {
            log.error("OpenAI Vision API 호출 중 오류 발생", e);
            throw new CustomException(BREED_ANALYSIS_FAILED);
        }
    }
}