package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.ProtectingAnimalApiFullResponse;
import com.kuit.findyou.global.external.dto.ProtectingAnimalItemDTO;
import com.kuit.findyou.global.external.properties.ProtectingAnimalApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ProtectingAnimalApiClient {

    private static final int DEFAULT_PAGE_SIZE = 1000;
    private static final String API_ENDPOINT = "/abandonmentPublic_v2";

    private final ProtectingAnimalApiProperties properties;
    private final RestClient protectingAnimalRestClient;

    public ProtectingAnimalApiClient(
            ProtectingAnimalApiProperties properties,
            @Qualifier("protectingAnimalRestClient") RestClient protectingAnimalRestClient
    ) {
        this.properties = properties;
        this.protectingAnimalRestClient = protectingAnimalRestClient;
    }

    public List<ProtectingAnimalItemDTO> fetchAllProtectingAnimals() {
        List<ProtectingAnimalItemDTO> allItems = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            try {
                ProtectingAnimalApiFullResponse response = fetchPageData(pageNo);

                if (isEmptyResponse(response)) {
                    log.warn("[구조동물 공공데이터 응답이 비어있습니다] pageNo={}", pageNo);
                    break;
                }

                List<ProtectingAnimalItemDTO> currentPageItems = response.response().body().items().item();
                allItems.addAll(currentPageItems);

                if (isLastPage(response, pageNo)) {
                    break;
                }

                pageNo++;

            } catch (Exception e) {
                log.error("[구조동물 공공데이터 페이지 {} 조회 실패]", pageNo, e);
                break;
            }
        }

        log.info("[구조동물 공공데이터 전체 조회 완료] 총 {}건", allItems.size());
        return allItems;
    }

    private ProtectingAnimalApiFullResponse fetchPageData(int pageNo) {
        return protectingAnimalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_ENDPOINT)
                        .queryParam("serviceKey", properties.apiKey())
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", DEFAULT_PAGE_SIZE)
                        .queryParam("_type", "json")
                        .build())
                .header("Accept", "application/json")
                .retrieve()
                .body(ProtectingAnimalApiFullResponse.class);
    }

    private boolean isEmptyResponse(ProtectingAnimalApiFullResponse response) {
        return response == null ||
                response.response() == null ||
                response.response().body() == null ||
                response.response().body().items() == null ||
                response.response().body().items().item() == null;
    }

    private boolean isLastPage(ProtectingAnimalApiFullResponse response, int currentPage) {
        int totalCount = Integer.parseInt(response.response().body().totalCount());
        int totalPages = (int) Math.ceil((double) totalCount / DEFAULT_PAGE_SIZE);

        return currentPage >= totalPages;
    }

    public String fetchRescuedAnimalCount(String bgnde, String endde) {
        ProtectingAnimalApiFullResponse resp = protectingAnimalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/abandonmentPublic_v2")
                        .queryParam("serviceKey", properties.apiKey())
                        .queryParam("bgnde", bgnde)
                        .queryParam("endde", endde)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .body(ProtectingAnimalApiFullResponse.class);

        String rescuedAnimalCount = resp.response().body().totalCount();

        return rescuedAnimalCount;
    }

}