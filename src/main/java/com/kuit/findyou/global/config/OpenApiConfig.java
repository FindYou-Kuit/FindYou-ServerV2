package com.kuit.findyou.global.config;

import com.kuit.findyou.global.external.properties.KakaoAddressApiProperties;
import com.kuit.findyou.global.external.properties.LossAnimalInfoProperties;
import com.kuit.findyou.global.external.properties.OpenAiApiProperties;
import com.kuit.findyou.global.external.properties.ProtectingAnimalApiProperties;
import com.kuit.findyou.global.external.properties.RescueAnimalStatsApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@EnableConfigurationProperties({
        ProtectingAnimalApiProperties.class,
        KakaoAddressApiProperties.class,
        OpenAiApiProperties.class,
        RescueAnimalStatsApiProperties.class,
        LossAnimalInfoProperties.class
})
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

    @Bean
    public RestClient kakaoAddressRestClient(KakaoAddressApiProperties props) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(props.apiUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(props.apiUrl())
                .defaultHeader("Authorization", "KakaoAK " + props.apiKey())
                .build();
    }

    @Bean
    public RestClient openAiRestClient(OpenAiApiProperties props) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(props.apiUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(props.apiUrl())
                .defaultHeader("Authorization", "Bearer " + props.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }


    @Bean
    public RestClient rescueAnimalStatsRestClient(RescueAnimalStatsApiProperties props) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(props.apiUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(props.apiUrl())
                .build();
    }

    @Bean
    public RestClient lossAnimalInfoRestClient(LossAnimalInfoProperties props) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(props.apiUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(props.apiUrl())
                .build();
    }
}
