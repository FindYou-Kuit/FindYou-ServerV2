package com.kuit.findyou.global.common.external.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openapi.protecting-animal")
public record ProtectingAnimalApiProperties(
        String apiUrl,
        String apiKey
) {}
