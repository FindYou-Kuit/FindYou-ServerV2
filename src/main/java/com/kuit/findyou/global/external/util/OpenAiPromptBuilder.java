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
                        Generate a response in the following format:
                         \\
                        Species,Breed,Color1,Color2,Color3,...

                         \\
                        - The species must be one of the following: "강아지", "고양이", "기타".
                         \\
                        - The breed must be exactly one, and it must match the species category:

                         \\
                        If the species is "강아지":
                         %s

                         \\
                        If the species is "고양이":
                         %s

                         \\
                        If the species is "기타":
                         %s

                         \\
                        - Colors must be one or more, separated by commas (",").
                         \\
                        The color must be chosen from the following fixed list:
                         %s

                         \\
                        - There should be no spaces between commas in the color list.

                         \\
                        **Example input & expected response:**
                         강아지,골든 리트리버,노란색
                         고양이,러시안 블루,회색,검은색
                         기타,기타축종,하얀색""",
                dogBreeds, catBreeds, etcBreeds, FIXED_COLORS);
    }
}
