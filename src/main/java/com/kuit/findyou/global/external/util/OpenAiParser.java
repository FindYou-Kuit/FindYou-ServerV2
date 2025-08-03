package com.kuit.findyou.global.external.util;

import com.kuit.findyou.global.external.exception.OpenAiParsingException;

import java.util.Arrays;
import java.util.List;

public class OpenAiParser {

    /**
     * 응답 문자열에서 축종(Species)을 추출
     *
     * @param input GPT 응답 문자열 (예: "개,푸들,하얀색")
     * @return 축종 문자열 (예: "개")
     * @throws OpenAiParsingException 포맷이 잘못되었거나 누락 시
     */
    public static String parseSpecies(String input) {
        List<String> parts = parseParts(input);
        return parts.get(0); // 이미 유효성 체크됨
    }

    /**
     * 응답 문자열에서 품종(Breed)을 추출
     *
     * @param input GPT 응답 문자열 (예: "개,푸들,하얀색")
     * @return 품종 문자열 (예: "푸들")
     * @throws OpenAiParsingException 포맷이 잘못되었거나 누락 시
     */
    public static String parseBreed(String input) {
        List<String> parts = parseParts(input);
        return parts.get(1);
    }

    /**
     * 응답 문자열에서 색상(Color) 목록을 추출
     *
     * @param input GPT 응답 문자열 (예: "개,푸들,하얀색,검은색")
     * @return 색상 문자열 리스트 (예: ["하얀색", "검은색"])
     * @throws OpenAiParsingException 포맷이 잘못되었거나 누락 시
     */
    public static List<String> parseColors(String input) {
        List<String> parts = parseParts(input);
        return parts.subList(2, parts.size());
    }

    /**
     * 입력 문자열을 쉼표(,) 기준으로 분리하고 유효성 검사를 수행합니다.
     *
     * @param input GPT 응답 문자열
     * @return 축종, 품종, 색상 등을 포함하는 파싱된 리스트
     * @throws OpenAiParsingException 포맷이 잘못되었거나 항목 수 부족 시
     */
    private static List<String> parseParts(String input) {
        if (input == null || input.isBlank()) {
            throw new OpenAiParsingException("입력이 null 이거나 비어 있습니다.");
        }

        if (!input.contains(",")) {
            throw new OpenAiParsingException("입력에 쉼표(,) 구분자가 없습니다. 잘못된 포맷입니다: " + input);
        }

        List<String> parts = Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (parts.size() < 3) {
            throw new OpenAiParsingException("입력 항목이 부족합니다. 최소 3개(축종, 품종, 색상)가 필요합니다. 입력: " + input);
        }

        return parts;
    }
}
