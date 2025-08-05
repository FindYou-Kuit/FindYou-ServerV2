package com.kuit.findyou.global.external.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openapi.openai")
public record OpenAiApiProperties(
        String apiUrl,
        String apiKey
) {
}
