package com.kuit.findyou.global.external.util;

import com.kuit.findyou.domain.breed.model.Species;

import java.util.List;
import java.util.Map;

public class OpenAiPromptBuilder {

    private static final String FIXED_COLORS = "검은색,노란색,점박이,하얀색,갈색,회색,적색,기타";

    /**
     * 종별 품종 리스트를 기반으로 GPT Vision 프롬프트 문자열을 생성.
     */
    public static String buildBreedDetectionPrompt(Map<String, List<String>> groupedBreeds) {
        String dogBreeds = String.join(",", groupedBreeds.getOrDefault(Species.DOG.getValue(), List.of()));
        String catBreeds = String.join(",", groupedBreeds.getOrDefault(Species.CAT.getValue(), List.of()));
        String etcBreeds = String.join(",", groupedBreeds.getOrDefault(Species.ETC.getValue(), List.of()));

        return String.format("""
                        Generate a response in the following EXACT format:
                        Species,Breed,Color1,Color2,Color3,...

                        STRICT RULES:
                        1. The species must be EXACTLY one of: "강아지", "고양이", "기타"
                        2. The breed must be exactly one from the appropriate category:
                           - For "강아지": %s
                           - For "고양이": %s
                           - For "기타": %s
                        3. Colors must be one or more from ONLY this list: %s
                        4. NO extra characters, spaces around commas, newlines, or backslashes
                        5. Return ONLY the comma-separated format

                        Valid Examples:
                        강아지,골든 리트리버,노란색
                        고양이,러시안 블루,회색,검은색
                        기타,기타축종,하얀색

                        IMPORTANT: Your response must start directly with the species name, no other text.""",
                dogBreeds, catBreeds, etcBreeds, FIXED_COLORS);
    }
}
