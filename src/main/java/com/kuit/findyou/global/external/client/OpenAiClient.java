package com.kuit.findyou.global.external.client;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;

@Component
@Slf4j
public class OpenAiClient {

    private static final String BREED_DETECTION_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "species": {
              "type": "string",
              "enum": ["강아지", "고양이", "기타"]
            },
            "breed": { "type": "string" },
            "furColors": {
              "type": "array",
              "items": {
                "type": "string",
                "enum": ["검은색","노란색","점박이","하얀색","갈색","회색","적색","기타"]
              }
            }
          },
          "required": ["species", "breed", "furColors"],
          "additionalProperties": false
        }
        """;

    private final ChatClient chatClient;

    public OpenAiClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public BreedAiDetectionResponseDTO analyzeImage(String imageUrl, String prompt) {
        try {
            UserMessage user = UserMessage.builder()
                    .text(prompt)
                    .media(List.of(
                            Media.builder()
                                    .mimeType(guessImageMediaType(imageUrl))
                                    .data(URI.create(imageUrl))
                                    .build()
                    ))
                    .build();

            BreedAiDetectionResponseDTO response = chatClient
                    .prompt()
                    .messages(user)
                    .options(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(50)
                            .temperature(0.0)
                            .responseFormat(new ResponseFormat(
                                    ResponseFormat.Type.JSON_SCHEMA, BREED_DETECTION_SCHEMA))
                            .build())
                    .call()
                    .entity(BreedAiDetectionResponseDTO.class);

            log.info("OpenAI Vision API 응답: {}", response);

            if (response == null) {
                throw new OpenAiClientException(OPENAI_CLIENT_EMPTY_RESPONSE);
            }

            return response;

        } catch (OpenAiClientException e) {
            log.error("OpenAI Vision API 응답이 비어있습니다.", e);
            throw new OpenAiClientException(OPENAI_CLIENT_EMPTY_RESPONSE);
        }
        catch (Exception e) {
            log.error("OpenAI Vision API 호출 중 오류 발생", e);
            throw new OpenAiClientException(OPENAI_CLIENT_CALL_FAILED, e);
        }
    }

    private MediaType guessImageMediaType(String inputUrl) {
        String url = inputUrl.toLowerCase();

        if (url.endsWith(".jpg") || url.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (url.endsWith(".png"))  return MediaType.IMAGE_PNG;
        if (url.endsWith(".gif"))  return MediaType.IMAGE_GIF;
        if (url.endsWith(".webp")) return MediaType.valueOf("image/webp");
        if (url.endsWith(".bmp"))  return MediaType.valueOf("image/bmp");
        if (url.endsWith(".tif") || url.endsWith(".tiff")) return MediaType.valueOf("image/tiff");

        // 기본값 - 대부분의 사진이 jpeg 이므로 안전한 default
        return MediaType.IMAGE_JPEG;
    }
}
