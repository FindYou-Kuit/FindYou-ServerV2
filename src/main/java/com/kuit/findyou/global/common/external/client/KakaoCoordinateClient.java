package com.kuit.findyou.global.common.external.client;

import com.kuit.findyou.global.common.external.dto.KakaoAddressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@Slf4j
public class KakaoAddressClient {

    private final RestClient kakaoAddressRestClient;

    public KakaoAddressClient(@Qualifier("kakaoAddressRestClient") RestClient kakaoAddressRestClient) {
        this.kakaoAddressRestClient = kakaoAddressRestClient;
    }

    private static final BigDecimal DEFAULT_COORDINATE = BigDecimal.valueOf(0.0);

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
                return new Coordinate(DEFAULT_COORDINATE, DEFAULT_COORDINATE);
            }

            KakaoAddressResponse.Document first = response.documents().get(0);
            return new Coordinate(
                    new BigDecimal(first.y()), // 위도
                    new BigDecimal(first.x())  // 경도
            );

        } catch (Exception e) {
            log.error("[Kakao 주소 API 요청 실패] address={}", address, e);
            return new Coordinate(DEFAULT_COORDINATE, DEFAULT_COORDINATE);
        }
    }

    public record Coordinate(BigDecimal latitude, BigDecimal longitude) {}
}
