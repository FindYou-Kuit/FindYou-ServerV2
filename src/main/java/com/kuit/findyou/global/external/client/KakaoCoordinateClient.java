package com.kuit.findyou.global.external.client;

import com.kuit.findyou.global.external.dto.KakaoAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@Slf4j
public class KakaoCoordinateClient {

    private final RestClient kakaoAddressRestClient;

    private static final Coordinate DEFAULT_COORDINATE = new Coordinate(BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0));

    public KakaoCoordinateClient(@Qualifier("kakaoAddressRestClient") RestClient kakaoAddressRestClient) {
        this.kakaoAddressRestClient = kakaoAddressRestClient;
    }

    public Coordinate getCoordinatesFromAddress(String address) {
        try {
            KakaoAddressResponse response = kakaoAddressRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("query", address)
                            .build())
                    .retrieve()
                    .body(KakaoAddressResponse.class);

            if (response == null || response.documents().isEmpty()) {
                log.warn("[Kakao 주소 변환 실패] address={}", address);
                return DEFAULT_COORDINATE;
            }

            KakaoAddressResponse.Document first = response.documents().get(0);
            return new Coordinate(
                    new BigDecimal(first.y()), // 위도
                    new BigDecimal(first.x())  // 경도
            );

        } catch (Exception e) {
            log.error("[Kakao 주소 API 요청 실패] address={}", address, e);
            return DEFAULT_COORDINATE;
        }
    }

    public record Coordinate(BigDecimal latitude, BigDecimal longitude) {}
}
