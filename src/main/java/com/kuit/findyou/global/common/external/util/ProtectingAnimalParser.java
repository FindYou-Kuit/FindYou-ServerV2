package com.kuit.findyou.global.common.external.util;

import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * 나이(age) 문자열에서 괄호 앞의 연도만 추출.
     * 예: "2020(년생)" → "2020"
     *
     * @param age 공공데이터 age 필드
     * @return 추출된 연도 문자열, 실패 시 "알수없음"
     */
    public static String parseAge(String age) {
        if (age == null || age.isBlank()) return UNKNOWN;
        return age.split("\\(")[0].trim();
    }

    /**
     * 몸무게(weight) 문자열에서 괄호 앞의 수치만 추출하고 쉼표(,)는 점(.)으로 변환.
     * 예: "15(Kg)" → "15", "3,5(Kg)" → "3.5"
     *
     * @param weight 공공데이터 weight 필드
     * @return 파싱된 몸무게 문자열, 실패 시 "알수없음"
     */
    public static String parseWeight(String weight) {
        if (weight == null || weight.isBlank()) return UNKNOWN;

        String value = weight.split("\\(")[0];
        return value.replace(',', '.').trim();
    }

    /**
     * HTML 인코딩된 색상 문자열을 디코딩하고 '&'를 ','로 치환
     * 예: "갈색&검정" → "갈색,검정"
     */
    public static String parseColor(String colorCd) {
        if (colorCd == null || colorCd.isBlank()) return UNKNOWN;

        return colorCd.replace("&", ",").trim();          // "갈색,검정"
    }

}
