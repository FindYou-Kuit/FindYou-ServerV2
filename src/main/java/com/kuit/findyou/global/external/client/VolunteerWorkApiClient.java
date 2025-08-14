package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.VolunteerWorksByKeywordApiResponse;
import com.kuit.findyou.global.external.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class VolunteerWorkApiClient {
    private final RestClient byKeywordRestClient;
    public VolunteerWorkApiClient(
            @Qualifier("volunteerWorkByKeywordRestClient") RestClient volunteerWorkByKeywordRestClient
    ){
        this.byKeywordRestClient = volunteerWorkByKeywordRestClient;
    }

    public VolunteerWorksByKeywordApiResponse getVolunteerWorksByKeyword(int pageNo, String keyword, int numOfRows){
        VolunteerWorksByKeywordApiResponse entity = byKeywordRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("pageNo", pageNo)
                        .queryParam("keyword", keyword)
                        .queryParam("numOfRows", numOfRows)
                        .build())
                .retrieve()
                .body(VolunteerWorksByKeywordApiResponse.class);

        if (entity.getBody() == null || entity.getHeader() == null) {
            throw new ExternalApiException("외부 API 에러 : body/header가 비어 있음");
        }
        String code = entity.getHeader().getResultCode();
        if (!"00".equals(code)) {
            String resultMsg = entity.getHeader().getResultMsg();
            throw new ExternalApiException("외부 API 에러 : code=" + code + ", msg=" + resultMsg);
        }

        return entity;
    }
}
