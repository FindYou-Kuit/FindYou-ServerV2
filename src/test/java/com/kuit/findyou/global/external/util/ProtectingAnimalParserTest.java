package com.kuit.findyou.global.external.util;

import com.kuit.findyou.domain.report.model.Neutering;
import com.kuit.findyou.domain.report.model.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class ProtectingAnimalParserTest {

    @Nested
    @DisplayName("parseDate 메서드 검증 => (yyyyMMdd -> LocalDate)")
    class ParseDate {
        @Test
        @DisplayName("yyyyMMdd 형식의 응답을 LocalDate 형식으로 변환한다.")
        void valid_formats_to_localdate() {
            assertThat(ProtectingAnimalParser.parseDate("20240718"))
                    .isEqualTo(LocalDate.of(2024, 7, 18));
        }

        @Test
        @DisplayName("null, 빈 값, 잘못된 값이 응답되면 2000년 1월 1일로 값이 설정된다.")
        void null_blank_or_invalid_returns_UNKNOWN_DATE_2000_01_01() {
            LocalDate unknown = LocalDate.of(2000, 1, 1);
            assertThat(ProtectingAnimalParser.parseDate(null)).isEqualTo(unknown);
            assertThat(ProtectingAnimalParser.parseDate("")).isEqualTo(unknown);
            assertThat(ProtectingAnimalParser.parseDate("bad")).isEqualTo(unknown);
        }
    }

    @Nested
    @DisplayName("parseAge 메서드 검증 => (출생연도로부터 나이 계산)")
    class ParseAge {
        @Test
        @DisplayName("2020(년생) 이라면 오늘 날짜 기준으로 만 나이를 계산한다.")
        void valid_years_between_birth_year_and_today() {
            int birthYear = 2020;
            long expected = ChronoUnit.YEARS.between(
                    LocalDate.of(birthYear, 1, 1),
                    LocalDate.now()
            );
            assertThat(ProtectingAnimalParser.parseAge("2020(년생)"))
                    .isEqualTo(String.valueOf(expected));
        }

        @Test
        @DisplayName("null, 빈 값, 잘못된 값이면 '미상'을 반환한다.")
        void null_blank_or_invalid_returns_UNKNOWN() {
            assertThat(ProtectingAnimalParser.parseAge(null)).isEqualTo("미상");
            assertThat(ProtectingAnimalParser.parseAge("")).isEqualTo("미상");
            assertThat(ProtectingAnimalParser.parseAge("??")).isEqualTo("미상");
        }

        @Test
        @DisplayName("올해 태어난 동물이면 0 반환")
        void parseAge_currentYear_zero() {
            int y = LocalDate.now().getYear();
            assertThat(ProtectingAnimalParser.parseAge(y + "(년생)"))
                    .isEqualTo("0");
        }

    }

    @Nested
    @DisplayName("parseWeight 메서드 검증")
    class ParseWeight {
        @Test
        @DisplayName("\"15(Kg)\" 는 15 로, \"3,5(Kg)\" 는 3.5 로 변환된다.")
        void extract_before_parenthesis_and_replace_comma_with_dot() {
            assertThat(ProtectingAnimalParser.parseWeight("15(Kg)")).isEqualTo("15");
            assertThat(ProtectingAnimalParser.parseWeight("3,5(Kg)")).isEqualTo("3.5");
        }

        @Test
        @DisplayName("null, 빈 값이면 '미상'을 반환한다.")
        void null_or_blank_returns_UNKNOWN() {
            assertThat(ProtectingAnimalParser.parseWeight(null)).isEqualTo("미상");
            assertThat(ProtectingAnimalParser.parseWeight("")).isEqualTo("미상");
        }

        @Test
        @DisplayName("공백 제거 및 , 동시 수정 검증")
        void parseWeight_trim_and_comma() {
            assertThat(ProtectingAnimalParser.parseWeight("  3,50 (Kg)"))
                    .isEqualTo("3.50");
        }

    }

    @Nested
    @DisplayName("parseColor 메서드 검증")
    class ParseColor {
        @Test
        @DisplayName("갈색&검정 → 갈색,검정 으로 변환된다.")
        void replace_ampersand_with_comma() {
            assertThat(ProtectingAnimalParser.parseColor("갈색&검정")).isEqualTo("갈색,검정");
        }

        @Test
        @DisplayName("null, 빈 값이면 '미상'을 반환한다.")
        void null_or_blank_returns_UNKNOWN() {
            assertThat(ProtectingAnimalParser.parseColor(null)).isEqualTo("미상");
            assertThat(ProtectingAnimalParser.parseColor("")).isEqualTo("미상");
        }
    }

    @Nested
    @DisplayName("parseSpecies 메서드 검증")
    class ParseSpecies {
        @Test
        @DisplayName("'개' 는 '강아지'로 변환된다.")
        void dog_mapped_to_강아지() {
            assertThat(ProtectingAnimalParser.parseSpecies("개")).isEqualTo("강아지");
        }

        @Test
        @DisplayName("'고양이' 는 그대로 반환된다.")
        void cat_returns_as_is() {
            assertThat(ProtectingAnimalParser.parseSpecies("고양이")).isEqualTo("고양이");
        }

        @Test
        @DisplayName("null, 빈 값이면 '미상'을 반환한다.")
        void null_or_blank_returns_UNKNOWN() {
            assertThat(ProtectingAnimalParser.parseSpecies(null)).isEqualTo("미상");
            assertThat(ProtectingAnimalParser.parseSpecies("")).isEqualTo("미상");
        }

        @Test
        @DisplayName("parseSpecies: '  개  ' → '강아지'")
        void parseSpecies_trims() {
            assertThat(ProtectingAnimalParser.parseSpecies("  개  ")).isEqualTo("강아지");
        }

    }

    @Nested
    @DisplayName("parseSex 메서드 검증")
    class ParseSex {
        @Test
        @DisplayName("M → Sex.M, F → Sex.F, 그 외는 Sex.Q 로 변환된다.")
        void map_to_enum() {
            assertThat(ProtectingAnimalParser.parseSex(null)).isEqualTo(Sex.Q);
            assertThat(ProtectingAnimalParser.parseSex("M")).isEqualTo(Sex.M);
            assertThat(ProtectingAnimalParser.parseSex("m")).isEqualTo(Sex.M);
            assertThat(ProtectingAnimalParser.parseSex("F")).isEqualTo(Sex.F);
            assertThat(ProtectingAnimalParser.parseSex("x")).isEqualTo(Sex.Q);
        }
    }

    @Nested
    @DisplayName("parseNeutering 메서드 검증")
    class ParseNeutering {
        @Test
        @DisplayName("Y → Neutering.Y, N → Neutering.N, 그 외는 Neutering.U 로 변환된다.")
        void map_to_enum() {
            assertThat(ProtectingAnimalParser.parseNeutering(null)).isEqualTo(Neutering.U);
            assertThat(ProtectingAnimalParser.parseNeutering("Y")).isEqualTo(Neutering.Y);
            assertThat(ProtectingAnimalParser.parseNeutering("y")).isEqualTo(Neutering.Y);
            assertThat(ProtectingAnimalParser.parseNeutering("N")).isEqualTo(Neutering.N);
            assertThat(ProtectingAnimalParser.parseNeutering("n")).isEqualTo(Neutering.N);
            assertThat(ProtectingAnimalParser.parseNeutering("?")).isEqualTo(Neutering.U);
        }
    }
}
