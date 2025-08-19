package com.kuit.findyou.global.external.util;

import com.kuit.findyou.domain.report.model.Neutering;
import com.kuit.findyou.domain.report.model.Sex;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import static com.kuit.findyou.domain.breed.model.Species.*;

public class ProtectingAnimalParser {

    private static final String UNKNOWN = "미상";
    private static final LocalDate UNKNOWN_DATE = LocalDate.of(0, 1, 1);

    /**
     * "yyyyMMdd" 형식의 날짜 문자열을 LocalDate 로 변환.
     * 예: "20240718" → LocalDate.of(2024, 7, 18)
     *
     * @param date 날짜 문자열 (예: 20240718)
     * @return 파싱된 LocalDate, 실패 시 LocalDate.of(0, 1, 1) 반환
     */
    public static LocalDate changeToLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        if (date == null || date.isBlank()) return UNKNOWN_DATE;

        try {
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            return UNKNOWN_DATE;
        }
    }

    /**
     * 출생 연도를 기반으로 만 나이를 계산.
     * 예: "2020(년생)" → "5"
     *
     * @param age 공공데이터 age 필드 (예: "2020(년생)")
     * @return 계산된 나이 (문자열), 실패 시 "미상"
     */
    public static String parseAge(String age) {
        try {
            if (age == null || age.isBlank()) return UNKNOWN;

            String yearStr = age.split("\\(")[0].trim();
            int birthYear = Integer.parseInt(yearStr);

            // 출생일 1월 1일로 가정 (정확한 날짜 정보가 없기 때문에)
            LocalDate birthDate = LocalDate.of(birthYear, 1, 1);
            LocalDate today = LocalDate.now();

            long years = ChronoUnit.YEARS.between(birthDate, today);
            return String.valueOf(years);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    /**
     * 몸무게(weight) 문자열에서 괄호 앞의 수치만 추출하고 쉼표(,)는 점(.)으로 변환.
     * 예: "15(Kg)" → "15", "3,5(Kg)" → "3.5"
     *
     * @param weight 공공데이터 weight 필드
     * @return 파싱된 몸무게 문자열, 실패 시 "미상"
     */
    public static String parseWeight(String weight) {
        try {
            if (weight == null || weight.isBlank()) return UNKNOWN;

            String value = weight.split("\\(")[0];
            return value.replace(',', '.').trim();
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    /**
     * HTML 인코딩된 색상 문자열을 디코딩하고 '&'를 ','로 치환
     * 예: "갈색&검정" → "갈색,검정"
     *
     * @param colorCd 공공데이터 colorCd 필드
     * @return 파싱된 색상 문자열, 실패 시 "미상"
     */
    public static String parseColor(String colorCd) {
        try {
            if (colorCd == null || colorCd.isBlank()) return UNKNOWN;

            return colorCd.replace("&", ",").trim();
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    /**
     * 공공데이터의 upKindNm 값 '개' 를 '강아지' 로 변환.
     * 예: "개" → "강아지"
     *
     * @param species 공공데이터의 축종명 (예: "개", "고양이", "기타 등등")
     * @return 개 -> 강아지 / 그 외는 그대로
     */
    public static String parseSpecies(String species) {
        if (species == null || species.isBlank()) return UNKNOWN;

        return species.trim().equals("개") ? DOG.getValue() : species;
    }

    /**
     * 공공데이터의 sexCd 값을 enum 으로 변환
     *
     * @param sex 공공데이터의 성별
     * @return M -> Sex.M / F -> Sex.F / 그 외는 Sex.Q (미상)
     */
    public static Sex parseSex(String sex) {
        if(sex == null) return Sex.Q;

        return switch (sex.trim().toUpperCase()) {
            case "M" -> Sex.M;
            case "F" -> Sex.F;
            default -> Sex.Q;
        };
    }

    /**
     * 공공데이터의 neuterYn 값을 enum 으로 변환
     *
     * @param neutering 공공데이터의 중성화 여부
     * @return Y -> Neutering.Y / N -> Neutering.N / 그 외는 Neutering.U (미상)
     */
    public static Neutering parseNeutering(String neutering) {
        if(neutering == null) return Neutering.U;

        return switch (neutering.trim().toUpperCase()) {
            case "Y" -> Neutering.Y;
            case "N" -> Neutering.N;
            default -> Neutering.U;
        };
    }
}
