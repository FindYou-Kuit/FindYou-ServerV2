package com.kuit.findyou.global.external.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openapi.loss-animal-info")
public record LossAnimalInfoProperties (
        String apiUrl,
        String apiKey
){

}
