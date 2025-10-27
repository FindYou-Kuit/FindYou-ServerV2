package com.kuit.findyou.global.external.util;

import com.kuit.findyou.domain.report.model.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class MissingAnimalParserTest {

    @Nested
    @DisplayName("parseBreed 메서드 검증")
    class ParseBreed {
        @Test
        @DisplayName("null 이거나 빈 값이 응답될 경우 '미상'을 반환한다.")
        void null_or_blank_then_UNKNOWN() {
            assertThat(MissingAnimalParser.parseBreed(null)).isEqualTo("미상");
            assertThat(MissingAnimalParser.parseBreed("")).isEqualTo("미상");
            assertThat(MissingAnimalParser.parseBreed("   ")).isEqualTo("미상");
        }

        @Test
        @DisplayName("품종의 좌우에 공백이 포함될 경우, 공백을 제거한다.")
        void trims_value() {
            assertThat(MissingAnimalParser.parseBreed("  진돗개  ")).isEqualTo("진돗개");
        }
    }

    @Nested
    @DisplayName("parseSpecies 메서드 검증")
    class ParseSpecies {
        Set<String> dogs = Set.of("진돗개", "치와와");
        Set<String> cats = Set.of("코리안 숏헤어");
        Set<String> etcs = Set.of("기타축종");

        @Test
        @DisplayName("품종 정보가 '미상' 일 경우 축종도 '미상'을 반환한다.")
        void UNKNOWN_breed_returns_UNKNOWN() {
            assertThat(MissingAnimalParser.parseSpecies("미상", dogs, cats, etcs)).isEqualTo("미상");
        }

        @Test
        @DisplayName("DB에 존재하는 품종이 응답될 경우, 해당 품종의 축종을 반환한다.")
        void dog_cat_etc_matched() {
            assertThat(MissingAnimalParser.parseSpecies("진돗개", dogs, cats, etcs)).isEqualTo("강아지");
            assertThat(MissingAnimalParser.parseSpecies("코리안 숏헤어", dogs, cats, etcs)).isEqualTo("고양이");
            assertThat(MissingAnimalParser.parseSpecies("기타축종", dogs, cats, etcs)).isEqualTo("기타");
        }

        @Test
        @DisplayName("품종이 '미상'이진 않지만, DB에 존재하지 않는 품종일 경우, '미상'을 반환한다.")
        void not_found_returns_UNKNOWN() {
            assertThat(MissingAnimalParser.parseSpecies("라쿤", dogs, cats, etcs)).isEqualTo("미상");
        }
    }

    @Nested
    @DisplayName("parseDate 메서드 검증 => (yyyy-MM-dd HH:mm:ss.S -> LocalDate)")
    class ParseDate {
        @Test
        @DisplayName("LocalDateTime 형식의 응답을 LocalDate 형식으로 변환한다.")
        void valid_formats_to_localdate() {
            assertThat(MissingAnimalParser.parseDate("2024-07-18 13:45:12.3"))
                    .isEqualTo(LocalDate.of(2024, 7, 18));
        }

        @Test
        @DisplayName("date 컬럼을 nullable 하지 않도록 유지하기 위해, 잘못된 값이 응답될 경우 2000년 1월 1일로 값이 설정되도록 한다.")
        void null_blank_or_invalid_returns_UNKNOWN_DATE_2000_01_01() {
            LocalDate unknown = LocalDate.of(2000, 1, 1);
            assertThat(MissingAnimalParser.parseDate(null)).isEqualTo(unknown);
            assertThat(MissingAnimalParser.parseDate("")).isEqualTo(unknown);
            assertThat(MissingAnimalParser.parseDate("bad")).isEqualTo(unknown);
        }
    }

    @Nested
    @DisplayName("parseSignificant 메서드 검증")
    class ParseSignificant {
        @Test
        @DisplayName("특이사항이 null 이거나 빈 문자열이 등록되었다면 '미등록'을 반환한다.")
        void null_or_blank_then_default() {
            assertThat(MissingAnimalParser.parseSignificant(null)).isEqualTo("미등록");
            assertThat(MissingAnimalParser.parseSignificant("")).isEqualTo("미등록");
        }

        @Test
        @DisplayName("특이사항의 좌우에 공백이 포함될 경우, 공백을 제거한다.")
        void trims_value() {
            assertThat(MissingAnimalParser.parseSignificant("  점무늬  ")).isEqualTo("점무늬");
        }
    }

    @Nested
    @DisplayName("parseSex 메서드 검증")
    class ParseSex {
        @Test
        @DisplayName("성별을 파싱한다.")
        void map_to_enum() {
            assertThat(MissingAnimalParser.parseSex(null)).isEqualTo(Sex.Q);
            assertThat(MissingAnimalParser.parseSex("M")).isEqualTo(Sex.M);
            assertThat(MissingAnimalParser.parseSex("F")).isEqualTo(Sex.F);
            assertThat(MissingAnimalParser.parseSex("x")).isEqualTo(Sex.Q);
        }
    }

    @Nested
    @DisplayName("trimOrNull 메서드 검증")
    class TrimOrNull {

        @Test
        @DisplayName("입력이 null이면 null을 반환한다")
        void returns_null_when_input_is_null() {
            // given
            String input = null;

            // when
            String result = MissingAnimalParser.trimOrNull(input);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("공백만 있는 문자열이면 빈 문자열을 반환한다")
        void returns_empty_string_when_only_whitespaces() {
            // given
            String input1 = "     ";
            String input2 = "\t\t";
            String input3 = "\n\n";

            // when & then
            assertThat(MissingAnimalParser.trimOrNull(input1)).isEqualTo("");
            assertThat(MissingAnimalParser.trimOrNull(input2)).isEqualTo("");
            assertThat(MissingAnimalParser.trimOrNull(input3)).isEqualTo("");
        }

        @Test
        @DisplayName("앞뒤 공백을 제거한다")
        void trims_leading_and_trailing_whitespaces() {
            // given
            String input1 = "  진돗개  ";
            String input2 = "\t고양이\n";

            // when & then
            assertThat(MissingAnimalParser.trimOrNull(input1)).isEqualTo("진돗개");
            assertThat(MissingAnimalParser.trimOrNull(input2)).isEqualTo("고양이");
        }

        @Test
        @DisplayName("공백이 없는 문자열은 그대로 반환한다")
        void returns_same_content_when_no_whitespaces() {
            // given
            String input = "코리안숏헤어";

            // when
            String result = MissingAnimalParser.trimOrNull(input);

            // then
            assertThat(result).isEqualTo("코리안숏헤어");
        }
    }
}
