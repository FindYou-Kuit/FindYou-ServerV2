package com.kuit.findyou.domain.breed.service.breedAiDetection;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.domain.breed.model.Breed;
import com.kuit.findyou.domain.breed.repository.BreedRepository;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.client.OpenAiClient;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import com.kuit.findyou.global.external.exception.OpenAiParsingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.kuit.findyou.domain.breed.model.Species.*;
import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BREED_ANALYSIS_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}