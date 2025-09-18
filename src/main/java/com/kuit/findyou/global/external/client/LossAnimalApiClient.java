package com.kuit.findyou.global.external.client;

import com.kuit.findyou.domain.home.dto.LossInfoServiceApiResponse;
import com.kuit.findyou.global.external.properties.LossAnimalInfoProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LossAnimalApiClient {
    private final LossAnimalInfoProperties properties;
    private final RestClient lossAnimalInfoRestClient;

    public LossAnimalApiClient(
            LossAnimalInfoProperties properties,
            @Qualifier("lossAnimalInfoRestClient") RestClient lossAnimalInfoRestClient){
        this.properties = properties;
        this.lossAnimalInfoRestClient = lossAnimalInfoRestClient;
    }

    public String fetchReportedAnimalCount(String bgnde, String endde) {
        LossInfoServiceApiResponse resp = lossAnimalInfoRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", properties.apiKey())
                        .queryParam("bgnde", bgnde)
                        .queryParam("endde", endde)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .body(LossInfoServiceApiResponse.class);

        String reportedAnimalCount = resp.response().body().totalCount();
        return reportedAnimalCount;
    }
}
