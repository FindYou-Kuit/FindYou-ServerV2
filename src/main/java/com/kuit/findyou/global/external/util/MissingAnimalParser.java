package com.kuit.findyou.global.external.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

import static com.kuit.findyou.domain.breed.model.Species.*;

public class MissingAnimalParser {

    private static final String DEFAULT_SIGNIFICANT = "미등록";
    private static final String UNKNOWN = "미상";
    private static final LocalDate DEFAULT_DATE = LocalDate.now();

    public static String parseBreed(String breedName) {
        return breedName != null ? breedName.trim() : UNKNOWN;
    }

    public static String parseSpecies(String breedName, Set<String> dogBreeds, Set<String> catBreeds, Set<String> otherBreeds) {
        if (breedName.equals(UNKNOWN)) return UNKNOWN;

        if (dogBreeds.contains(breedName)) return DOG.getValue();

        if (catBreeds.contains(breedName)) return CAT.getValue();

        if (otherBreeds.contains(breedName)) return ETC.getValue();

        return UNKNOWN;
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return DEFAULT_DATE; // 기본값: 오늘 날짜
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
            return dateTime.toLocalDate();
        } catch (DateTimeParseException e) {
            return DEFAULT_DATE;
        }
    }

    public static String parseSignificant(String significant) {
        return significant != null ? significant : DEFAULT_SIGNIFICANT;
    }
}
