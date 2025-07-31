package com.kuit.findyou.global.external.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GptChatResponseParser {

    /**
     * 응답 문자열에서 축종(Species)을 추출
     *
     * @param input GPT 응답 문자열 (예: "개,푸들,하얀색")
     * @return 축종 문자열 (예: "개"), 없으면 null
     */
    public static String parseSpecies(String input) {
        List<String> parts = parseParts(input);

        return !parts.isEmpty() ? parts.get(0) : null;
    }

    /**
     * 응답 문자열에서 품종(Breed)을 추출
     *
     * @param input GPT 응답 문자열 (예: "개,푸들,하얀색")
     * @return 품종 문자열 (예: "푸들"), 없으면 null
     */
    public static String parseBreed(String input) {
        List<String> parts = parseParts(input);

        return parts.size() >= 2 ? parts.get(1) : null;
    }

    /**
     * 응답 문자열에서 색상(Color) 목록을 추출
     *
     * @param input GPT 응답 문자열 (예: "개,푸들,하얀색,검은색")
     * @return 색상 문자열 리스트 (예: ["하얀색", "검은색"]), 없으면 빈 리스트 반환
     */
    public static List<String> parseColors(String input) {
        List<String> parts = parseParts(input);

        return parts.size() >= 3 ? parts.subList(2, parts.size()) : Collections.emptyList();
    }

    /**
     * 입력 문자열을 쉼표(,) 기준으로 분리하고 공백을 제거한 리스트로 반환합니다.
     *
     * @param input GPT 응답 원본 문자열
     * @return 축종, 품종, 색상 항목을 포함하는 문자열 리스트
     */
    private static List<String> parseParts(String input) {
        if (input == null || input.isBlank()) return Collections.emptyList();

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .toList();
    }
}
