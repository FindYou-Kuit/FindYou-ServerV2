package com.kuit.findyou.global.external.util;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;
import com.kuit.findyou.global.external.exception.OpenAiParsingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
class OpenAiParserTest {

    private final Map<String, List<String>> breedGroup = Map.of(
            "강아지", List.of("진돗개", "치와와", "포메라니안"),
            "고양이", List.of("코리안 숏헤어", "러시안 블루"),
            "기타", List.of("기타축종")
    );

    @Nested
    @DisplayName("parseSpecies 메서드 검증")
    class ParseSpecies {

        @Test
        @DisplayName("유효한 축종이면 해당 축종을 반환한다.")
        void returns_valid_species() {
            String input = "강아지,치와와,하얀색";
            assertThat(OpenAiParser.parseSpecies(input)).isEqualTo("강아지");
        }

        @Test
        @DisplayName("유효하지 않은 축종이면 예외를 던진다.")
        void invalid_species_throws() {
            String input = "토끼,네덜란드 드워프,하얀색";
            assertThatThrownBy(() -> OpenAiParser.parseSpecies(input))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_SPECIES_INVALID.getValue());
        }

        @Test
        @DisplayName("쉼표(,) 구분자가 없으면 예외를 던진다.")
        void missing_commas_throws() {
            String input = "강아지 치와와 하얀색";
            assertThatThrownBy(() -> OpenAiParser.parseSpecies(input))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_NO_COMMA_DELIMITER.getValue());
        }

        @Test
        @DisplayName("선행 특수문자/개행/백슬래시/따옴표 등이 포함되어도 정제 후 파싱된다.")
        void cleans_and_parses() {
            String messy = "\\n***   강아지,  \"치와와\"  , 하얀색\\n, 갈색\n";
            assertThat(OpenAiParser.parseSpecies(messy)).isEqualTo("강아지");
        }

        @Test
        @DisplayName("null 또는 빈 문자열이면 예외를 던진다.")
        void null_or_blank_throws() {
            assertThatThrownBy(() -> OpenAiParser.parseSpecies(null))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_INPUT_NULL_OR_BLANK.getValue());
            assertThatThrownBy(() -> OpenAiParser.parseSpecies("  "))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_INPUT_NULL_OR_BLANK.getValue());;
        }

        @Test
        @DisplayName("정제 후 비어 있으면 예외를 던진다(선행 특수문자/백슬래시/개행만 있을 때).")
        void parseSpecies_cleanedToBlank_throws() {
            String noisy = "***\\n\\\\\\n   "; // 특수문자/백슬래시/개행만
            assertThatThrownBy(() -> OpenAiParser.parseSpecies(noisy))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_INPUT_NULL_OR_BLANK.getValue());
        }

    }

    @Nested
    @DisplayName("parseBreed 메서드 검증")
    class ParseBreed {

        @Test
        @DisplayName("해당 축종의 유효한 품종이면 그대로 반환한다.")
        void returns_valid_breed() {
            String input = "강아지,치와와,하얀색";
            String species = OpenAiParser.parseSpecies(input);
            assertThat(OpenAiParser.parseBreed(input, species, breedGroup)).isEqualTo("치와와");
        }

        @Test
        @DisplayName("품종 이름에 따옴표/공백이 있어도 정제 후 검증된다.")
        void quotes_and_spaces_are_cleaned() {
            String input = "강아지,  \"치와와\"  , 하얀색, 갈색";
            String species = OpenAiParser.parseSpecies(input);
            assertThat(OpenAiParser.parseBreed(input, species, breedGroup)).isEqualTo("치와와");
        }

        @Test
        @DisplayName("해당 축종의 품종 목록이 비어있으면 예외를 던진다.")
        void empty_breed_list_for_species_throws() {
            Map<String, List<String>> emptyCats = Map.of(
                    "강아지", List.of("진돗개"),
                    "고양이", List.of(), // empty
                    "기타", List.of("기타축종")
            );
            String input = "고양이,러시안 블루,회색";
            String species = OpenAiParser.parseSpecies(input);

            assertThatThrownBy(() -> OpenAiParser.parseBreed(input, species, emptyCats))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_BREED_GROUP_EMPTY.getValue());
        }

        @Test
        @DisplayName("축종의 유효 목록에 없는 품종이면 예외를 던진다.")
        void invalid_breed_for_species_throws() {
            String input = "강아지,스핑크스,하얀색";
            String species = OpenAiParser.parseSpecies(input);

            assertThatThrownBy(() -> OpenAiParser.parseBreed(input, species, breedGroup))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_BREED_INVALID.getValue());
        }

        @Test
        @DisplayName("입력 항목이 부족하면(색상 없음) 예외를 던진다.")
        void insufficient_parts_throws() {
            String input = "강아지,치와와"; // 색상 누락
            String species = "강아지";
            assertThatThrownBy(() -> OpenAiParser.parseBreed(input, species, breedGroup))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_PARTS_TOO_FEW.getValue());
        }

        @Test
        @DisplayName("품종 그룹에 축종 키가 없으면 예외(=null) 발생")
        void parseBreed_missingSpeciesKey_throws() {
            Map<String, List<String>> group = Map.of( // '강아지' 키 없음
                    "고양이", List.of("러시안 블루"),
                    "기타", List.of("기타축종")
            );
            String input = "강아지,치와와,하얀색";
            String species = OpenAiParser.parseSpecies(input);

            assertThatThrownBy(() -> OpenAiParser.parseBreed(input, species, group))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_BREED_GROUP_EMPTY.getValue());
        }

    }

    @Nested
    @DisplayName("parseColors 메서드 검증")
    class ParseColors {

        @Test
        @DisplayName("허용된 색상만 필터링하고 중복을 제거한다.")
        void filters_and_deduplicates() {
            String input = "강아지,치와와,하얀색,갈색,하얀색";
            List<String> colors = OpenAiParser.parseColors(input);
            assertThat(colors).containsExactly("하얀색", "갈색"); // encounter 순서 보장(distinct)
        }

        @Test
        @DisplayName("허용되지 않은 색상만 있으면 예외를 던진다.")
        void invalid_colors_only_throws() {
            String input = "강아지,치와와,보라색,파란색";
            assertThatThrownBy(() -> OpenAiParser.parseColors(input))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_COLORS_EMPTY.getValue());
        }

        @Test
        @DisplayName("따옴표/개행/백슬래시가 있어도 정제 후 파싱된다.")
        void cleans_noise_then_parses() {
            String messy = "\\n강아지,치와와,  \"하얀색\" , 갈색\\n";
            List<String> colors = OpenAiParser.parseColors(messy);
            assertThat(colors).containsExactly("하얀색", "갈색");
        }

        @Test
        @DisplayName("입력 항목이 3개 미만이면 예외를 던진다.")
        void less_than_three_parts_throws() {
            String input = "강아지,치와와"; // 색상 없음
            assertThatThrownBy(() -> OpenAiParser.parseColors(input))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_PARTS_TOO_FEW.getValue());
        }

        @Test
        @DisplayName("null 또는 빈 문자열이면 예외를 던진다.")
        void null_or_blank_throws() {
            assertThatThrownBy(() -> OpenAiParser.parseColors(null))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_INPUT_NULL_OR_BLANK.getValue());;
            assertThatThrownBy(() -> OpenAiParser.parseColors("  "))
                    .isInstanceOf(OpenAiParsingException.class)
                    .hasMessage(OPENAI_PARSER_INPUT_NULL_OR_BLANK.getValue());;
        }

        @Test
        @DisplayName("색상: 유효/무효/따옴표/개행 섞여도 정제 후 유효만, 순서 유지, 중복 제거")
        void parseColors_mixed() {
            String input = "강아지,치와와,\"검은색\"\\n,보라색,  회색 , 파란색 , 검은색";
            List<String> colors = OpenAiParser.parseColors(input);
            assertThat(colors).containsExactly("검은색", "회색");
        }

    }

    @Nested
    @DisplayName("정제 로직(선행 특수문자/개행/백슬래시/따옴표) 통합 검증")
    class CleanupIntegration {

        @Test
        @DisplayName("복잡한 노이즈가 있어도 species/breed/colors 전부 정상 파싱된다.")
        void messy_input_all_ok() {
            String messy = "***\\n  강아지 ,  \"포메라니안\" ,  검은색 ,  하얀색  \\n";
            String species = OpenAiParser.parseSpecies(messy);
            String breed = OpenAiParser.parseBreed(messy, species, breedGroup);
            List<String> colors = OpenAiParser.parseColors(messy);

            assertThat(species).isEqualTo("강아지");
            assertThat(breed).isEqualTo("포메라니안");
            assertThat(colors).containsExactly("검은색", "하얀색");
        }
    }
}
