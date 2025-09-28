package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.dto.LossInfoServiceApiResponse;
import com.kuit.findyou.global.external.properties.LossAnimalInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.HOME_STATISTICS_UPDATE_FAILED;

@Slf4j
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
        log.info("[fetchReportedAnimalCount] (bgnde={}, endde={}) 수치 집계 시작", bgnde, endde);
        try{
            LossInfoServiceApiResponse resp = lossAnimalInfoRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", properties.apiKey())
                            .queryParam("bgnde", bgnde)
                            .queryParam("endde", endde)
                            .queryParam("_type", "json")
                            .build())
                    .retrieve()
                    .body(LossInfoServiceApiResponse.class);

            if (resp == null || resp.response() == null || resp.response().body() == null || resp.response().body().totalCount() == null) {
                throw new RuntimeException("외부 API 응답이 비어 있습니다");
            }

            log.info("[fetchReportedAnimalCount] (bgnde={}, endde={}) 수치 집계 완료", bgnde, endde);
            String reportedAnimalCount = resp.response().body().totalCount();
            return reportedAnimalCount;
        }
        catch (RestClientResponseException e) {
            // HTTP 4xx / 5xx
            log.error("[fetchReportedAnimalCount] (bgnde={}, endde={}) 외부 서버 응답 오류 HTTP = {} body = {}", bgnde, endde, e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
        catch (ResourceAccessException e) {
            // 네트워크 오류 / 타임아웃
            log.error("[fetchReportedAnimalCount] (bgnde={}, endde={}) 네트워크 오류 혹은 타임아웃 ", bgnde, endde, e);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
        catch (Exception e) {
            // 그 외 알 수 없는 예외
            log.error("[fetchReportedAnimalCount] (bgnde={}, endde={}) 수치 집계 실패 ", bgnde, endde);
            throw new CustomException(HOME_STATISTICS_UPDATE_FAILED);
        }
    }
}
