package com.kuit.findyou.global.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kuit.findyou.global.external.properties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;

@Configuration
@EnableConfigurationProperties({
        ProtectingAnimalApiProperties.class,
        KakaoAddressApiProperties.class,
        OpenAiApiProperties.class,
        RescueAnimalStatsApiProperties.class,
        LossAnimalInfoProperties.class,
        VolunteerWorkByKeywordProperties.class
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

    @Bean
    public RestClient volunteerWorkByKeywordRestClient(VolunteerWorkByKeywordProperties props) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(props.apiUrl());
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        // XML 매퍼 생성. 알 수 없는 필드 무시
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // XML 메시지 컨버터 등록
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        xmlConverter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_XML,
                MediaType.TEXT_XML
        ));

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .defaultHeader("Accept", MediaType.APPLICATION_XML_VALUE)
                .messageConverters(converters -> {
                    converters.add(xmlConverter);  // 기본 컨버터들 뒤에 XML 컨버터 추가
                })
                .baseUrl(props.apiUrl())
                .build();
    }
}
