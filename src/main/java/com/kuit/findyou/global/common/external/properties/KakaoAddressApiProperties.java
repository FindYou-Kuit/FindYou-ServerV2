package com.kuit.findyou.global.common.external.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openapi.kakao")
public record KakaoAddressApiProperties(
        String apiUrl,
        String apiKey
) {
}
