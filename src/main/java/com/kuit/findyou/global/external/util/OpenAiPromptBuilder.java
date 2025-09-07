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
                You are a veterinary vision assistant. Analyze the provided image and extract information for exactly one primary animal.
                The server enforces a JSON schema (species, breed, furColors). Output JSON only—no extra text.

                [System rules]
                - The final output must match the server-enforced JSON schema 100%% and must not contain any text outside the JSON.
                - Never invent values outside the allowed lists (species/breed/colors).

                [Field constraints]
                - species: exactly one of "강아지" | "고양이" | "기타".
                - breed: choose exactly one from the allowed list for the detected species (no synonyms/typos):
                  · Allowed for "강아지": %s
                  · Allowed for "고양이": %s
                  · Allowed for "기타": %s
                  If uncertain, pick the closest item **within the list** only.
                - furColors: choose 1–3 from: %s. List **without duplicates** in order of **visual dominance**.

                [Decision guide]
                - If multiple animals are visible, use the **largest or most central** one.
                - species cues: (dogs) muzzle/ear shapes, body proportions; (cats) whisker pads, vertical pupils, facial contour.
                - If occluded or lighting is unusual, choose the **largest visible** colors up to 3.

                [Output]
                - Return **only** the JSON that conforms to the schema (no prose, no backticks).
                """,
                dogBreeds, catBreeds, etcBreeds, FIXED_COLORS);
    }
}
