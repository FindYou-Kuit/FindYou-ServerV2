package com.kuit.findyou.global.external.client;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.OPENAI_CLIENT_CALL_FAILED;
import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.OPENAI_CLIENT_EMPTY_RESPONSE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OpenAiClientTest {

    @Mock ChatClient.Builder chatClientBuilder;
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock ChatClient.CallResponseSpec callResponseSpec;

    OpenAiClient client;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        client = new OpenAiClient(chatClientBuilder);
    }

    private void stubPromptChainSuccess(BreedAiDetectionResponseDTO dto) {
        when(chatClient.prompt()).thenReturn(requestSpec);

        doReturn(requestSpec).when(requestSpec).messages(any(UserMessage.class));
        doReturn(requestSpec).when(requestSpec).options(any(OpenAiChatOptions.class));

        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(eq(BreedAiDetectionResponseDTO.class))).thenReturn(dto);
    }

    private void stubPromptChainNullEntity() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        doReturn(requestSpec).when(requestSpec).messages(any(UserMessage.class));
        doReturn(requestSpec).when(requestSpec).options(any(OpenAiChatOptions.class));
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(eq(BreedAiDetectionResponseDTO.class))).thenReturn(null);
    }

    private void stubPromptChainEntityThrows() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        doReturn(requestSpec).when(requestSpec).messages(any(UserMessage.class));
        doReturn(requestSpec).when(requestSpec).options(any(OpenAiChatOptions.class));
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(eq(BreedAiDetectionResponseDTO.class)))
                .thenThrow(new RuntimeException("timeout"));
    }

    @Test
    @DisplayName("성공: JSON Schema 준수 응답 → DTO 반환")
    void analyzeImage_success() {
        BreedAiDetectionResponseDTO dto =
                new BreedAiDetectionResponseDTO("강아지", "치와와", List.of("하얀색"));
        stubPromptChainSuccess(dto);

        var result = client.analyzeImage("https://ex.com/dog.jpg", "프롬프트");

        assertThat(result).isNotNull();
        assertThat(result.species()).isEqualTo("강아지");
        assertThat(result.breed()).isEqualTo("치와와");
        assertThat(result.furColors()).containsExactly("하얀색");
    }

    @Test
    @DisplayName("실패: entity == null → OpenAiClientException(EMPTY_RESPONSE)")
    void analyzeImage_nullResponse_throws() {
        stubPromptChainNullEntity();

        assertThatThrownBy(() -> client.analyzeImage("https://ex.com/dog.jpg", "p"))
                .isInstanceOf(OpenAiClientException.class)
                .hasMessage(OPENAI_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("실패: 체인 내부 예외 → OpenAiClientException(CALL_FAILED) 래핑")
    void analyzeImage_chainThrows_wrapped() {
        stubPromptChainEntityThrows();

        assertThatThrownBy(() -> client.analyzeImage("https://ex.com/dog.jpg", "prompt"))
                .isInstanceOf(OpenAiClientException.class)
                .hasMessage(OPENAI_CLIENT_CALL_FAILED.getValue());
    }
}
