package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.constant.ExternalExceptionMessage;
import com.kuit.findyou.global.external.dto.ProtectingAnimalApiFullResponse;
import com.kuit.findyou.global.external.dto.ProtectingAnimalItemDTO;
import com.kuit.findyou.global.external.exception.ProtectingAnimalApiClientException;
import com.kuit.findyou.global.external.properties.ProtectingAnimalApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.HOME_STATISTICS_UPDATE_FAILED;
import static com.kuit.findyou.global.external.constant.ExternalExceptionMessage.*;

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
                    throw new ProtectingAnimalApiClientException(PROTECTING_ANIMAL_API_CLIENT_EMPTY_RESPONSE);
                }

                List<ProtectingAnimalItemDTO> currentPageItems = response.response().body().items().item();
                allItems.addAll(currentPageItems);

                if (isLastPage(response, pageNo)) {
                    break;
                }

                pageNo++;

            } catch (Exception e) {
                log.error("[구조동물 공공데이터 페이지 {} 조회 실패]", pageNo, e);
                throw new ProtectingAnimalApiClientException(PROTECTING_ANIMAL_API_CLIENT_CALL_FAILED);
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
        log.info("[fetchRescuedAnimalCount] (bgnde={}, endde={}) 수치 집계 시작", bgnde, endde);
        try {
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


            if (resp == null || resp.response() == null || resp.response().body() == null || resp.response().body().totalCount() == null) {
                throw new RuntimeException("외부 API 응답이 비어 있습니다");
            }

            log.info("[fetchRescuedAnimalCount] (bgnde={}, endde={}) 수치 집계 완료", bgnde, endde);
            String rescuedAnimalCount = resp.response().body().totalCount();
            return rescuedAnimalCount;
        }
        catch (RestClientResponseException e) {
            // HTTP 4xx / 5xx
            log.error("[fetchRescuedAnimalCount] (bgnde={}, endde={}) 외부서버와 통신 불가  HTTP = {} body = {}", bgnde, endde, e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
        catch (ResourceAccessException e) {
            // 네트워크 오류 / 타임아웃
            log.error("[fetchRescuedAnimalCount] (bgnde={}, endde={}) 네트워크 오류 혹은 타임아웃 ", bgnde, endde, e);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
        catch (Exception e) {
            // 그 외 알 수 없는 예외
            log.error("[fetchRescuedAnimalCount] (bgnde={}, endde={}) 수치 집계 실패 ", bgnde, endde);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
    }

}