package com.kuit.findyou.global.external.util;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;
import com.kuit.findyou.global.external.exception.OpenAiParsingException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;

public class OpenAiParser {

    // 허용된 축종 목록
    private static final Set<String> VALID_SPECIES = Set.of("강아지", "고양이", "기타");

    // 허용된 색상 목록
    private static final Set<String> VALID_COLORS = Set.of(
            "검은색", "노란색", "점박이", "하얀색", "갈색", "회색", "적색", "기타"
    );

    /**
     * 응답 문자열에서 축종(Species)을 추출하고 검증
     */
    public static String parseSpecies(String input) {
        List<String> parts = parseParts(input);
        String species = cleanString(parts.get(0));

        if (!VALID_SPECIES.contains(species)) {
            throw new OpenAiParsingException(OPENAI_PARSER_SPECIES_INVALID);
        }

        return species;
    }

    /**
     * 응답 문자열에서 품종(Breed)을 추출하고 정제
     */
    public static String parseBreed(String input, String species, Map<String, List<String>> breedGroup) {
        List<String> parts = parseParts(input);
        String breed = cleanString(parts.get(1));

        return validateBreed(breed, species, breedGroup);
    }

    /**
     * 품종이 해당 축종의 유효한 품종인지 검증
     */
    private static String validateBreed(String breed, String species, Map<String, List<String>> breedGroup) {
        List<String> validBreeds = breedGroup.get(species);

        if (validBreeds == null || validBreeds.isEmpty()) {
            throw new OpenAiParsingException(OPENAI_PARSER_BREED_GROUP_EMPTY);
        }

        // 유효하지 않은 품종인 경우
        if (!validBreeds.contains(breed)) {
            throw new OpenAiParsingException(OPENAI_PARSER_BREED_INVALID);
        }

        return breed;
    }

    /**
     * 응답 문자열에서 색상(Color) 목록을 추출하고 검증
     */
    public static List<String> parseColors(String input) {
        List<String> parts = parseParts(input);

        List<String> validColors = parts.subList(2, parts.size()).stream()
                .map(OpenAiParser::cleanString)
                .filter(VALID_COLORS::contains)
                .distinct()
                .toList();

        if (validColors.isEmpty()) {
            throw new OpenAiParsingException(OPENAI_PARSER_COLORS_EMPTY);
        }

        return validColors;
    }

    /**
     * 입력 문자열을 쉼표(,) 기준으로 분리하고 유효성 검사를 수행
     */
    private static List<String> parseParts(String input) {
        if (input == null || input.isBlank()) {
            throw new OpenAiParsingException(OPENAI_PARSER_INPUT_NULL_OR_BLANK);
        }

        // GPT 응답 정제
        String cleanedInput = cleanResponse(input);

        if (!cleanedInput.contains(",")) {
            throw new OpenAiParsingException(OPENAI_PARSER_NO_COMMA_DELIMITER);
        }

        List<String> parts = Arrays.stream(cleanedInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (parts.size() < 3) {
            throw new OpenAiParsingException(OPENAI_PARSER_PARTS_TOO_FEW);
        }

        return parts;
    }

    /**
     * GPT 응답에서 불필요한 문자들을 제거하여 정제
     */
    private static String cleanResponse(String input) {
        if (input == null) {
            throw new OpenAiParsingException(OPENAI_PARSER_INPUT_NULL_OR_BLANK);
        }

        String result = input
                .replaceAll("\\\\+n?", "")     // 백슬래시와 \n 제거
                .replaceAll("\\r?\\n", "")     // 모든 개행문자 제거
                .replaceAll("\\r", "")         // 캐리지 리턴 제거
                .replaceAll("^[^가-힣a-zA-Z]*", "") // 앞의 특수문자 제거 (한글/영문이 나올 때까지)
                .trim();

        if (result.isBlank()) {
            throw new OpenAiParsingException(OPENAI_PARSER_INPUT_NULL_OR_BLANK);
        }

        return result;
    }

    /**
     * 개별 문자열 정제 (축종, 품종, 색상용)
     */
    private static String cleanString(String input) {
        if (input == null) {
            throw new OpenAiParsingException(OPENAI_PARSER_INPUT_NULL_OR_BLANK);
        }

        String result = input
                .replaceAll("\\\\+", "")      // 백슬래시 제거
                .replaceAll("[\\r\\n]", "")   // 개행문자 제거
                .replaceAll("^\"|\"$", "")    // 앞뒤 따옴표 제거
                .trim();

        if (result.isBlank()) {
            throw new OpenAiParsingException(OPENAI_PARSER_INPUT_NULL_OR_BLANK);
        }

        return result;
    }
}
