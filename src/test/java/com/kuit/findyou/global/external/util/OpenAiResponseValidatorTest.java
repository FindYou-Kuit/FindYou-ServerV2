package com.kuit.findyou.global.external.util;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.global.external.exception.OpenAiResponseValidatingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("OpenAiResponseValidator 테스트")
class OpenAiResponseValidatorTest {

    // 공통으로 쓸 정상 그룹 (검증 대상: species → breed 리스트)
    private static final Map<String, List<String>> BREED_GROUP = Map.of(
            "강아지", List.of("치와와", "진돗개"),
            "고양이", List.of("러시안 블루", "스코티쉬 폴드"),
            "기타",   List.of("기타축종")
    );

    @Nested
    @DisplayName("실패 케이스")
    class Failures {

        @Test
        @DisplayName("dto == null → INPUT_NULL_OR_BLANK")
        void dto_null() {
            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(null, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK.getValue());
        }

        @Test
        @DisplayName("species == null → INPUT_NULL_OR_BLANK")
        void species_null() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO(null, "치와와", List.of("하얀색"));

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK.getValue());
        }

        @Test
        @DisplayName("species == 공백 → INPUT_NULL_OR_BLANK")
        void species_blank() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("   ", "치와와", List.of("하얀색"));

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK.getValue());
        }

        @Test
        @DisplayName("species 가 허용 목록 외 → SPECIES_INVALID")
        void species_invalid_value() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("새", "치와와", List.of("하얀색"));

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_SPECIES_INVALID.getValue());
        }

        @Test
        @DisplayName("breed == null → INPUT_NULL_OR_BLANK")
        void breed_null() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("강아지", null, List.of("하얀색"));

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK.getValue());
        }

        @Test
        @DisplayName("breed == 공백 → INPUT_NULL_OR_BLANK")
        void breed_blank() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("강아지", "   ", List.of("하얀색"));

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK.getValue());
        }

        @Test
        @DisplayName("breedGroup 에 해당 species 키가 없음 → BREED_GROUP_EMPTY")
        void breed_group_missing_for_species() {
            // species=고양이지만, 고양이 키가 없는 그룹
            Map<String, List<String>> brokenGroup = Map.of(
                    "강아지", List.of("치와와")
            );
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("고양이", "러시안 블루", List.of("회색"));

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, brokenGroup)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_BREED_GROUP_EMPTY.getValue());
        }

        @Test
        @DisplayName("breed 가 species 그룹 내 목록에 없음 → BREED_INVALID")
        void breed_not_in_group() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("강아지", "스코티쉬 폴드", List.of("하얀색")); // 고양이 품종을 강아지로

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_BREED_INVALID.getValue());
        }

        @Test
        @DisplayName("furColors == null → COLORS_INVALID")
        void colors_null() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("강아지", "치와와", null);

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_COLORS_INVALID.getValue());
        }

        @Test
        @DisplayName("furColors 가 전부 무효 색상 → COLORS_INVALID")
        void colors_all_invalid() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("강아지", "치와와", List.of("파란색", "초록색"));

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_COLORS_INVALID.getValue());
        }

        @Test
        @DisplayName("furColors 가 공백/널 문자열만 포함 → INPUT_NULL_OR_BLANK → COLORS_INVALID 흐름 전 throw")
        void colors_contains_blank_entries() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("강아지", "치와와",
                            Arrays.asList("   ", null));

            assertThatThrownBy(() ->
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP)
            )
                    .isInstanceOf(OpenAiResponseValidatingException.class)
                    .hasMessage(OPENAI_VALIDATOR_INPUT_NULL_OR_BLANK.getValue());
        }
    }

    @Nested
    @DisplayName("성공/정규화 케이스")
    class Success {

        @Test
        @DisplayName("species/breed 앞뒤 공백 제거, colors 중복/공백 제거 및 distinct 적용")
        void trims_and_distinct_colors() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("  강아지  ", "  치와와  ",
                            List.of("하얀색", "  하얀색 ", "갈색", "파란색")); // 파란색은 무효 → 제거

            BreedAiDetectionResponseDTO result =
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP);

            assertThat(result).isNotNull();
            assertThat(result.species()).isEqualTo("강아지");      // trim 반영
            assertThat(result.breed()).isEqualTo("치와와");        // trim 반영
            assertThat(result.furColors()).containsExactlyInAnyOrder("하얀색", "갈색"); // 중복 제거 + 무효 제거
            assertThat(result.furColors()).hasSize(2);
        }

        @Test
        @DisplayName("유효 색상 하나만 살아남아도 OK")
        void at_least_one_valid_color_is_ok() {
            BreedAiDetectionResponseDTO dto =
                    new BreedAiDetectionResponseDTO("고양이", "러시안 블루",
                            List.of("초록색", "회색", "보라색")); // 회색만 유효

            BreedAiDetectionResponseDTO result =
                    OpenAiResponseValidator.validateOpenAiResponse(dto, BREED_GROUP);

            assertThat(result).isNotNull();
            assertThat(result.species()).isEqualTo("고양이");
            assertThat(result.breed()).isEqualTo("러시안 블루");
            assertThat(result.furColors()).containsExactly("회색");
        }
    }
}
