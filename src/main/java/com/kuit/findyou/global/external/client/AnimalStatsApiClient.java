package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.ProtectingAndAdoptedAnimalCount;
import com.kuit.findyou.global.external.dto.RescueAnimalStatsServiceApiResponse;
import com.kuit.findyou.global.external.properties.RescueAnimalStatsApiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AnimalStatsApiClient {
    private final RescueAnimalStatsApiProperties properties;
    private final RestClient restClient;

    public AnimalStatsApiClient(
            RescueAnimalStatsApiProperties properties,
            @Qualifier("rescueAnimalStatsRestClient") RestClient restClient){
        this.restClient = restClient;
        this.properties = properties;
    }

    public ProtectingAndAdoptedAnimalCount fetchProtectingAndAdoptedAnimalCount(String bgnde, String endde) {
        RescueAnimalStatsServiceApiResponse resp = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", properties.apiKey())
                        .queryParam("bgnde", bgnde)
                        .queryParam("endde", endde)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .body(RescueAnimalStatsServiceApiResponse.class);

        String protectingAnimalCount = "0";
        String adoptedAnimalCount = "0";
        for(RescueAnimalStatsServiceApiResponse.Item item : resp.response().body().items().item()){
            if(isProtectingAnimalTotalCount(item)) protectingAnimalCount = item.total();
            else if(isAdoptedAnimalCount(item)) adoptedAnimalCount = item.total();
        }

        return new ProtectingAndAdoptedAnimalCount(protectingAnimalCount, adoptedAnimalCount);
    }

    private static boolean isAdoptedAnimalCount(RescueAnimalStatsServiceApiResponse.Item item) {
        return "chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.regoin()) && "입양".equals(item.prcesssName());
    }

    private static boolean isProtectingAnimalTotalCount(RescueAnimalStatsServiceApiResponse.Item item) {
        return "chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.regoin()) && "보호중".equals(item.prcesssName());
    }
}
