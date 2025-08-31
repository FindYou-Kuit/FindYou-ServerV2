package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.dto.OpenAiResponse;
import com.kuit.findyou.global.external.exception.OpenAiClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;

@Component
@Slf4j
public class OpenAiClient {

    private final RestClient openAiRestClient;

    public OpenAiClient(@Qualifier("openAiRestClient") RestClient openAiRestClient) {
        this.openAiRestClient = openAiRestClient;
    }

    public String analyzeImage(String imageUrl, String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o",
                    "max_tokens", 50,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "image_url",
                                                    "image_url", Map.of("url", imageUrl)
                                            ),
                                            Map.of(
                                                    "type", "text",
                                                    "text", prompt
                                            )
                                    )
                            )
                    )
            );

            OpenAiResponse response = openAiRestClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new OpenAiClientException(OPENAI_CLIENT_EMPTY_RESPONSE);
            }

            String content = response.choices().get(0).message().content();
            log.info("OpenAI Vision API 응답: {}", content);

            return content;

        } catch (OpenAiClientException e) {
            log.error("OpenAI Vision API 응답이 비어있습니다.", e);
            throw new OpenAiClientException(OPENAI_CLIENT_EMPTY_RESPONSE);
        }
        catch (Exception e) {
            log.error("OpenAI Vision API 호출 중 오류 발생", e);
            throw new OpenAiClientException(OPENAI_CLIENT_CALL_FAILED, e);
        }
    }
}
