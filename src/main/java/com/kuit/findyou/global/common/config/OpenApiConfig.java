package com.kuit.findyou.global.common.config;

import com.kuit.findyou.global.common.external.properties.ProtectingAnimalApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@EnableConfigurationProperties(ProtectingAnimalApiProperties.class)
public class OpenApiConfig {

    @Bean
    public RestClient protectingAnimalRestClient(ProtectingAnimalApiProperties props) {

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(props.apiUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(props.apiUrl())
                .build();
    }
}
