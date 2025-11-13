package com.kuit.findyou.domain.breed.service.breedAiDetection;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.OpenAiClient;
import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import com.kuit.findyou.global.external.exception.OpenAiResponseValidatingException;
import com.kuit.findyou.global.external.util.OpenAiResponseValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.kuit.findyou.domain.breed.model.Species.*;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BREED_ANALYSIS_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BreedAiDetectionServiceImplTest {

    @Mock
    OpenAiClient openAiClient;

    @Mock
    BreedRepository breedRepository;

    @InjectMocks
    BreedAiDetectionServiceImpl breedAiDetectionService;

    @Test
    @DisplayName("analyzeBreedWithAi - 품종 AI 판별하기 성공")
    void analyzeBreedWithAi_Success() {
        // given: 품종 리스트 mocking (프롬프트 생성용)
        when(breedRepository.findAll()).thenReturn(List.of(
                Breed.builder().name("치와와").species(DOG.getValue()).build(),
                Breed.builder().name("스코티쉬 폴드").species(CAT.getValue()).build(),
                Breed.builder().name("기타축종").species(ETC.getValue()).build()
        ));

        // OpenAI 정상 응답 mocking
        String aiOk = "강아지,치와와,하얀색,갈색";
        when(openAiClient.analyzeImage(eq("test-url"), anyString()))
                .thenReturn(new BreedAiDetectionResponseDTO("강아지", "치와와", List.of("하얀색", "갈색")));

        // when
        BreedAiDetectionResponseDTO dto = breedAiDetectionService.analyzeBreedWithAi("test-url");

        // then: 반환값 검증
        assertThat(dto.species()).isEqualTo("강아지");
        assertThat(dto.breed()).isEqualTo("치와와");
        assertThat(dto.furColors()).containsExactlyInAnyOrder("하얀색", "갈색");

        // and: 실제 전달된 prompt 내용에 우리가 넣은 품종들이 포함됐는지 검증
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(openAiClient).analyzeImage(eq("test-url"), promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        assertThat(prompt)
                .contains("치와와")
                .contains("스코티쉬 폴드")
                .contains("기타축종");
    }

    @Test
    @DisplayName("OpenAiClientException 발생 시 CustomException(BREED_ANALYSIS_FAILED) 발생")
    void analyzeBreedWithAi_openAiClientException() {
        // given
        when(breedRepository.findAll()).thenReturn(List.of(Breed.builder().name("진돗개").species("강아지").build()));
        when(openAiClient.analyzeImage(eq("test-url"), anyString()))
                .thenThrow(new OpenAiClientException("API 호출 실패"));

        // when & then
        assertThatThrownBy(() -> breedAiDetectionService.analyzeBreedWithAi("test-url"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(BREED_ANALYSIS_FAILED.getMessage());
    }

    @Test
    @DisplayName("Validator 가 예외를 던지면 → CustomException(BREED_ANALYSIS_FAILED)로 변환")
    void analyzeBreedWithAi_validatorThrows_customException() {
        // given: 프롬프트 생성용 품종
        when(breedRepository.findAll()).thenReturn(List.of(
                Breed.builder().name("치와와").species("강아지").build(),
                Breed.builder().name("스코티쉬 폴드").species("고양이").build(),
                Breed.builder().name("기타축종").species("기타").build()
        ));

        // OpenAI 원 응답(raw)
        BreedAiDetectionResponseDTO raw =
                new BreedAiDetectionResponseDTO("강아지", "치와와", List.of("하얀색"));
        when(openAiClient.analyzeImage(eq("test-url"), anyString())).thenReturn(raw);

        // when & then: Validator 가 검증 실패를 던지면 서비스는 CustomException 으로 변환
        try (MockedStatic<OpenAiResponseValidator> mocked = mockStatic(OpenAiResponseValidator.class)) {
            mocked.when(() -> OpenAiResponseValidator.validateOpenAiResponse(eq(raw), anyMap()))
                    .thenThrow(new OpenAiResponseValidatingException(ExternalExceptionMessage.OPENAI_VALIDATOR_COLORS_INVALID));

            assertThatThrownBy(() -> breedAiDetectionService.analyzeBreedWithAi("test-url"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(BREED_ANALYSIS_FAILED.getMessage());
        }
    }

    @Test
    @DisplayName("Validator 가 정규화된 DTO를 반환하면 → 서비스는 그 결과를 그대로 반환")
    void analyzeBreedWithAi_validatorReturns_normalizedDto() {
        // given
        when(breedRepository.findAll()).thenReturn(List.of(
                Breed.builder().name("치와와").species("강아지").build(),
                Breed.builder().name("스코티쉬 폴드").species("고양이").build()
        ));

        // OpenAI 원 응답(raw) (예: 비표준 색상명)
        BreedAiDetectionResponseDTO raw =
                new BreedAiDetectionResponseDTO("강아지", "치와와", List.of("화이트", "브라운"));
        when(openAiClient.analyzeImage(eq("test-url"), anyString())).thenReturn(raw);

        // Validator 가 표준화/정규화된 DTO 를 반환한다고 가정
        BreedAiDetectionResponseDTO normalized =
                new BreedAiDetectionResponseDTO("강아지", "치와와", List.of("하얀색", "갈색"));

        // when
        try (MockedStatic<OpenAiResponseValidator> mocked = mockStatic(OpenAiResponseValidator.class)) {
            mocked.when(() -> OpenAiResponseValidator.validateOpenAiResponse(eq(raw), anyMap()))
                    .thenReturn(normalized);

            BreedAiDetectionResponseDTO result = breedAiDetectionService.analyzeBreedWithAi("test-url");

            // then
            assertThat(result).isNotNull();
            assertThat(result.species()).isEqualTo("강아지");
            assertThat(result.breed()).isEqualTo("치와와");
            assertThat(result.furColors()).containsExactlyInAnyOrder("하얀색", "갈색");
        }
    }
}