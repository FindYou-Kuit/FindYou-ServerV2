package com.kuit.findyou.global.external.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.dto.ProtectingAndAdoptedAnimalCount;
import com.kuit.findyou.global.external.dto.RescueAnimalStatsServiceApiResponse;
import com.kuit.findyou.global.external.properties.RescueAnimalStatsApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.HOME_STATISTICS_UPDATE_FAILED;

@Slf4j
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
        log.info("[fetchProtectingAndAdoptedAnimalCount] (bgnde={}, endde={}) 수치 집계 시작", bgnde, endde);
        try {
            String rawResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", properties.apiKey())
                            .queryParam("bgnde", bgnde)
                            .queryParam("endde", endde)
                            .queryParam("_type", "json")
                            .build())
                    .retrieve()
                    .body(String.class);// 원본 문자열로 받기

            RescueAnimalStatsServiceApiResponse resp = null;
            try{
                resp = new ObjectMapper().readValue(rawResponse, RescueAnimalStatsServiceApiResponse.class);
            }
            catch(Exception e){
                log.error("[fetchProtectingAndAdoptedAnimalCount] 응답 역직렬화 불가", e.getMessage());
                throw new RuntimeException("응답을 역직렬화하는 과정에서 오류가 발생했습니다");
            }

            if (resp == null || resp.response() == null
                    || resp.response().body() == null
                    || resp.response().body().items() == null
                    || resp.response().body().items().item() == null
                    || resp.response().body().items().item().isEmpty()) {
                log.error("[fetchProtectingAndAdoptedAnimalCount] 응답이 비어 있음");
                throw new RuntimeException("외부 API 응답이 비어 있습니다");
            }

            String protectingAnimalCount = null;
            String adoptedAnimalCount = null;
            for (RescueAnimalStatsServiceApiResponse.Item item : resp.response().body().items().item()) {
                if (isProtectingAnimalTotalCount(item)) protectingAnimalCount = item.total();
                else if (isAdoptedAnimalCount(item)) adoptedAnimalCount = item.total();
            }

            if (protectingAnimalCount == null){
                log.info("[fetchProtectingAndAdoptedAnimalCount] protectingAnimalCount가 비어 있으므로 기본값으로 대체");
                protectingAnimalCount = "0";
            }
            if (adoptedAnimalCount == null) {
                log.info("[fetchProtectingAndAdoptedAnimalCount] adoptedAnimalCount가 비어 있으므로 기본값으로 대체");
                adoptedAnimalCount = "0";
            }


            log.info("[fetchProtectingAndAdoptedAnimalCount] (bgnde={}, endde={}) 수치 집계 완료", bgnde, endde);
            return new ProtectingAndAdoptedAnimalCount(protectingAnimalCount, adoptedAnimalCount);
        }
        catch (RestClientResponseException e) {
            // HTTP 4xx / 5xx
            log.error("[fetchProtectingAndAdoptedAnimalCount] (bgnde={}, endde={}) 외부서버와 통신 불가  HTTP = {} body = {}", bgnde, endde, e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
        catch (ResourceAccessException e) {
            // 네트워크 오류 / 타임아웃
            log.error("[fetchProtectingAndAdoptedAnimalCount] (bgnde={}, endde={}) 네트워크 오류 혹은 타임아웃 ", bgnde, endde, e);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
        catch (Exception e) {
            // 그 외 알 수 없는 예외
            log.error("[fetchProtectingAndAdoptedAnimalCount] (bgnde={}, endde={}) 수치 집계 실패 ", bgnde, endde);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
    }

    private static boolean isAdoptedAnimalCount(RescueAnimalStatsServiceApiResponse.Item item) {
        return "chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.region()) && "입양".equals(item.processName());
    }

    private static boolean isProtectingAnimalTotalCount(RescueAnimalStatsServiceApiResponse.Item item) {
        return "chart1".equalsIgnoreCase(item.section()) && "전체 지역".equals(item.region()) && "보호중".equals(item.processName());
    }
}
