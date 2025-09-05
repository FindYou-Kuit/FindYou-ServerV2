package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;
import com.kuit.findyou.global.external.dto.OpenAiResponse;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OpenAiClientTest {

    @Mock RestClient restClient;
    @Mock RestClient.RequestBodyUriSpec postSpec;
    @Mock RestClient.ResponseSpec responseSpec;

    OpenAiClient client;

    @BeforeEach
    void setUp() {
        client = new OpenAiClient(restClient);
    }

    private void stubChain() {
        when(restClient.post()).thenReturn(postSpec);
        when(postSpec.body(anyMap())).thenReturn(postSpec);
        when(postSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("성공: choices[0].message.content 반환")
    void analyzeImage_success() {
        stubChain();

        OpenAiResponse.Message msg = new OpenAiResponse.Message("id-1", "강아지,치와와,하얀색");
        OpenAiResponse.Choice choice = new OpenAiResponse.Choice(msg);
        OpenAiResponse response = new OpenAiResponse(List.of(choice));

        when(responseSpec.body(eq(OpenAiResponse.class))).thenReturn(response);

        String result = client.analyzeImage("https://img", "프롬프트");
        assertThat(result).isEqualTo("강아지,치와와,하얀색");
    }

    @Test
    @DisplayName("실패: response == null → OpenAiClientException")
    void analyzeImage_nullResponse_throws() {
        stubChain();
        when(responseSpec.body(eq(OpenAiResponse.class))).thenReturn(null);

        assertThatThrownBy(() -> client.analyzeImage("https://img", "p"))
                .isInstanceOf(OpenAiClientException.class)
                .hasMessage(OPENAI_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("실패: choices 비어있음 → OpenAiClientException")
    void analyzeImage_emptyChoices_throws() {
        stubChain();
        when(responseSpec.body(eq(OpenAiResponse.class))).thenReturn(new OpenAiResponse(List.of()));

        assertThatThrownBy(() -> client.analyzeImage("https://img", "p"))
                .isInstanceOf(OpenAiClientException.class)
                .hasMessage(OPENAI_CLIENT_EMPTY_RESPONSE.getValue());
    }

    @Test
    @DisplayName("실패: RestClient 체인에서 예외 → OpenAiClientException 래핑")
    void analyzeImage_restClientThrows_wrapped() {
        stubChain();
        when(responseSpec.body(eq(OpenAiResponse.class))).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> client.analyzeImage("https://img", "prompt"))
                .isInstanceOf(OpenAiClientException.class)
                .hasMessage(OPENAI_CLIENT_CALL_FAILED.getValue());
    }
}
