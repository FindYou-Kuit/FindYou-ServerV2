package com.kuit.findyou.global.external.util;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.global.external.exception.OpenAiResponseValidatingException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;

public class OpenAiResponseValidator {

    private static final Set<String> VALID_SPECIES = Set.of("강아지", "고양이", "기타");
    private static final Set<String> VALID_COLORS  = Set.of(
            "검은색","노란색","점박이","하얀색","갈색","회색","적색","기타"
    );

    /**
     * Spring AI가 매핑한 DTO 를 받아 2차 검증 후 반환
     */
    public static BreedAiDetectionResponseDTO validateOpenAiResponse(
            BreedAiDetectionResponseDTO dto,
            Map<String, List<String>> breedGroup
    ) {
        if (dto == null) {
            throw new OpenAiResponseValidatingException(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK);
        }

        // 1. species 검증
        String species = nullSafeTrim(dto.species());
        if (!VALID_SPECIES.contains(species)) {
            throw new OpenAiResponseValidatingException(OPENAI_VALIDATOR_SPECIES_INVALID);
        }

        // 2. breed 검증
        String breed = nullSafeTrim(dto.breed());
        List<String> validBreeds = Optional.ofNullable(breedGroup.get(species))
                .orElseThrow(() -> new OpenAiResponseValidatingException(OPENAI_VALIDATOR_BREED_GROUP_EMPTY));

        if (!validBreeds.contains(breed)) {
            throw new OpenAiResponseValidatingException(OPENAI_VALIDATOR_BREED_INVALID);
        }

        // 3. colors 검증
        List<String> colors = Optional.ofNullable(dto.furColors()).orElse(List.of());
        List<String> validColors = colors.stream()
                .map(OpenAiResponseValidator::nullSafeTrim)
                .filter(VALID_COLORS::contains)
                .distinct()
                .collect(Collectors.toList());

        if (validColors.isEmpty()) {
            throw new OpenAiResponseValidatingException(OPENAI_VALIDATOR_COLORS_INVALID);
        }

        return new BreedAiDetectionResponseDTO(species, breed, validColors);
    }

    private static String nullSafeTrim(String input) {
        if (input == null) {
            throw new OpenAiResponseValidatingException(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK);
        }

        String trimmedInput = input.trim();

        if (trimmedInput.isEmpty()) {
            throw new OpenAiResponseValidatingException(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK);
        }
        return trimmedInput;
    }
}
