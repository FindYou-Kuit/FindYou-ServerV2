package com.kuit.findyou.global.external.client;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ActiveProfiles;

import java.util.Base64;
import java.util.List;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.OPENAI_CLIENT_CALL_FAILED;
import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.OPENAI_CLIENT_EMPTY_RESPONSE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OpenAiClientTest {

    @Mock
    ChatClient.Builder chatClientBuilder;
    @Mock
    ChatClient chatClient;
    @Mock
    ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    ChatClient.CallResponseSpec callResponseSpec;

    OpenAiClient client;

    // 테스트에 사용할 샘플 Base64 문자열 (실제 이미지 데이터일 필요 없음)
    private final String VALID_BASE64_IMAGE = "dGVzdA=="; // "test"를 인코딩한 값

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        client = new OpenAiClient(chatClientBuilder);
    }

    private void stubPromptChainSuccess(BreedAiDetectionResponseDTO dto) {
        when(chatClient.prompt()).thenReturn(requestSpec);
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
        // given
        BreedAiDetectionResponseDTO dto =
                new BreedAiDetectionResponseDTO("강아지", "치와와", List.of("하얀색"));
        stubPromptChainSuccess(dto);

        ArgumentCaptor<UserMessage> messageCaptor = ArgumentCaptor.forClass(UserMessage.class);
        when(requestSpec.messages(messageCaptor.capture())).thenReturn(requestSpec);

        // when
        var result = client.analyzeImage(VALID_BASE64_IMAGE, "프롬프트");

        // then
        assertThat(result).isNotNull();
        assertThat(result.species()).isEqualTo("강아지");
        assertThat(result.breed()).isEqualTo("치와와");
        assertThat(result.furColors()).containsExactly("하얀색");

        // then: UserMessage 에 디코딩된 데이터가 잘 들어갔는지 검증
        UserMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getMedia()).hasSize(1);
        Media capturedMedia = capturedMessage.getMedia().get(0);

        // getData()의 결과를 byte[]로 직접 받기
        byte[] actualBytes = (byte[]) capturedMedia.getData();
        byte[] expectedBytes = Base64.getDecoder().decode(VALID_BASE64_IMAGE);

        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    @DisplayName("실패: entity == null → OpenAiClientException(EMPTY_RESPONSE)")
    void analyzeImage_nullResponse_throws() {
        // given
        stubPromptChainNullEntity();

        // when & then
        assertThatThrownBy(() -> client.analyzeImage(VALID_BASE64_IMAGE, "p"))
                .isInstanceOf(OpenAiClientException.class)
                .hasMessage(OPENAI_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("실패: 체인 내부 예외 → OpenAiClientException(CALL_FAILED) 래핑")
    void analyzeImage_chainThrows_wrapped() {
        // given
        stubPromptChainEntityThrows();

        // when & then
        assertThatThrownBy(() -> client.analyzeImage(VALID_BASE64_IMAGE, "prompt"))
                .isInstanceOf(OpenAiClientException.class)
                .hasMessage(OPENAI_CLIENT_CALL_FAILED.getValue());
    }

    @Test
    @DisplayName("실패: 잘못된 Base64 문자열 → OpenAiClientException(CALL_FAILED) 래핑")
    void analyzeImage_invalidBase64_throws() {
        // given
        String invalidBase64 = "this-is-not-base64";

        // when & then
        assertThatThrownBy(() -> client.analyzeImage(invalidBase64, "prompt"))
                .isInstanceOf(OpenAiClientException.class)
                .hasMessage(OPENAI_CLIENT_CALL_FAILED.getValue());
    }

}