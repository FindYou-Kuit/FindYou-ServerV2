package com.kuit.findyou.global.external.client;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Nested
    @DisplayName("guessImageMediaType")
    class GuessImageMediaTypeTests {

        @Test
        @DisplayName(".jpg → image/jpeg")
        void jpg() {
            String mime = captureMimeForUrl("https://ex.com/dog.jpg");
            assertThat(mime).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName(".jpeg → image/jpeg")
        void jpeg() {
            String mime = captureMimeForUrl("https://ex.com/dog.jpeg");
            assertThat(mime).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName(".JPEG(대문자+쿼리스트링) → image/jpeg")
        void jpegUpperWithQuery() {
            String mime = captureMimeForUrl("https://ex.com/DOG.JPEG?x=1&y=2");
            assertThat(mime).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName(".gif -> image/gif")
        void gif() {
            String mime = captureMimeForUrl("https://ex.com/image.gif");
            assertThat(mime).isEqualTo("image/gif");
        }

        @Test
        @DisplayName(".png → image/png")
        void png() {
            String mime = captureMimeForUrl("https://ex.com/img.png");
            assertThat(mime).isEqualTo("image/png");
        }

        @Test
        @DisplayName(".webp → image/webp")
        void webp() {
            String mime = captureMimeForUrl("https://cdn.ex.com/a.webp");
            assertThat(mime).isEqualTo("image/webp");
        }

        @Test
        @DisplayName(".bmp → image/bmp")
        void bmp() {
            String mime = captureMimeForUrl("https://ex.com/raw.bmp");
            assertThat(mime).isEqualTo("image/bmp");
        }

        @Test
        @DisplayName(".tif/.tiff → image/tiff")
        void tiffVariants() {
            String mime1 = captureMimeForUrl("https://ex.com/scan.tif");
            assertThat(mime1).isEqualTo("image/tiff");

            String mime2 = captureMimeForUrl("https://ex.com/scan.tiff");
            assertThat(mime2).isEqualTo("image/tiff");
        }

        @Test
        @DisplayName("확장자 없음 → 기본값 image/jpeg")
        void defaultToJpeg() {
            String mime = captureMimeForUrl("https://ex.com/noext");
            assertThat(mime).isEqualTo("image/jpeg");
        }
    }

    private String captureMimeForUrl(String url) {
        // 1. 체인 스텁
        when(chatClient.prompt()).thenReturn(requestSpec);

        // UserMessage 캡처
        ArgumentCaptor<UserMessage> messageCaptor = ArgumentCaptor.forClass(UserMessage.class);
        doReturn(requestSpec).when(requestSpec).messages(messageCaptor.capture());
        doReturn(requestSpec).when(requestSpec).options(any(OpenAiChatOptions.class));
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(eq(BreedAiDetectionResponseDTO.class)))
                .thenReturn(new BreedAiDetectionResponseDTO("강아지", "치와와", List.of("하얀색")));

        // 2. 실행
        breedAiCall(url); // 아래의 작은 헬퍼 사용

        // 3. 캡처한 UserMessage 에서 mime type 확인
        UserMessage um = messageCaptor.getValue();
        assertThat(um).isNotNull();
        assertThat(um.getMedia()).isNotEmpty();

        // org.springframework.util.MimeType 로 들어오므로 문자열 비교가 안전
        return um.getMedia().get(0).getMimeType().toString();
    }

    private void breedAiCall(String url) {
        // analyzeImage 를 한번 호출해서 messages(...) 가 실제로 불리게 함
        client.analyzeImage(url, "p");
    }
}
