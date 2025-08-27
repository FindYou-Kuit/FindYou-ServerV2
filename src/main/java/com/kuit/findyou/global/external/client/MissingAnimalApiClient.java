package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.MissingAnimalApiFullResponse;
import com.kuit.findyou.global.external.dto.MissingAnimalItemDTO;
import com.kuit.findyou.global.external.properties.LossAnimalInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MissingAnimalApiClient {

    private static final int DEFAULT_PAGE_SIZE = 1000;

    private final LossAnimalInfoProperties properties;
    private final RestClient missingAnimalRestClient;

    public MissingAnimalApiClient(
            LossAnimalInfoProperties properties,
            @Qualifier("lossAnimalInfoRestClient") RestClient missingAnimalRestClient
    ) {
        this.properties = properties;
        this.missingAnimalRestClient = missingAnimalRestClient;
    }

    public List<MissingAnimalItemDTO> fetchAllMissingAnimals(String bgnde, String ended) {
        List<MissingAnimalItemDTO> allItems = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            try {
                MissingAnimalApiFullResponse response = fetchPageData(pageNo, bgnde, ended);

                if (isEmptyResponse(response)) {
                    log.warn("[분실동물 공공데이터 응답이 비어있습니다] pageNo={}", pageNo);
                    break;
                }

                List<MissingAnimalItemDTO> currentPageItems = response.response().body().items().item();
                allItems.addAll(currentPageItems);

                if (isLastPage(response, pageNo)) {
                    break;
                }

                pageNo++;

            } catch (Exception e) {
                log.error("[분실동물 공공데이터 페이지 {} 조회 실패]", pageNo, e);
                break;
            }
        }

        log.info("[분실동물 공공데이터 전체 조회 완료] 총 {}건", allItems.size());
        return allItems;
    }

    private MissingAnimalApiFullResponse fetchPageData(int pageNo, String bgnde, String ended) {
        return missingAnimalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", properties.apiKey())
                        .queryParam("bgnde", bgnde)
                        .queryParam("endde", ended)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", DEFAULT_PAGE_SIZE)
                        .queryParam("_type", "json")
                        .build())
                .header("Accept", "application/json")
                .retrieve()
                .body(MissingAnimalApiFullResponse.class);
    }

    private boolean isEmptyResponse(MissingAnimalApiFullResponse response) {
        return response == null ||
                response.response() == null ||
                response.response().body() == null ||
                response.response().body().items() == null ||
                response.response().body().items().item() == null;
    }

    private boolean isLastPage(MissingAnimalApiFullResponse response, int currentPage) {
        int totalCount = Integer.parseInt(response.response().body().totalCount());
        int totalPages = (int) Math.ceil((double) totalCount / DEFAULT_PAGE_SIZE);

        return currentPage >= totalPages;
    }
}