package com.kuit.findyou.global.external.util;

import com.kuit.findyou.domain.report.model.Sex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

import static com.kuit.findyou.domain.breed.model.Species.*;

public class MissingAnimalParser {

    private static final String DEFAULT_SIGNIFICANT = "미등록";
    private static final String UNKNOWN = "미상";
    private static final LocalDate UNKNOWN_DATE = LocalDate.of(2000, 1, 1);

    public static String parseBreed(String breedName) {
        return (breedName == null || breedName.isBlank()) ? UNKNOWN : breedName.trim();
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
            return UNKNOWN_DATE;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
            return dateTime.toLocalDate();
        } catch (DateTimeParseException e) {
            return UNKNOWN_DATE;
        }
    }

    public static String parseSignificant(String significant) {
        return (significant == null || significant.isBlank()) ? DEFAULT_SIGNIFICANT : significant.trim();
    }

    public static Sex parseSex(String sex) {
        if(sex == null) return Sex.Q;

        return switch (sex.trim().toUpperCase()) {
            case "M" -> Sex.M;
            case "F" -> Sex.F;
            default -> Sex.Q;
        };
    }

    public static String trimOrNull(String s) {
        return (s == null) ? null : s.trim();
    }
}
