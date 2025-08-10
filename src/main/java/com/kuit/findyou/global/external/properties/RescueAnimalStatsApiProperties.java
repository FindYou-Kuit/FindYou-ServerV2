package com.kuit.findyou.global.external.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openapi.rescue-animal-stats")
public record RescueAnimalStatsApiProperties(
        String apiUrl,
        String apiKey
){ }
